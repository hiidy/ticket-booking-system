package main

import (
	"bytes"
	"context"
	"crypto/tls"
	"encoding/json"
	"flag"
	"fmt"
	"io"
	"log"
	"math/rand"
	"net"
	"net/http"
	"os"
	"runtime"
	"runtime/pprof"
	"strings"
	"sync"
	"sync/atomic"
	"time"

	"github.com/google/uuid"
	"golang.org/x/net/http2"
)

var cpuprofile = flag.String("cpuprofile", "", "CPU 프로파일 출력 파일")
var memprofile = flag.String("memprofile", "", "메모리 프로파일 출력 파일")
var blockprofile = flag.String("blockprofile", "", "블로킹 프로파일 출력 파일")
var mutexprofile = flag.String("mutexprofile", "", "뮤텍스 프로파일 출력 파일")
var goroutineprofile = flag.String("goroutineprofile", "", "고루틴 프로파일 출력 파일")

var (
	baseURL          = flag.String("url", "https://internal-alb-2004079858.ap-northeast-2.elb.amazonaws.com/api/bookings/sync", "타겟 URL")
	httpMethod       = flag.String("method", "POST", "HTTP 메서드 (GET, POST)")
	totalRequests    = flag.Int("requests", 100, "총 요청 수")
	maxConns         = flag.Int("conns", 2000, "호스트당 최대 연결 수")
	timeout          = flag.Duration("timeout", 20*time.Second, "요청 타임아웃")
	maxMemberID      = flag.Int("members", 9000, "최대 멤버 ID")
	showProgress     = flag.Bool("progress", true, "실시간 진행률 표시")
	progressInterval = flag.Duration("interval", 1*time.Second, "진행률 출력 간격")
	enableWarmup     = flag.Bool("warmup", true, "워밍업 활성화")
	warmupRequests   = flag.Int("warmup-requests", 100, "워밍업 요청 수")
	numClients       = flag.Int("clients", 10, "HTTP 클라이언트 개수")
)

type SubSection struct {
	SectionID int     // 1..86 (세부 섹션 고유 번호)
	Group     string  // "G1","G2","G3","P","R","S","A"
	Index     int     // 그룹 내 인덱스 (1부터)
	SeatStart int     // 이 세부 섹션의 좌석 시작 ID (전역 seat id)
	SeatEnd   int     // 이 세부 섹션의 좌석 끝 ID (전역 seat id)
	Weight    float64 // 전체에서 이 세부 섹션이 선택될 확률
}

var (
	SubSections       []SubSection
	CumulativeWeights []float64 // SubSections와 같은 인덱스
)

var rngPool = sync.Pool{
	New: func() any {
		return rand.New(rand.NewSource(time.Now().UnixNano()))
	},
}

func withRNG(f func(r *rand.Rand)) {
	r := rngPool.Get().(*rand.Rand)
	f(r)
	rngPool.Put(r)
}

type BookingRequest struct {
	MemberID  int   `json:"memberId"`
	TicketIDs []int `json:"ticketIds"`
	SectionID int   `json:"sectionId"`
}

type StatsShard struct {
	sent      int64
	completed int64
	success   int64
	fail      int64
	status2xx int64
	status4xx int64
	status5xx int64
	other     int64

	minLatency int64
	maxLatency int64
	sumLatency int64

	_ [64 - (11*8)%64]byte
}

type ShardedStats struct {
	shards    []StatsShard
	numShards int
}

func NewShardedStats(numShards int) *ShardedStats {
	if numShards <= 0 {
		numShards = runtime.NumCPU()
	}

	shards := make([]StatsShard, numShards)
	for i := range shards {
		shards[i].minLatency = int64(^uint64(0) >> 1) // max int64
	}

	return &ShardedStats{
		shards:    shards,
		numShards: numShards,
	}
}

func (s *ShardedStats) getShard(id int) *StatsShard {
	return &s.shards[id%s.numShards]
}

