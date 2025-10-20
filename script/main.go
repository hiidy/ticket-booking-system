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
var traceprofile = flag.String("traceprofile", "", "실행 추적 출력 파일")

var (
	baseURL          = flag.String("url", "https://internal-alb-2004079858.ap-northeast-2.elb.amazonaws.com/api/bookings/sync", "타겟 URL")
	httpMethod       = flag.String("method", "GET", "HTTP 메서드 (GET, POST)")
	totalRequests    = flag.Int("requests", 100, "총 요청 수")
	maxConns         = flag.Int("conns", 2000, "호스트당 최대 연결 수")
	timeout          = flag.Duration("timeout", 20*time.Second, "요청 타임아웃")
	maxMemberID      = flag.Int("members", 9000, "최대 멤버 ID")
	showProgress     = flag.Bool("progress", true, "실시간 진행률 표시")
	progressInterval = flag.Duration("interval", 1*time.Second, "진행률 출력 간격")
	enableWarmup     = flag.Bool("warmup", true, "워밍업 활성화")
	warmupRequests   = flag.Int("warmup-requests", 100, "워밍업 요청 수")
	numClients       = flag.Int("clients", 10, "HTTP 클라이언트 개수 (mutex 경합 감소)")
)

var (
	SECTION_WEIGHTS = []float64{0.35, 0.25, 0.15, 0.10, 0.08, 0.07}
	SECTION_RANGES  = [][2]int{
		{1, 75}, {76, 150}, {151, 225},
		{226, 300}, {301, 375}, {376, 450},
	}
	CUMULATIVE_WEIGHTS []float64
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

type Stats struct {
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
}

func init() {
	rand.Seed(time.Now().UnixNano())
	CUMULATIVE_WEIGHTS = make([]float64, len(SECTION_WEIGHTS))
	sum := 0.0
	for i, w := range SECTION_WEIGHTS {
		sum += w
		CUMULATIVE_WEIGHTS[i] = sum
	}
}

func pickSection(r *rand.Rand) int {
	v := r.Float64()
	for i, cw := range CUMULATIVE_WEIGHTS {
		if v <= cw {
			return i + 1
		}
	}
	return 6
}

func createPayload(maxMember int) ([]byte, error) {
	var payload []byte

	withRNG(func(r *rand.Rand) {
		sectionID := pickSection(r)
		sectionRange := SECTION_RANGES[sectionID-1]

		seatCount := r.Intn(4) + 1
		seatIDs := make([]int, seatCount)
		for i := 0; i < seatCount; i++ {
			seatIDs[i] = r.Intn(sectionRange[1]-sectionRange[0]+1) + sectionRange[0]
		}

		req := BookingRequest{
			MemberID:  r.Intn(maxMember) + 1,
			TicketIDs: seatIDs,
			SectionID: sectionID,
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

func warmupConnections(clients []*http.Client, url string, method string, count int) {
	fmt.Printf("\n🔥 워밍업 시작: %d개의 연결 미리 생성 중 (%d개 클라이언트 사용)...\n", count, len(clients))
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

	fmt.Printf("✓ 워밍업 완료: %d개 연결 생성 (성공: %d/%d, 소요시간: %v)\n",
		count, successCount, count, warmupDuration)
	fmt.Printf("  평균 연결 생성 시간: %.2fms\n",
		float64(warmupDuration.Milliseconds())/float64(count))
	fmt.Printf("  클라이언트당 연결: ~%d개\n\n", count/len(clients))

	time.Sleep(500 * time.Millisecond)
}

func sendRequest(id int, clients []*http.Client, payload []byte, stats *Stats, wg *sync.WaitGroup, url string, method string) {
	defer wg.Done()

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
		atomic.AddInt64(&stats.fail, 1)
		atomic.AddInt64(&stats.completed, 1)
		return
	}

	ctx, cancel := context.WithTimeout(context.Background(), *timeout)
	req = req.WithContext(ctx)

	resp, err := client.Do(req)
	cancel()

	latency := time.Since(startTime).Milliseconds()
	atomic.AddInt64(&stats.completed, 1)

	if err != nil {
		atomic.AddInt64(&stats.fail, 1)
		return
	}
	io.Copy(io.Discard, resp.Body)
	resp.Body.Close()

	atomic.AddInt64(&stats.success, 1)
	atomic.AddInt64(&stats.sumLatency, latency)

	for {
		oldMin := atomic.LoadInt64(&stats.minLatency)
		if oldMin == 0 || latency < oldMin {
			if atomic.CompareAndSwapInt64(&stats.minLatency, oldMin, latency) {
				break
			}
		} else {
			break
		}
	}

	for {
		oldMax := atomic.LoadInt64(&stats.maxLatency)
		if latency > oldMax {
			if atomic.CompareAndSwapInt64(&stats.maxLatency, oldMax, latency) {
				break
			}
		} else {
			break
		}
	}

	switch {
	case resp.StatusCode >= 200 && resp.StatusCode < 300:
		atomic.AddInt64(&stats.status2xx, 1)
	case resp.StatusCode >= 400 && resp.StatusCode < 500:
		atomic.AddInt64(&stats.status4xx, 1)
	case resp.StatusCode >= 500 && resp.StatusCode < 600:
		atomic.AddInt64(&stats.status5xx, 1)
	default:
		atomic.AddInt64(&stats.other, 1)
	}
}

func printProgress(stats *Stats, elapsed time.Duration, total int) {
	sent := atomic.LoadInt64(&stats.sent)
	completed := atomic.LoadInt64(&stats.completed)
	success := atomic.LoadInt64(&stats.success)
	fail := atomic.LoadInt64(&stats.fail)

	rps := float64(completed) / elapsed.Seconds()
	progress := float64(completed) / float64(total) * 100

	avgLatency := int64(0)
	if success > 0 {
		avgLatency = atomic.LoadInt64(&stats.sumLatency) / success
	}

	fmt.Printf("[%6.1fs] Sent: %7d | Done: %7d/%d (%.1f%%) | RPS: %8.0f | Succ: %7d | Fail: %6d | Avg: %4dms\n",
		elapsed.Seconds(), sent, completed, total, progress, rps, success, fail, avgLatency)
}

func printConfig(method string) {
	separator := strings.Repeat("=", 75)
	fmt.Printf("%s\n", separator)
	fmt.Printf(" HTTP/2 부하 테스트 시작 (다중 클라이언트)\n")
	fmt.Printf("%s\n", separator)
	fmt.Printf("설정:\n")
	fmt.Printf("  HTTP 메서드:         %s\n", method)
	fmt.Printf("  타겟 URL:            %s\n", *baseURL)
	fmt.Printf("  총 요청 수:          %s\n", formatNumber(*totalRequests))
	fmt.Printf("  동시 고루틴 수:      %s\n", formatNumber(*totalRequests))
	fmt.Printf("  HTTP 클라이언트 수:  %d개 (mutex 경합 감소)\n", *numClients)
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
	if *traceprofile != "" {
		fmt.Printf("  실행 추적:           %s\n", *traceprofile)
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

func printFinalResults(stats *Stats, totalDuration, generationDuration time.Duration, total int) {
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
	fmt.Printf("  발사된 요청:         %s\n", formatNumber(int(atomic.LoadInt64(&stats.sent))))
	fmt.Printf("  완료된 요청:         %s\n", formatNumber(int(atomic.LoadInt64(&stats.completed))))
	fmt.Printf("\n")
	fmt.Printf("처리량:\n")
	fmt.Printf("  평균 RPS:            %s req/s\n", formatNumber(int(float64(atomic.LoadInt64(&stats.completed))/totalDuration.Seconds())))
	fmt.Printf("  최대 RPS:            %s req/s (이론상)\n", formatNumber(int(float64(total)/totalDuration.Seconds())))
	fmt.Printf("\n")

	success := atomic.LoadInt64(&stats.success)
	fail := atomic.LoadInt64(&stats.fail)
	totalCompleted := success + fail

	fmt.Printf("결과:\n")
	fmt.Printf("  성공:                %s (%.2f%%)\n", formatNumber(int(success)), float64(success)/float64(totalCompleted)*100)
	fmt.Printf("  실패:                %s (%.2f%%)\n", formatNumber(int(fail)), float64(fail)/float64(totalCompleted)*100)
	fmt.Printf("\n")
	fmt.Printf("상태 코드:\n")
	fmt.Printf("  2xx:                 %s\n", formatNumber(int(atomic.LoadInt64(&stats.status2xx))))
	fmt.Printf("  4xx:                 %s\n", formatNumber(int(atomic.LoadInt64(&stats.status4xx))))
	fmt.Printf("  5xx:                 %s\n", formatNumber(int(atomic.LoadInt64(&stats.status5xx))))
	fmt.Printf("  기타:                %s\n", formatNumber(int(atomic.LoadInt64(&stats.other))))
	fmt.Printf("\n")

	if success > 0 {
		fmt.Printf("응답 시간 (Latency):\n")
		fmt.Printf("  최소:                %d ms\n", atomic.LoadInt64(&stats.minLatency))
		fmt.Printf("  최대:                %d ms\n", atomic.LoadInt64(&stats.maxLatency))
		fmt.Printf("  평균:                %d ms\n", atomic.LoadInt64(&stats.sumLatency)/success)
	}
	fmt.Printf("%s\n", separator)

	fmt.Printf("\n 성능 요약:\n")
	fmt.Printf("   - %s 요청 처리 완료: %v\n", formatNumber(total), totalDuration)
	fmt.Printf("   - 초당 처리량: %s requests/sec\n", formatNumber(int(float64(atomic.LoadInt64(&stats.completed))/totalDuration.Seconds())))
	fmt.Printf("   - 동시 고루틴: %s개, HTTP 클라이언트: %d개\n", formatNumber(total), *numClients)
	fmt.Printf("   - 총 연결 풀: %s (클라이언트당 %s)\n", formatNumber(*maxConns**numClients), formatNumber(*maxConns))
	if success > 0 {
		fmt.Printf("   - 평균 응답시간: %dms\n", atomic.LoadInt64(&stats.sumLatency)/success)
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
		fmt.Printf("✓ CPU 프로파일링 활성화: %s\n", *cpuprofile)
	}

	if *blockprofile != "" {
		runtime.SetBlockProfileRate(1)
		fmt.Printf("✓ 블로킹 프로파일링 활성화: %s\n", *blockprofile)
	}

	if *mutexprofile != "" {
		runtime.SetMutexProfileFraction(1)
		fmt.Printf("✓ 뮤텍스 프로파일링 활성화: %s\n", *mutexprofile)
	}

	method := strings.ToUpper(*httpMethod)
	if method != "GET" && method != "POST" {
		log.Fatalf("지원되지 않은 메서드 사용: %s GET, POST만 가능\n", *httpMethod)
	}

	runtime.GOMAXPROCS(runtime.NumCPU())

	printConfig(method)

	fmt.Printf("\n %d개의 HTTP 클라이언트 생성 중...\n", *numClients)
	clients := make([]*http.Client, *numClients)
	for i := 0; i < *numClients; i++ {
		clients[i] = createOptimizedHTTP2Client(*maxConns, *timeout)
	}
	fmt.Printf("✓ 클라이언트 생성 완료 (총 연결 풀: %s)\n", formatNumber(*maxConns**numClients))

	if *enableWarmup {
		warmupConnections(clients, *baseURL, method, *warmupRequests)
	}

	stats := &Stats{minLatency: int64(^uint64(0) >> 1)}

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

	fmt.Printf("🚀 고루틴 %s개 생성 및 요청 시작\n\n", formatNumber(*totalRequests))
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
		atomic.AddInt64(&stats.sent, 1)
	}

	generationDuration := time.Since(generationStart)
	fmt.Printf("✓ 모든 고루틴 발사 완료 (소요시간: %v)\n", generationDuration)
	fmt.Println("\n⏳ 모든 고루틴의 처리 완료 대기 중....")

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
		fmt.Printf("\n✓ 메모리 프로파일 저장 완료: %s\n", *memprofile)
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
		fmt.Printf("✓ 블로킹 프로파일 저장 완료: %s\n", *blockprofile)
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
		fmt.Printf("✓ 뮤텍스 프로파일 저장 완료: %s\n", *mutexprofile)
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
		fmt.Printf("✓ 고루틴 프로파일 저장 완료: %s\n", *goroutineprofile)
	}
}
