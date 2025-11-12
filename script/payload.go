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

		ticketCount := r.Intn(4) + 1
		span := s.SeatEnd - s.SeatStart + 1

		ticketIDs := make([]int, 0, ticketCount)

		if ticketCount >= span {
			for i := 0; i < span; i++ {
				ticketIDs = append(ticketIDs, s.SeatStart+i)
			}
		} else {
			used := make(map[int]bool, ticketCount)
			for len(ticketIDs) < ticketCount {
				ticketID := s.SeatStart + r.Intn(span)
				if !used[ticketID] {
					ticketIDs = append(ticketIDs, ticketID)
					used[ticketID] = true
				}
			}
		}

		req := BookingRequest{
			MemberID:  r.Intn(maxMember) + 1,
			TicketIDs: ticketIDs,
			SectionID: sectionID,
		}
		payload, err = json.Marshal(req)
	})

	return payload, err
}
