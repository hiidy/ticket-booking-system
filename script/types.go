package main

type BookingRequest struct {
	MemberID  int   `json:"memberId"`
	TicketIDs []int `json:"ticketIds"`
	SectionID int   `json:"sectionId"`
}

type BookingResponse struct {
	BookingID string `json:"bookingId"`
}

type BookingStatusResponse struct {
	Success   bool   `json:"success"`
	BookingID int64  `json:"bookingId"`
	RequestID string `json:"requestId"`
}

type SubSection struct {
	SectionID int    // 1..86
	Group     string // "G1","G2","G3","P","R","S","A"
	Index     int
	SeatStart int     // 이 세부 섹션의 좌석 시작 ID
	SeatEnd   int     // 이 세부 섹션의 좌석 끝 ID
	Weight    float64 // 전체에서 이 세부 섹션이 선택될 확률
}
