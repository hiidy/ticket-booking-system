package com.seatwise.show.service;

import com.seatwise.core.BaseCode;
import com.seatwise.core.exception.BusinessException;
import com.seatwise.redis.RedisCache;
import com.seatwise.redis.RedisKeyBuilder;
import com.seatwise.redis.RedisKeys;
import com.seatwise.show.dto.TicketAvailability;
import com.seatwise.show.dto.request.TicketCreateRequest;
import com.seatwise.show.dto.response.SeatAvailabilityResponse;
import com.seatwise.show.dto.response.TicketResponse;
import com.seatwise.show.entity.Show;
import com.seatwise.show.entity.Ticket;
import com.seatwise.show.repository.ShowRepository;
import com.seatwise.show.repository.TicketRepository;
import com.seatwise.venue.entity.SeatRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketService {

  private final TicketRepository ticketRepository;
  private final ShowRepository showRepository;
  private final SeatRepository seatRepository;
  private final TicketCacheData ticketCacheData;
  private final RedissonClient redissonClient;
  private final RedisCache redisCache;

  public List<Long> createTickets(Long showId, TicketCreateRequest request) {
    Show show =
        showRepository
            .findById(showId)
            .orElseThrow(() -> new BusinessException(BaseCode.SHOW_NOT_FOUND));

    List<Ticket> tickets =
        request.ticketPrices().stream()
            .map(
                ticketPrice ->
                    seatRepository
                        .findByIdBetween(ticketPrice.startSeatId(), ticketPrice.endSeatId())
                        .stream()
                        .map(seat -> Ticket.createAvailable(show, seat, null, ticketPrice.price()))
                        .toList())
            .flatMap(Collection::stream)
            .toList();

    List<Ticket> savedTickets = ticketRepository.saveAll(tickets);
    return savedTickets.stream().map(Ticket::getId).toList();
  }

  public List<TicketAvailability> getTicketAvailabilityBySection(Long showId, Long sectionId) {
    // 캐시에서 ticket availability 조회
    List<TicketAvailability> ticketCache = getTicketAvailabilityByCache(showId, sectionId);
    if (!ticketCache.isEmpty()) {
      return ticketCache;
    }

    // 분산락으로 캐시 스탬피드 방지
    String lockKey = String.format("ticket_lock:%d:%d", showId, sectionId);
    RLock lock = redissonClient.getLock(lockKey);

    try {
      if (lock.tryLock(1, TimeUnit.SECONDS)) {
        // 락 획득 후 다시 캐시 확인 (Double-Checked Locking)
        ticketCache = getTicketAvailabilityByCache(showId, sectionId);
        if (!ticketCache.isEmpty()) {
          return ticketCache;
        }
        List<Ticket> tickets = ticketRepository.findTicketsByShowIdAndSectionId(showId, sectionId);
        List<TicketAvailability> ticketAvailabilities =
            tickets.stream()
                .map(
                    ticket ->
                        new TicketAvailability(
                            ticket.getId(),
                            showId,
                            sectionId,
                            ticket.getSeat().getRowName(),
                            ticket.getSeat().getColName(),
                            ticket.getStatus()))
                .toList();

        cacheTicketAvailabilities(showId, sectionId, ticketAvailabilities);

        return ticketAvailabilities;
      } else {
        return getTicketAvailabilityByCache(showId, sectionId);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return getTicketAvailabilityByCache(showId, sectionId);
    } finally {
      if (lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }

  public List<TicketAvailability> getTicketAvailabilityByCache(Long showId, Long sectionId) {
    String availableKey =
        RedisKeyBuilder.createRedisKey(RedisKeys.TICKET_AVAILABLE, showId, sectionId);
    String bookedKey = RedisKeyBuilder.createRedisKey(RedisKeys.TICKET_BOOKED, showId, sectionId);
    String lockedKey = RedisKeyBuilder.createRedisKey(RedisKeys.TICKET_LOCKED, showId, sectionId);

    List<TicketAvailability> result = new ArrayList<>();

    result.addAll(ticketCacheData.getData(availableKey));
    result.addAll(ticketCacheData.getData(bookedKey));
    result.addAll(ticketCacheData.getData(lockedKey));

    return result;
  }

  public List<TicketResponse> getTickets(Long showId) {
    List<Ticket> tickets = ticketRepository.findAllByShowId(showId);
    LocalDateTime requestTime = LocalDateTime.now();
    if (tickets.isEmpty()) {
      throw new BusinessException(BaseCode.TICKET_NOT_FOUND);
    }
    return tickets.stream().map(showSeat -> TicketResponse.from(showSeat, requestTime)).toList();
  }

  public List<SeatAvailabilityResponse> getTicketAvailabilityByGrade(Long showId) {
    return ticketRepository.findTicketAvailabilityByShowId(showId);
  }

  private void cacheTicketAvailabilities(
      Long showId, Long sectionId, List<TicketAvailability> ticketAvailabilities) {
    for (TicketAvailability ticket : ticketAvailabilities) {
      String cacheKey;

      switch (ticket.status()) {
        case AVAILABLE:
          cacheKey = RedisKeyBuilder.createRedisKey(RedisKeys.TICKET_AVAILABLE, showId, sectionId);
          break;
        case BOOKED:
          cacheKey = RedisKeyBuilder.createRedisKey(RedisKeys.TICKET_BOOKED, showId, sectionId);
          break;
        case PAYMENT_PENDING:
          cacheKey = RedisKeyBuilder.createRedisKey(RedisKeys.TICKET_LOCKED, showId, sectionId);
          break;
        default:
          continue;
      }

      String field = String.valueOf(ticket.ticketId());
      redisCache.putHash(cacheKey, field, ticket, 30, TimeUnit.MINUTES);
    }
  }
}
