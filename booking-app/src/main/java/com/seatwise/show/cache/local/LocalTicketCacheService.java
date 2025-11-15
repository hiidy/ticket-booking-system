package com.seatwise.show.cache.local;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.seatwise.show.cache.TicketCacheService;
import com.seatwise.show.cache.config.CacheProperties;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
public class LocalTicketCacheService implements TicketCacheService {

  private final Cache<Long, Long> ticketCache;

  public LocalTicketCacheService(CacheProperties cacheProperties) {
    CacheProperties.LocalCacheProperties properties = cacheProperties.local();

    Caffeine<Object, Object> builder = Caffeine.newBuilder().maximumSize(properties.maxSize());

    // TTL 설정 (쓰기 후 만료)
    if (properties.ttl() != null) {
      builder.expireAfterWrite(properties.ttl());
    }

    if (properties.recordStats()) {
      builder.recordStats();
    }

    this.ticketCache = builder.build();

    log.info(
        "LocalTicketCacheService initialized with maxSize={}, ttl={}",
        properties.maxSize(),
        properties.ttl());
  }

  @Override
  public void holdTickets(List<Long> ticketIds, Long memberId) {
    if (ticketIds == null || ticketIds.isEmpty()) {
      return;
    }

    Map<Long, Long> entries =
        ticketIds.stream().collect(Collectors.toMap(ticketId -> ticketId, ticketId -> memberId));

    ticketCache.putAll(entries);

    log.debug("Held {} tickets for member {} in local cache", ticketIds.size(), memberId);
  }

  @Override
  public void holdTicket(Long ticketId, Long memberId) {
    if (ticketId == null || memberId == null) {
      return;
    }

    ticketCache.put(ticketId, memberId);
    log.debug("Held ticket {} for member {} in local cache", ticketId, memberId);
  }

  @Override
  public Optional<Long> getHoldMember(Long ticketId) {
    if (ticketId == null) {
      return Optional.empty();
    }

    Long memberId = ticketCache.getIfPresent(ticketId);
    return Optional.ofNullable(memberId);
  }

  @Override
  public Map<Long, Long> getHoldMembers(List<Long> ticketIds) {
    if (ticketIds == null || ticketIds.isEmpty()) {
      return Collections.emptyMap();
    }

    return ticketCache.getAllPresent(ticketIds);
  }

  @Override
  public boolean hasUnavailableTickets(List<Long> ticketIds, Long memberId) {
    if (ticketIds == null || ticketIds.isEmpty()) {
      return false;
    }

    Map<Long, Long> holdMembers = getHoldMembers(ticketIds);

    // 다른 멤버가 홀드한 티켓이 있는지 확인
    boolean hasUnavailable =
        holdMembers.values().stream().anyMatch(holdMemberId -> !holdMemberId.equals(memberId));

    log.debug(
        "Checked availability for {} tickets, unavailable: {}", ticketIds.size(), hasUnavailable);

    return hasUnavailable;
  }

  @Override
  public void releaseTickets(List<Long> ticketIds) {
    if (ticketIds == null || ticketIds.isEmpty()) {
      return;
    }

    ticketCache.invalidateAll(ticketIds);
    log.debug("Released {} tickets from local cache", ticketIds.size());
  }

  @Override
  public void releaseTicket(Long ticketId) {
    if (ticketId == null) {
      return;
    }

    ticketCache.invalidate(ticketId);
    log.debug("Released ticket {} from local cache", ticketId);
  }

  @Override
  public void invalidate(List<Long> ticketIds) {
    releaseTickets(ticketIds);
  }

  @Override
  public void invalidateAll() {
    ticketCache.invalidateAll();
    log.info("Invalidated all entries in local cache");
  }

  public CacheStats getStats() {
    return ticketCache.stats();
  }

  public long size() {
    return ticketCache.estimatedSize();
  }

  public void cleanUp() {
    ticketCache.cleanUp();
    log.debug("Cleaned up local cache");
  }
}
