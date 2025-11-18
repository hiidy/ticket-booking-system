package main

// 로그 스케일 버킷 계산 함수
func getLogBucket(latencyMs int64) int {
	if latencyMs <= 0 {
		return 0
	}

	// 0-10ms: 1ms 단위 (10개 버킷)
	if latencyMs < 10 {
		return int(latencyMs)
	}

	// 10-100ms: 1ms 단위 (90개 버킷)
	if latencyMs < 100 {
		return 10 + int(latencyMs-10)
	}

	// 100-1000ms: 10ms 단위 (90개 버킷)
	if latencyMs < 1000 {
		return 100 + int((latencyMs-100)/10)
	}

	// 1000-2000ms: 100ms 단위 (10개 버킷)
	if latencyMs < 2000 {
		return 190 + int((latencyMs-1000)/100)
	}

	// 2000-3000ms: 100ms 단위 (10개 버킷)
	if latencyMs < 3000 {
		return 200 + int((latencyMs-2000)/100)
	}

	// 3000-4000ms: 100ms 단위 (10개 버킷)
	if latencyMs < 4000 {
		return 210 + int((latencyMs-3000)/100)
	}

	// 4000-5000ms: 100ms 단위 (10개 버킷)
	if latencyMs < 5000 {
		return 220 + int((latencyMs-4000)/100)
	}

	// 5000ms+
	return 230
}

// 버킷별 레이턴시 추정값 계산
func estimateLatency(bucket int) int64 {
	if bucket < 10 {
		// 0-9ms
		return int64(bucket)
	} else if bucket < 100 {
		// 10-99ms
		return int64(bucket)
	} else if bucket < 190 {
		// 100-999ms (10ms 단위)
		return int64(100 + (bucket-100)*10 + 5) // 중간값
	} else if bucket < 200 {
		// 1000-1999ms (100ms 단위)
		return int64(1000 + (bucket-190)*100 + 50) // 중간값
	} else if bucket < 210 {
		// 2000-2999ms (100ms 단위)
		return int64(2000 + (bucket-200)*100 + 50)
	} else if bucket < 220 {
		// 3000-3999ms (100ms 단위)
		return int64(3000 + (bucket-210)*100 + 50)
	} else if bucket < 230 {
		// 4000-4999ms (100ms 단위)
		return int64(4000 + (bucket-220)*100 + 50)
	} else {
		// 5000ms+
		return 5500 // 보수적 추정
	}
}
