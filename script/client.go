package main

import (
	"bytes"
	"context"
	"crypto/tls"
	"fmt"
	"io"
	"log"
	"net"
	"net/http"
	"sync"
	"sync/atomic"
	"time"

	"github.com/google/uuid"
	"golang.org/x/net/http2"
)

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