func (s *ShardedStats) aggregate() (sent, completed, success, fail, status2xx, status4xx, status5xx, other, minLat, maxLat, sumLat int64) {
	minLat = int64(^uint64(0) >> 1)
	maxLat = 0

	for i := range s.shards {
		shard := &s.shards[i]

		sent += atomic.LoadInt64(&shard.sent)
		completed += atomic.LoadInt64(&shard.completed)
		success += atomic.LoadInt64(&shard.success)
		fail += atomic.LoadInt64(&shard.fail)
		status2xx += atomic.LoadInt64(&shard.status2xx)
		status4xx += atomic.LoadInt64(&shard.status4xx)
		status5xx += atomic.LoadInt64(&shard.status5xx)
		other += atomic.LoadInt64(&shard.other)
		sumLat += atomic.LoadInt64(&shard.sumLatency)

		shardMin := atomic.LoadInt64(&shard.minLatency)
		shardMax := atomic.LoadInt64(&shard.maxLatency)

		if shardMin < minLat && shardMin > 0 {
			minLat = shardMin
		}
		if shardMax > maxLat {
			maxLat = shardMax
		}
	}

	return
}

func init() {
	type groupDef struct {
		name       string
		count      int
		weight     float64
		totalSeats int
	}

	groups := []groupDef{
		{"G1", 10, 0.25, 6000},
		{"G2", 12, 0.20, 6000},
		{"G3", 12, 0.18, 6000},
		{"P", 20, 0.15, 10000},
		{"R", 11, 0.10, 8000},
		{"S", 12, 0.07, 6000},
		{"A", 9, 0.05, 4000},
	}
	SubSections = make([]SubSection, 0, 86)
	seatCursor := 1
	sectionID := 1

	for _, g := range groups {
		base := g.totalSeats / g.count
		rem := g.totalSeats % g.count
		wPerSub := g.weight / float64(g.count)

		for i := 1; i <= g.count; i++ {
			seatSpan := base
			if i == g.count {
				seatSpan += rem
			}
			start := seatCursor
			end := start + seatSpan - 1

			SubSections = append(SubSections, SubSection{
				SectionID: sectionID,
				Group:     g.name,
				Index:     i,
				SeatStart: start,
				SeatEnd:   end,
				Weight:    wPerSub,
			})

			seatCursor = end + 1
			sectionID++
		}
	}
	CumulativeWeights = make([]float64, len(SubSections))
	sum := 0.0
	for i := range SubSections {
		sum += SubSections[i].Weight
		CumulativeWeights[i] = sum
	}
}

func pickSection(r *rand.Rand) int {
	v := r.Float64()
	for i, cw := range CumulativeWeights {
		if v <= cw {
			return SubSections[i].SectionID
		}
	}
	return SubSections[len(SubSections)-1].SectionID
}

func createPayload(maxMember int) ([]byte, error) {
	var payload []byte

	withRNG(func(r *rand.Rand) {
		sectionID := pickSection(r)

		var s SubSection
		for i := range SubSections {
			if SubSections[i].SectionID == sectionID {
				s = SubSections[i]
				break
			}
		}

		seatCount := r.Intn(4) + 1
		seatIDs := make([]int, seatCount)
		span := s.SeatEnd - s.SeatStart + 1
		for i := 0; i < seatCount; i++ {
			seatIDs[i] = s.SeatStart + r.Intn(span)
		}

		req := BookingRequest{
			MemberID:  r.Intn(maxMember) + 1,
			TicketIDs: seatIDs,
			SectionID: sectionID, // 1..86
		}
		payload, _ = json.Marshal(req)
	})

	return payload, nil
}

func createOptimizedHTTP2Client(maxConnsPerHost int, requestTimeout time.Duration) *http.Client {
	maxIdleConns := maxConnsPerHost * 2
	if maxIdleConns > 10000 {
		maxIdleConns = 10000
	}

	dialer := &net.Dialer{
		Timeout:   10 * time.Second,
		KeepAlive: 30 * time.Second,
	}

	transport := &http.Transport{
		DialContext: dialer.DialContext,

		TLSClientConfig: &tls.Config{
			InsecureSkipVerify: true,
		},

		MaxIdleConns:          maxIdleConns,
		MaxIdleConnsPerHost:   maxConnsPerHost,
		MaxConnsPerHost:       maxConnsPerHost,
		IdleConnTimeout:       90 * time.Second,
		TLSHandshakeTimeout:   10 * time.Second,
		ExpectContinueTimeout: 1 * time.Second,

		DisableKeepAlives:  false,
		DisableCompression: false,
		ForceAttemptHTTP2:  true,

		ResponseHeaderTimeout: 10 * time.Second,
		WriteBufferSize:       32 * 1024,
		ReadBufferSize:        32 * 1024,
	}

	if err := http2.ConfigureTransport(transport); err != nil {
		log.Printf("HTTP/2 설정 실패: %v\n", err)
	}

	return &http.Client{
		Transport: transport,
		Timeout:   requestTimeout,
	}
}

