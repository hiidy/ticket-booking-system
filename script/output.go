package main

import (
	"fmt"
	"runtime"
	"strings"
	"time"
)

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
