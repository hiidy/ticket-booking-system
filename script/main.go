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

var (
	// 프로파일링 플래그
	cpuprofile       = flag.String("cpuprofile", "", "CPU 프로파일 출력 파일")
	memprofile       = flag.String("memprofile", "", "메모리 프로파일 출력 파일")
	blockprofile     = flag.String("blockprofile", "", "블로킹 프로파일 출력 파일")
	mutexprofile     = flag.String("mutexprofile", "", "뮤텍스 프로파일 출력 파일")
	goroutineprofile = flag.String("goroutineprofile", "", "고루틴 프로파일 출력 파일")

	// 테스트 설정 플래그
	baseURL       = flag.String("url", "http://localhost:8080/api/bookings/sync", "타겟 URL")
	httpMethod    = flag.String("method", "POST", "HTTP 메서드 (GET, POST)")
	totalRequests = flag.Int("requests", 100, "총 요청 수")
	maxConns      = flag.Int("conns", 2000, "호스트당 최대 연결 수")
	numClients    = flag.Int("clients", 10, "HTTP 클라이언트 개수")
	timeout       = flag.Duration("timeout", 20*time.Second, "요청 타임아웃")

	// 워밍업 플래그
	enableWarmup   = flag.Bool("warmup", true, "워밍업 활성화")
	warmupRequests = flag.Int("warmup-requests", 100, "워밍업 요청 수")

	// 진행률 표시 플래그
	showProgress     = flag.Bool("progress", true, "실시간 진행률 표시")
	progressInterval = flag.Duration("interval", 1*time.Second, "진행률 출력 간격")

	// 페이로드 생성 플래그
	maxMemberID = flag.Int("members", 9000, "최대 멤버 ID")
)

type BookingRequest struct {
	MemberID  int   `json:"memberId"`
	TicketIDs []int `json:"ticketIds"`
	SectionID int   `json:"sectionId"`
}

type SubSection struct {
	SectionID int    // 1..86
	Group     string // "G1","G2","G3","P","R","S","A"
	Index     int
	SeatStart int     // 이 세부 섹션의 좌석 시작 ID
	SeatEnd   int     // 이 세부 섹션의 좌석 끝 ID
	Weight    float64 // 전체에서 이 세부 섹션이 선택될 확률
}

type StatsShard struct {
	// 요청 카운터
	sent      int64
	completed int64
	success   int64
	fail      int64

	// 상태 코드 카운터
	status2xx int64
	status4xx int64
	status5xx int64
	other     int64

	// 레이턴시 통계
	minLatency int64
	maxLatency int64
	sumLatency int64
	histogram  [21]int64 // 0-10ms, 10-20ms, ..., 200ms+

	_ [64 - (11*8+21*8)%64]byte // 캐시 라인 패딩
}

// ShardedStats 샤딩된 통계 구조체
type ShardedStats struct {
	shards    []StatsShard
	numShards int
}

// NewShardedStats 새로운 샤딩된 통계 생성
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

// getShard ID에 해당하는 샤드 반환
func (s *ShardedStats) getShard(id int) *StatsShard {
	return &s.shards[id%s.numShards]
}

// aggregate 모든 샤드의 통계 집계
func (s *ShardedStats) aggregate() (sent, completed, success, fail, status2xx, status4xx, status5xx, other, minLat, maxLat, sumLat int64) {
	minLat = int64(^uint64(0) >> 1)
	maxLat = 0
	hasValidLatency := false

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

		if shardMin < int64(^uint64(0)>>1) {
			hasValidLatency = true
			if shardMin < minLat {
				minLat = shardMin
			}
		}

		if shardMax > maxLat {
			maxLat = shardMax
		}
	}

	if !hasValidLatency {
		minLat = 0
	}
	return
}

// calculatePercentiles 히스토그램 기반 백분위수 계산
func (s *ShardedStats) calculatePercentiles() (p50, p95, p99 int64) {
	totalCounts := make([]int64, 21)

	// 모든 샤드의 히스토그램 합산
	for i := range s.shards {
		for j := 0; j < 21; j++ {
			totalCounts[j] += atomic.LoadInt64(&s.shards[i].histogram[j])
		}
	}

	// 총 요청 수 계산
	total := int64(0)
	for _, count := range totalCounts {
		total += count
	}

	if total == 0 {
		return 0, 0, 0
	}

	// 백분위수 목표값 계산
	p50Target := total * 50 / 100
	p95Target := total * 95 / 100
	p99Target := total * 99 / 100

	// 누적 카운트로 백분위수 찾기
	cumulative := int64(0)
	for bucket, count := range totalCounts {
		cumulative += count

		if p50 == 0 && cumulative >= p50Target {
			p50 = int64(bucket*10 + 5) // 버킷 중간값
		}
		if p95 == 0 && cumulative >= p95Target {
			p95 = int64(bucket*10 + 5)
		}
		if p99 == 0 && cumulative >= p99Target {
			p99 = int64(bucket*10 + 5)
			break
		}
	}

	return
}