func warmupConnections(clients []*http.Client, url string, method string, count int, numShards int) {
	fmt.Printf("\n워밍업 시작: %d개의 연결 미리 생성 중 (%d개 클라이언트 사용)...\n", count, len(clients))
	warmupStart := time.Now()

	var wg sync.WaitGroup
	successCount := int64(0)

	for i := 0; i < count; i++ {
		wg.Add(1)
		go func(id int) {
			defer wg.Done()

			client := clients[id%len(clients)]

			var req *http.Request
			var err error

			if method == "GET" {
				req, err = http.NewRequest("GET", url, nil)
			} else {
				payload, _ := createPayload(*maxMemberID)
				req, err = http.NewRequest("POST", url, bytes.NewBuffer(payload))
				if err == nil {
					req.Header.Set("Content-Type", "application/json")
					req.Header.Set("Idempotency-Key", uuid.New().String())
				}
			}

			if err != nil {
				return
			}

			ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
			defer cancel()
			req = req.WithContext(ctx)

			resp, err := client.Do(req)
			if err == nil {
				io.Copy(io.Discard, resp.Body)
				resp.Body.Close()
				atomic.AddInt64(&successCount, 1)
			}
		}(i)
	}

	wg.Wait()
	warmupDuration := time.Since(warmupStart)

	fmt.Printf("워밍업 완료: %d개 연결 생성 (성공: %d/%d, 소요시간: %v)\n",
		count, successCount, count, warmupDuration)
	fmt.Printf("평균 연결 생성 시간: %.2fms\n",
		float64(warmupDuration.Milliseconds())/float64(count))
	fmt.Printf("클라이언트당 연결: ~%d개\n\n", count/len(clients))

	time.Sleep(500 * time.Millisecond)
}

func sendRequest(id int, clients []*http.Client, payload []byte, stats *ShardedStats, wg *sync.WaitGroup, url string, method string) {
	defer wg.Done()

	shard := stats.getShard(id)
	client := clients[id%len(clients)]

	startTime := time.Now()

	var req *http.Request
	var err error

	if method == "GET" {
		req, err = http.NewRequest("GET", url, nil)
	} else {
		req, err = http.NewRequest("POST", url, bytes.NewBuffer(payload))
		if err == nil {
			req.Header.Set("Content-Type", "application/json")
			req.Header.Set("Idempotency-Key", uuid.New().String())
		}
	}
	if err != nil {
		atomic.AddInt64(&shard.fail, 1)
		atomic.AddInt64(&shard.completed, 1)
		return
	}

	ctx, cancel := context.WithTimeout(context.Background(), *timeout)
	defer cancel()
	req = req.WithContext(ctx)

	resp, err := client.Do(req)

	latency := time.Since(startTime).Milliseconds()
	atomic.AddInt64(&shard.completed, 1)

	if err != nil {
		atomic.AddInt64(&shard.fail, 1)
		return
	}
	io.Copy(io.Discard, resp.Body)
	resp.Body.Close()

	atomic.AddInt64(&shard.success, 1)
	atomic.AddInt64(&shard.sumLatency, latency)

	for {
		oldMin := atomic.LoadInt64(&shard.minLatency)
		if oldMin == 0 || latency < oldMin {
			if atomic.CompareAndSwapInt64(&shard.minLatency, oldMin, latency) {
				break
			}
		} else {
			break
		}
	}

	for {
		oldMax := atomic.LoadInt64(&shard.maxLatency)
		if latency > oldMax {
			if atomic.CompareAndSwapInt64(&shard.maxLatency, oldMax, latency) {
				break
			}
		} else {
			break
		}
	}

	switch {
	case resp.StatusCode >= 200 && resp.StatusCode < 300:
		atomic.AddInt64(&shard.status2xx, 1)
	case resp.StatusCode >= 400 && resp.StatusCode < 500:
		atomic.AddInt64(&shard.status4xx, 1)
	case resp.StatusCode >= 500 && resp.StatusCode < 600:
		atomic.AddInt64(&shard.status5xx, 1)
	default:
		atomic.AddInt64(&shard.other, 1)
	}
}

