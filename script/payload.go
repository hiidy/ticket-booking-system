package main

import (
	"encoding/json"
	"math/rand"
	"sync"
	"time"
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
