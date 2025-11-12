package main

import (
	"flag"
	"fmt"
	"log"
	"net/http"
	"os"
	"runtime"
	"runtime/pprof"
	"strings"
	"sync"
	"sync/atomic"
	"time"
)

var (
	// 프로파일링 플래그
	cpuprofile       = flag.String("cpuprofile", "", "CPU 프로파일 출력 파일")
	memprofile       = flag.String("memprofile", "", "메모리 프로파일 출력 파일")
	blockprofile     = flag.String("blockprofile", "", "블로킹 프로파일 출력 파일")
	mutexprofile     = flag.String("mutexprofile", "", "뮤텍스 프로파일 출력 파일")
	goroutineprofile = flag.String("goroutineprofile", "", "고루틴 프로파일 출력 파일")

	// 테스트 설정 플래그
	baseURL       = flag.String("url", "http://localhost:8080/api/bookings/sync/redis-lock", "타겟 URL")
	lockType      = flag.String("lock", "redis", "락 타입: redis 또는 db")
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

	// 락 타입 검증 및 URL 자동 설정
	lockTypeValue := strings.ToLower(*lockType)
	if lockTypeValue != "redis" && lockTypeValue != "db" {
		log.Fatalf("지원되지 않은 락 타입: %s (redis 또는 db만 가능)\n", *lockType)
	}

	// URL이 기본값이면 락 타입에 맞게 자동 설정
	targetURL := *baseURL
	if strings.Contains(targetURL, "/api/bookings/sync") && !strings.Contains(targetURL, "-lock") {
		if lockTypeValue == "redis" {
			targetURL = strings.Replace(targetURL, "/sync", "/sync/redis-lock", 1)
		} else {
			targetURL = strings.Replace(targetURL, "/sync", "/sync/db-lock", 1)
		}
		fmt.Printf("🔧 락 타입에 따라 URL 자동 설정: %s\n", targetURL)
	}

	runtime.GOMAXPROCS(runtime.NumCPU())

	numShards := runtime.NumCPU()
	printConfig(method, numShards, targetURL, lockTypeValue)

	// HTTP 클라이언트 생성
	fmt.Printf("\n%d개의 HTTP 클라이언트 생성 중.....\n", *numClients)
	clients := make([]*http.Client, *numClients)
	for i := 0; i < *numClients; i++ {
		clients[i] = createOptimizedHTTP2Client(*maxConns, *timeout)
	}
	fmt.Printf("클라이언트 생성 완료 (총 연결 풀: %s)\n", formatNumber(*maxConns**numClients))

	// 워밍업
	if *enableWarmup {
		warmupConnections(clients, targetURL, method, *warmupRequests, numShards)
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

		go sendRequest(i, clients, payload, stats, &wg, targetURL, method)

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