func printProgress(stats *ShardedStats, elapsed time.Duration, total int) {
	sent, completed, success, fail, _, _, _, _, _, _, sumLat := stats.aggregate()

	rps := float64(completed) / elapsed.Seconds()
	progress := float64(completed) / float64(total) * 100

	avgLatency := int64(0)
	if success > 0 {
		avgLatency = sumLat / success
	}

	fmt.Printf("[%6.1fs] Sent: %7d | Done: %7d/%d (%.1f%%) | RPS: %8.0f | Succ: %7d | Fail: %6d | Avg: %4dms\n",
		elapsed.Seconds(), sent, completed, total, progress, rps, success, fail, avgLatency)
}

func printConfig(method string, numShards int) {
	separator := strings.Repeat("=", 75)
	fmt.Printf("%s\n", separator)
	fmt.Printf(" HTTP/2 부하 테스트 시작 (샤딩된 통계 구조)\n")
	fmt.Printf("%s\n", separator)
	fmt.Printf("설정:\n")
	fmt.Printf("  HTTP 메서드:         %s\n", method)
	fmt.Printf("  타겟 URL:            %s\n", *baseURL)
	fmt.Printf("  총 요청 수:          %s\n", formatNumber(*totalRequests))
	fmt.Printf("  동시 고루틴 수:      %s\n", formatNumber(*totalRequests))
	fmt.Printf("  HTTP 클라이언트 수:  %d개\n", *numClients)
	fmt.Printf("  통계 샤드 수:        %d개 (CPU 코어당 1개)\n", numShards)
	fmt.Printf("  최대 연결 수:        %s (per host, per client)\n", formatNumber(*maxConns))
	fmt.Printf("  총 연결 풀 크기:     %s (= %d clients × %d conns)\n",
		formatNumber(*maxConns**numClients), *numClients, *maxConns)
	fmt.Printf("  요청 타임아웃:       %v\n", *timeout)
	if *enableWarmup {
		fmt.Printf("  워밍업:              활성화 (%d개 요청)\n", *warmupRequests)
	} else {
		fmt.Printf("  워밍업:              비활성화\n")
	}
	if method == "POST" {
		fmt.Printf("  최대 멤버 ID:        %s\n", formatNumber(*maxMemberID))
	}
	fmt.Printf("  CPU 코어 수:         %d\n", runtime.NumCPU())

	if *cpuprofile != "" {
		fmt.Printf("  CPU 프로파일:        %s\n", *cpuprofile)
	}
	if *memprofile != "" {
		fmt.Printf("  메모리 프로파일:     %s\n", *memprofile)
	}
	if *blockprofile != "" {
		fmt.Printf("  블로킹 프로파일:     %s\n", *blockprofile)
	}
	if *mutexprofile != "" {
		fmt.Printf("  뮤텍스 프로파일:     %s\n", *mutexprofile)
	}
	if *goroutineprofile != "" {
		fmt.Printf("  고루틴 프로파일:     %s\n", *goroutineprofile)
	}

	fmt.Printf("%s\n", separator)
}

func formatNumber(n int) string {
	s := fmt.Sprintf("%d", n)
	var result []byte
	for i, c := range s {
		if i > 0 && (len(s)-i)%3 == 0 {
			result = append(result, ',')
		}
		result = append(result, byte(c))
	}
	return string(result)
}

