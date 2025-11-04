package main

import (
	"math/rand"
)

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

func pickSection(r *rand.Rand) int {
	v := r.Float64()
	for i, cw := range CumulativeWeights {
		if v <= cw {
			return SubSections[i].SectionID
		}
	}
	return SubSections[len(SubSections)-1].SectionID
}
