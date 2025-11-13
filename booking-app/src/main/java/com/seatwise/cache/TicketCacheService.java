package com.seatwise.cache;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TicketCacheService {

  void holdTickets(List<Long> ticketIds, Long memberId);

  void holdTicket(Long ticketId, Long memberId);

  Optional<Long> getHoldMember(Long ticketId);

  Map<Long, Long> getHoldMembers(List<Long> ticketIds);

  boolean hasUnavailableTickets(List<Long> ticketIds, Long memberId);

  void releaseTickets(List<Long> ticketIds);

  void releaseTicket(Long ticketId);

  void invalidate(List<Long> ticketIds);

  void invalidateAll();
}
