package com.seatwise.queue;

import java.util.List;

public record ProduceRequest(Long memberId, List<Long> showSeatIds, Long sectionId) {}
