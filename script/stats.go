package main

import (
	"runtime"
	"sync/atomic"
)

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