func printFinalResults(stats *ShardedStats, totalDuration, generationDuration time.Duration, total int) {
	sent, completed, success, fail, status2xx, status4xx, status5xx, other, minLat, maxLat, sumLat := stats.aggregate()

	separator := strings.Repeat("=", 75)
	fmt.Printf("\n%s\n", separator)
	fmt.Printf("최종 결과\n")
	fmt.Printf("%s\n", separator)
	fmt.Printf("시간:\n")
	fmt.Printf("  총 소요 시간:        %v\n", totalDuration)
	fmt.Printf("  요청 생성 시간:      %v\n", generationDuration)
	fmt.Printf("  요청 처리 시간:      %v\n", totalDuration)
	fmt.Printf("\n")
	fmt.Printf("요청:\n")
	fmt.Printf("  총 요청 수:          %s\n", formatNumber(total))
	fmt.Printf("  발사된 요청:         %s\n", formatNumber(int(sent)))
	fmt.Printf("  완료된 요청:         %s\n", formatNumber(int(completed)))
	fmt.Printf("\n")
	fmt.Printf("처리량:\n")
	fmt.Printf("  평균 RPS:            %s req/s\n", formatNumber(int(float64(completed)/totalDuration.Seconds())))
	fmt.Printf("  최대 RPS:            %s req/s (이론상)\n", formatNumber(int(float64(total)/totalDuration.Seconds())))
	fmt.Printf("\n")

	totalCompleted := success + fail

	fmt.Printf("결과:\n")
	fmt.Printf("  성공:                %s (%.2f%%)\n", formatNumber(int(success)), float64(success)/float64(totalCompleted)*100)
	fmt.Printf("  실패:                %s (%.2f%%)\n", formatNumber(int(fail)), float64(fail)/float64(totalCompleted)*100)
	fmt.Printf("\n")
	fmt.Printf("상태 코드:\n")
	fmt.Printf("  2xx:                 %s\n", formatNumber(int(status2xx)))
	fmt.Printf("  4xx:                 %s\n", formatNumber(int(status4xx)))
	fmt.Printf("  5xx:                 %s\n", formatNumber(int(status5xx)))
	fmt.Printf("  기타:                 %s\n", formatNumber(int(other)))
	fmt.Printf("\n")

	if success > 0 {
		fmt.Printf("응답 시간 (Latency):\n")
		fmt.Printf("  최소:                %d ms\n", minLat)
		fmt.Printf("  최대:                %d ms\n", maxLat)
		fmt.Printf("  평균:                %d ms\n", sumLat/success)
	}
	fmt.Printf("%s\n", separator)

	fmt.Printf("\n 성능 요약:\n")
	fmt.Printf("   - %s 요청 처리 완료: %v\n", formatNumber(total), totalDuration)
	fmt.Printf("   - 초당 처리량: %s requests/sec\n", formatNumber(int(float64(completed)/totalDuration.Seconds())))
	fmt.Printf("   - 동시 고루틴: %s개, HTTP 클라이언트: %d개, 통계 샤드: %d개\n",
		formatNumber(total), *numClients, stats.numShards)
	fmt.Printf("   - 총 연결 풀: %s (클라이언트당 %s)\n", formatNumber(*maxConns**numClients), formatNumber(*maxConns))
	if success > 0 {
		fmt.Printf("   - 평균 응답시간: %dms\n", sumLat/success)
	}
	fmt.Println()
}