var (
	SubSections       []SubSection
	CumulativeWeights []float64
	SectionMap        map[int]SubSection
)

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

	// 섹션 생성
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

	// 누적 가중치 계산
	CumulativeWeights = make([]float64, len(SubSections))
	sum := 0.0
	for i := range SubSections {
		sum += SubSections[i].Weight
		CumulativeWeights[i] = sum
	}

	// 섹션 맵 생성
	SectionMap = make(map[int]SubSection, len(SubSections))
	for _, sub := range SubSections {
		SectionMap[sub.SectionID] = sub
	}
}

var rngPool = sync.Pool{
	New: func() any {
		return rand.New(rand.NewSource(time.Now().UnixNano()))
	},
}

// withRNG RNG Pool에서 임대하여 사용
func withRNG(f func(r *rand.Rand)) {
	r := rngPool.Get().(*rand.Rand)
	f(r)
	rngPool.Put(r)
}

// pickSection 가중치 기반 섹션 선택
func pickSection(r *rand.Rand) int {
	v := r.Float64()
	for i, cw := range CumulativeWeights {
		if v <= cw {
			return SubSections[i].SectionID
		}
	}
	return SubSections[len(SubSections)-1].SectionID
}

// createPayload 예약 요청 페이로드 생성
func createPayload(maxMember int) ([]byte, error) {
	var payload []byte
	var err error

	withRNG(func(r *rand.Rand) {
		sectionID := pickSection(r)
		s := SectionMap[sectionID]

		seatCount := r.Intn(4) + 1
		span := s.SeatEnd - s.SeatStart + 1

		seatIDs := make([]int, 0, seatCount)

		if seatCount >= span {
			for i := 0; i < span; i++ {
				seatIDs = append(seatIDs, s.SeatStart+i)
			}
		} else {
			used := make(map[int]bool, seatCount)
			for len(seatIDs) < seatCount {
				seat := s.SeatStart + r.Intn(span)
				if !used[seat] {
					seatIDs = append(seatIDs, seat)
					used[seat] = true
				}
			}
		}

		req := BookingRequest{
			MemberID:  r.Intn(maxMember) + 1,
			TicketIDs: seatIDs,
			SectionID: sectionID,
		}
		payload, err = json.Marshal(req)
	})

	return payload, err
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

func sendRequest(id int, clients []*http.Client, payload []byte, stats *ShardedStats, wg *sync.WaitGroup, url, method string) {
	defer wg.Done()

	client := clients[id%len(clients)]
	shard := stats.getShard(id)

	ctx, cancel := context.WithTimeout(context.Background(), *timeout)
	defer cancel()

	var req *http.Request
	var err error

	if method == "POST" {
		req, err = http.NewRequestWithContext(ctx, method, url, bytes.NewReader(payload))
		if err != nil {
			atomic.AddInt64(&shard.fail, 1)
			atomic.AddInt64(&shard.completed, 1)
			return
		}
		req.Header.Set("Content-Type", "application/json")
	} else {
		req, err = http.NewRequestWithContext(ctx, method, url, nil)
		if err != nil {
			atomic.AddInt64(&shard.fail, 1)
			atomic.AddInt64(&shard.completed, 1)
			return
		}
	}

	req.Header.Set("X-Request-ID", uuid.New().String())

	start := time.Now()
	resp, err := client.Do(req)
	latencyMs := time.Since(start).Milliseconds()

	atomic.AddInt64(&shard.completed, 1)

	if err != nil {
		atomic.AddInt64(&shard.fail, 1)
		return
	}
	defer resp.Body.Close()

	io.Copy(io.Discard, resp.Body)

	atomic.AddInt64(&shard.success, 1)

	// 레이턴시 통계 업데이트
	atomic.AddInt64(&shard.sumLatency, latencyMs)

	// Min 레이턴시 업데이트
	for {
		oldMin := atomic.LoadInt64(&shard.minLatency)
		if latencyMs >= oldMin {
			break
		}
		if atomic.CompareAndSwapInt64(&shard.minLatency, oldMin, latencyMs) {
			break
		}
	}

	// Max 레이턴시 업데이트
	for {
		oldMax := atomic.LoadInt64(&shard.maxLatency)
		if latencyMs <= oldMax {
			break
		}
		if atomic.CompareAndSwapInt64(&shard.maxLatency, oldMax, latencyMs) {
			break
		}
	}

	// 히스토그램 업데이트
	bucket := latencyMs / 10
	if bucket >= 20 {
		bucket = 20
	}
	atomic.AddInt64(&shard.histogram[bucket], 1)

	// 상태 코드 기록
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

// warmupConnections 연결 워밍업
func warmupConnections(clients []*http.Client, url, method string, warmupCount, numShards int) {
	fmt.Printf("\n연결 워밍업 시작 (%d개 요청)...\n", warmupCount)
	warmupStart := time.Now()

	var wg sync.WaitGroup
	warmupStats := NewShardedStats(numShards)

	for i := 0; i < warmupCount; i++ {
		wg.Add(1)

		var payload []byte
		var err error
		if method == "POST" {
			payload, err = createPayload(*maxMemberID)
			if err != nil {
				wg.Done()
				continue
			}
		}

		go sendRequest(i, clients, payload, warmupStats, &wg, url, method)
	}

	wg.Wait()
	warmupDuration := time.Since(warmupStart)

	_, completed, success, _, _, _, _, _, _, _, _ := warmupStats.aggregate()
	fmt.Printf("워밍업 완료: %d개 완료 (%d개 성공) in %v\n", completed, success, warmupDuration)
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

// printConfig 테스트 설정 출력
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

// printProgress 실시간 진행률 출력
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

// printFinalResults 최종 결과 출력
func printFinalResults(stats *ShardedStats, totalDuration, generationDuration time.Duration, total int) {
	sent, completed, success, fail, status2xx, status4xx, status5xx, other, minLat, maxLat, sumLat := stats.aggregate()

	p50, p95, p99 := stats.calculatePercentiles()

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
		fmt.Printf("  중앙값 (p50):        %d ms\n", p50)
		fmt.Printf("  p95:                 %d ms\n", p95)
		fmt.Printf("  p99:                 %d ms\n", p99)
	}
	fmt.Printf("%s\n", separator)

	fmt.Printf("\n 성능 요약:\n")
	fmt.Printf("   - %s 요청 처리 완료: %v\n", formatNumber(total), totalDuration)
	fmt.Printf("   - 초당 처리량: %s requests/sec\n", formatNumber(int(float64(completed)/totalDuration.Seconds())))
	fmt.Printf("   - 동시 고루틴: %s개, HTTP 클라이언트: %d개, 통계 샤드: %d개\n",
		formatNumber(total), *numClients, stats.numShards)
	fmt.Printf("   - 총 연결 풀: %s (클라이언트당 %s)\n", formatNumber(*maxConns**numClients), formatNumber(*maxConns))
	if success > 0 {
		fmt.Printf("   - 평균 응답시간: %dms (p50: %dms, p95: %dms, p99: %dms)\n",
			sumLat/success, p50, p95, p99)
	}
	fmt.Println()
}

func main() {
	flag.Parse()

	// 프로파일링 설정
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

	// HTTP 메서드 검증
	method := strings.ToUpper(*httpMethod)
	if method != "GET" && method != "POST" {
		log.Fatalf("지원되지 않은 메서드 사용: %s GET, POST만 가능\n", *httpMethod)
	}

	runtime.GOMAXPROCS(runtime.NumCPU())

	numShards := runtime.NumCPU()
	printConfig(method, numShards)

	// HTTP 클라이언트 생성
	fmt.Printf("\n%d개의 HTTP 클라이언트 생성 중.....\n", *numClients)
	clients := make([]*http.Client, *numClients)
	for i := 0; i < *numClients; i++ {
		clients[i] = createOptimizedHTTP2Client(*maxConns, *timeout)
	}
	fmt.Printf("클라이언트 생성 완료 (총 연결 풀: %s)\n", formatNumber(*maxConns**numClients))

	// 워밍업
	if *enableWarmup {
		warmupConnections(clients, *baseURL, method, *warmupRequests, numShards)
	}

	// 통계 초기화
	stats := NewShardedStats(numShards)

	var wg sync.WaitGroup

	// 진행률 표시 고루틴
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

	// 요청 발사
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

	// 진행률 표시 종료
	if *showProgress {
		close(stopProgress)
		progressWg.Wait()
	}

	// 최종 결과 출력
	printFinalResults(stats, totalDuration, generationDuration, *totalRequests)

	// 프로파일 저장
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
