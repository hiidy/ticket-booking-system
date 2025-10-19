package main

import (
	"bytes"
	"context"
	"crypto/tls"
	"encoding/json"
	"flag"
	"fmt"
	"log"
	"math/rand"
	"net"
	"net/http"
	"runtime"
	"strings"
	"sync"
	"sync/atomic"
	"time"

	"github.com/google/uuid"
	"golang.org/x/net/http2"
)

var (
	// CLI 플래그
	baseURL          = flag.String("url", "http://localhost:8080/healthz", "타겟 URL")
	httpMethod       = flag.String("method", "POST", "HTTP 메서드 (GET, POST)")
	totalRequests    = flag.Int("requests", 100, "총 요청 수")
	numWorkers       = flag.Int("workers", 5000, "워커 수 (동시성)")
	maxConns         = flag.Int("conns", 2000, "호스트당 최대 연결 수")
	timeout          = flag.Duration("timeout", 20*time.Second, "요청 타임아웃")
	workQueueSize    = flag.Int("queue", 10000, "작업 큐 버퍼 크기")
	maxMemberID      = flag.Int("members", 9000, "최대 멤버 ID")
	showProgress     = flag.Bool("progress", true, "실시간 진행률 표시")
	progressInterval = flag.Duration("interval", 1*time.Second, "진행률 출력 간격")
)

var (
	SECTION_WEIGHTS = []float64{0.35, 0.25, 0.15, 0.10, 0.08, 0.07}
	SECTION_RANGES  = [][2]int{
		{1, 75}, {76, 150}, {151, 225},
		{226, 300}, {301, 375}, {376, 450},
	}
	CUMULATIVE_WEIGHTS []float64
)

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

type WorkItem struct {
	id      int
	payload []byte
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

func pickSection() int {
	r := rand.Float64()
	for i, cw := range CUMULATIVE_WEIGHTS {
		if r <= cw {
			return i + 1
		}
	}
	return 6
}

func createPayload(maxMember int) ([]byte, error) {
	sectionID := pickSection()
	sectionRange := SECTION_RANGES[sectionID-1]

	seatCount := rand.Intn(4) + 1
	seatIDs := make([]int, seatCount)
	for i := 0; i < seatCount; i++ {
		seatIDs[i] = rand.Intn(sectionRange[1]-sectionRange[0]+1) + sectionRange[0]
	}

	req := BookingRequest{
		MemberID:  rand.Intn(maxMember) + 1,
		TicketIDs: seatIDs,
		SectionID: sectionID,
	}

	return json.Marshal(req)
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

func worker(id int, client *http.Client, workChan <-chan WorkItem, stats *Stats, wg *sync.WaitGroup, url string, method string) {
	defer wg.Done()

	httpMethod := strings.ToUpper(method)

	for item := range workChan {
		startTime := time.Now()

		var req *http.Request
		var err error

		if httpMethod == "GET" {
			req, err = http.NewRequest("GET", url, nil)
		} else {
			req, err = http.NewRequest("POST", url, bytes.NewBuffer(item.payload))
			if err == nil {
				req.Header.Set("Content-Type", "application/json")
				req.Header.Set("Idempotency-Key", uuid.New().String())
			}
		}
		if err != nil {
			atomic.AddInt64(&stats.fail, 1)
			atomic.AddInt64(&stats.completed, 1)
			continue
		}

		ctx, cancel := context.WithTimeout(context.Background(), *timeout)
		req = req.WithContext(ctx)

		resp, err := client.Do(req)
		cancel()

		latency := time.Since(startTime).Milliseconds()
		atomic.AddInt64(&stats.completed, 1)

		if err != nil {
			atomic.AddInt64(&stats.fail, 1)
			continue
		}

		resp.Body.Close()

		atomic.AddInt64(&stats.success, 1)
		atomic.AddInt64(&stats.sumLatency, latency)

		// min/max 레이턴시 업데이트
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
	fmt.Printf(" HTTP/2 부하 테스트 시작\n")
	fmt.Printf("%s\n", separator)
	fmt.Printf("설정:\n")
	fmt.Printf("  HTTP 메서드:         %s\n", method)
	fmt.Printf("  타겟 URL:            %s\n", *baseURL)
	fmt.Printf("  총 요청 수:          %s\n", formatNumber(*totalRequests))
	fmt.Printf("  워커 수:             %s\n", formatNumber(*numWorkers))
	fmt.Printf("  최대 연결 수:        %s (per host)\n", formatNumber(*maxConns))
	fmt.Printf("  작업 큐 크기:        %s\n", formatNumber(*workQueueSize))
	fmt.Printf("  요청 타임아웃:       %v\n", *timeout)
	if method == "POST" {
		fmt.Printf("  최대 멤버 ID:        %s\n", formatNumber(*maxMemberID))
	}
	fmt.Printf("  CPU 코어 수:         %d\n", runtime.NumCPU())
	fmt.Printf("%s\n\n", separator)
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

	// 성능 요약
	fmt.Printf("\n 성능 요약:\n")
	fmt.Printf("   - %s 요청 처리 완료: %v\n", formatNumber(total), totalDuration)
	fmt.Printf("   - 초당 처리량: %s requests/sec\n", formatNumber(int(float64(atomic.LoadInt64(&stats.completed))/totalDuration.Seconds())))
	fmt.Printf("   - HTTP/2 멀티플렉싱: %s workers, %s max conns\n", formatNumber(*numWorkers), formatNumber(*maxConns))
	if success > 0 {
		fmt.Printf("   - 평균 응답시간: %dms\n", atomic.LoadInt64(&stats.sumLatency)/success)
	}
	fmt.Println()
}

func main() {
	flag.Parse()

	method := strings.ToUpper(*httpMethod)
	if method != "GET" && method != "POST" {
		log.Fatalf("지원되지 않은 메서드 사용: %s GET, POST만 가능\n", *httpMethod)
	}

	runtime.GOMAXPROCS(runtime.NumCPU())

	printConfig(method)

	client := createOptimizedHTTP2Client(*maxConns, *timeout)
	stats := &Stats{minLatency: int64(^uint64(0) >> 1)}

	workChan := make(chan WorkItem, *workQueueSize)
	var workerWg sync.WaitGroup

	fmt.Println("워커 풀 초기화 시작")
	for i := 0; i < *numWorkers; i++ {
		workerWg.Add(1)
		go worker(i, client, workChan, stats, &workerWg, *baseURL, method)
	}
	fmt.Printf("%s개 워커 준비 완료\n\n", formatNumber(*numWorkers))

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

	fmt.Println("요청 생성")
	startTime := time.Now()
	generationStart := time.Now()

	for i := 0; i < *totalRequests; i++ {
		var payload []byte
		var err error

		// POST 요청인 경우에만 페이로드 생성
		if method == "POST" {
			payload, err = createPayload(*maxMemberID)
			if err != nil {
				fmt.Printf("Payload 생성 실패 #%d: %v\n", i, err)
				continue
			}
		}

		workChan <- WorkItem{
			id:      i,
			payload: payload,
		}

		atomic.AddInt64(&stats.sent, 1)
	}

	generationDuration := time.Since(generationStart)
	fmt.Printf("\n모든 요청 발사 완료 (소요시간: %v)\n", generationDuration)
	fmt.Println("\n워커들의 처리 완료 대기 중....")

	close(workChan)
	workerWg.Wait()

	totalDuration := time.Since(startTime)

	if *showProgress {
		close(stopProgress)
		progressWg.Wait()
	}

	printFinalResults(stats, totalDuration, generationDuration, *totalRequests)
}