func main() {
	flag.Parse()

	if *cpuprofile != "" {
		f, err := os.Create(*cpuprofile)
		if err != nil {
			log.Fatal("CPU 프로파일 생성 실패:", err)
		}
		defer f.Close()
		if err := pprof.StartCPUProfile(f); err != nil {
			log.Fatal("CPU 프로파일 시작 실패:", err)
		}
		defer pprof.StopCPUProfile()
		fmt.Printf("CPU 프로파일링 활성화: %s\n", *cpuprofile)
	}

	if *blockprofile != "" {
		runtime.SetBlockProfileRate(1)
		fmt.Printf("블로킹 프로파일링 활성화: %s\n", *blockprofile)
	}

	if *mutexprofile != "" {
		runtime.SetMutexProfileFraction(1)
		fmt.Printf("뮤텍스 프로파일링 활성화: %s\n", *mutexprofile)
	}

	method := strings.ToUpper(*httpMethod)
	if method != "GET" && method != "POST" {
		log.Fatalf("지원되지 않은 메서드 사용: %s GET, POST만 가능\n", *httpMethod)
	}

	runtime.GOMAXPROCS(runtime.NumCPU())

	numShards := runtime.NumCPU()
	printConfig(method, numShards)

	fmt.Printf("\n%d개의 HTTP 클라이언트 생성 중.....\n", *numClients)
	clients := make([]*http.Client, *numClients)
	for i := 0; i < *numClients; i++ {
		clients[i] = createOptimizedHTTP2Client(*maxConns, *timeout)
	}
	fmt.Printf("클라이언트 생성 완료 (총 연결 풀: %s)\n", formatNumber(*maxConns**numClients))

	if *enableWarmup {
		warmupConnections(clients, *baseURL, method, *warmupRequests, numShards)
	}

	stats := NewShardedStats(numShards)

	var wg sync.WaitGroup

	var stopProgress chan bool
	var progressWg sync.WaitGroup

	if *showProgress {
		stopProgress = make(chan bool)
		progressWg.Add(1)

		go func() {
			defer progressWg.Done()
			ticker := time.NewTicker(*progressInterval)
			defer ticker.Stop()
			startTime := time.Now()

			for {
				select {
				case <-ticker.C:
					elapsed := time.Since(startTime)
					printProgress(stats, elapsed, *totalRequests)
				case <-stopProgress:
					return
				}
			}
		}()
	}

	fmt.Printf(" 고루틴 %s개 생성 및 요청 시작 (통계 샤드: %d개)\n\n", formatNumber(*totalRequests), numShards)
	startTime := time.Now()
	generationStart := time.Now()

	for i := 0; i < *totalRequests; i++ {
		wg.Add(1)

		var payload []byte
		var err error

		if method == "POST" {
			payload, err = createPayload(*maxMemberID)
			if err != nil {
				fmt.Printf("Payload 생성 실패 #%d: %v\n", i, err)
				wg.Done()
				continue
			}
		}

		go sendRequest(i, clients, payload, stats, &wg, *baseURL, method)

		shard := stats.getShard(i)
		atomic.AddInt64(&shard.sent, 1)
	}

	generationDuration := time.Since(generationStart)
	fmt.Printf(" 모든 고루틴 발사 완료 (소요시간: %v)\n", generationDuration)
	fmt.Println("\n 모든 고루틴의 처리 완료 대기 중....")

	wg.Wait()

	totalDuration := time.Since(startTime)

	if *showProgress {
		close(stopProgress)
		progressWg.Wait()
	}

	printFinalResults(stats, totalDuration, generationDuration, *totalRequests)

	if *memprofile != "" {
		f, err := os.Create(*memprofile)
		if err != nil {
			log.Fatal("메모리 프로파일 생성 실패:", err)
		}
		defer f.Close()
		runtime.GC()
		if err := pprof.WriteHeapProfile(f); err != nil {
			log.Fatal("메모리 프로파일 작성 실패:", err)
		}
		fmt.Printf("\n메모리 프로파일 저장 완료: %s\n", *memprofile)
	}

	if *blockprofile != "" {
		f, err := os.Create(*blockprofile)
		if err != nil {
			log.Fatal("블로킹 프로파일 생성 실패:", err)
		}
		defer f.Close()
		if err := pprof.Lookup("block").WriteTo(f, 0); err != nil {
			log.Fatal("블로킹 프로파일 작성 실패:", err)
		}
		fmt.Printf("블로킹 프로파일 저장 완료: %s\n", *blockprofile)
	}

	if *mutexprofile != "" {
		f, err := os.Create(*mutexprofile)
		if err != nil {
			log.Fatal("뮤텍스 프로파일 생성 실패:", err)
		}
		defer f.Close()
		if err := pprof.Lookup("mutex").WriteTo(f, 0); err != nil {
			log.Fatal("뮤텍스 프로파일 작성 실패:", err)
		}
		fmt.Printf("뮤텍스 프로파일 저장 완료: %s\n", *mutexprofile)
	}

	if *goroutineprofile != "" {
		f, err := os.Create(*goroutineprofile)
		if err != nil {
			log.Fatal("고루틴 프로파일 생성 실패:", err)
		}
		defer f.Close()
		if err := pprof.Lookup("goroutine").WriteTo(f, 0); err != nil {
			log.Fatal("고루틴 프로파일 작성 실패:", err)
		}
		fmt.Printf("고루틴 프로파일 저장 완료: %s\n", *goroutineprofile)
	}
}
