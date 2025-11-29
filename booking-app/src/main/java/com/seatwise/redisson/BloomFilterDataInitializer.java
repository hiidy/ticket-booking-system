package com.seatwise.redisson;

import com.seatwise.show.entity.Section;
import com.seatwise.show.entity.Show;
import com.seatwise.show.entity.Ticket;
import com.seatwise.show.repository.SectionRepository;
import com.seatwise.show.repository.ShowRepository;
import com.seatwise.show.repository.TicketRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BloomFilterDataInitializer {

  private final Map<String, BloomFilterHandler> bloomFilters;
  private final TicketRepository ticketRepository;
  private final ShowRepository showRepository;
  private final SectionRepository sectionRepository;

  private BloomFilterHandler getFilter(String name) {
    BloomFilterHandler filter = bloomFilters.get(name);
    if (filter == null) {
      log.warn("'{}' 블룸 필터가 설정되지 않았습니다. 초기화를 건너뜁니다", name);
    }
    return filter;
  }

  @PostConstruct
  public void initialize() {
    log.info("========== 블룸 필터 데이터 초기화 시작 ==========");

    try {
      loadTicketData();
      loadShowData();
      loadSectionData();
      loadShowSectionData();

      log.info("========== 블룸 필터 데이터 초기화 완료 ==========");
    } catch (Exception e) {
      log.error("블룸 필터 데이터 초기화 실패", e);
    }
  }

  private void loadTicketData() {
    log.info("티켓 데이터를 블룸 필터에 로딩");
    BloomFilterHandler filter = getFilter("ticket");
    if (filter == null) {
      return;
    }

    try {
      List<Ticket> tickets = ticketRepository.findAll();
      if (tickets.isEmpty()) {
        log.info("블룸 필터에 로딩할 티켓을 찾지 못함");
        return;
      }

      int added = 0;
      for (Ticket ticket : tickets) {
        Long id = ticket.getId();
        if (id != null && filter.add(String.valueOf(id))) {
          added++;
        }
      }

      log.info("{}개의 티켓을 블룸 필터에 로딩했습니다", added);
    } catch (Exception e) {
      log.error("티켓 데이터 로딩 실패", e);
      throw e;
    }
  }

  private void loadShowData() {
    log.info("공연 데이터를 블룸 필터에 로딩");
    BloomFilterHandler filter = getFilter("show");
    if (filter == null) {
      log.warn("공연 블룸 필터가 설정되지 않았습니다. 공연 데이터 로딩을 건너뜁니다");
      return;
    }

    try {
      List<Show> shows = showRepository.findAll();
      if (shows.isEmpty()) {
        log.info("블룸 필터에 로딩할 공연을 찾지 못함");
        return;
      }

      int added = 0;
      for (Show show : shows) {
        Long id = show.getId();
        if (id != null && filter.add(String.valueOf(id))) {
          added++;
        }
      }

      log.info("{}개의 공연을 블룸 필터에 로딩했습니다", added);
    } catch (Exception e) {
      log.error("공연 데이터 로딩 실패", e);
      throw e;
    }
  }

  private void loadSectionData() {
    log.info("섹션 데이터를 블룸 필터에 로딩");
    BloomFilterHandler filter = getFilter("section");
    if (filter == null) {
      log.warn("섹션 블룸 필터가 설정되지 않았습니다. 섹션 데이터 로딩을 건너뜁니다");
      return;
    }

    try {
      List<Section> sections = sectionRepository.findAll();
      if (sections.isEmpty()) {
        log.info("블룸 필터에 로딩할 섹션을 찾지 못함");
        return;
      }

      int added = 0;
      for (Section section : sections) {
        Long id = section.getId();
        if (id != null && filter.add(String.valueOf(id))) {
          added++;
        }
      }

      log.info("{}개의 섹션을 블룸 필터에 로딩했습니다", added);
    } catch (Exception e) {
      log.error("섹션 데이터 로딩 실패", e);
      throw e;
    }
  }

  private void loadShowSectionData() {
    log.info("공연-섹션 조합 데이터를 블룸 필터에 로딩");
    BloomFilterHandler filter = getFilter("show_section");
    if (filter == null) {
      log.warn("공연-섹션 블룸 필터가 설정되지 않았습니다. 공연-섹션 데이터 로딩을 건너뜁니다");
      return;
    }

    try {
      List<Ticket> tickets = ticketRepository.findAll();
      if (tickets.isEmpty()) {
        log.info("블룸 필터에 로딩할 티켓을 찾지 못함");
        return;
      }

      int added = 0;
      for (Ticket ticket : tickets) {
        if (ticket.getShow() != null && ticket.getSectionId() != null) {
          Long showId = ticket.getShow().getId();
          Long sectionId = ticket.getSectionId();
          if (showId != null && sectionId != null) {
            String showSectionKey = showId + ":" + sectionId;
            if (filter.add(showSectionKey)) {
              added++;
            }
          }
        }
      }

      log.info("{}개의 공연-섹션 조합을 블룸 필터에 로딩했습니다", added);
    } catch (Exception e) {
      log.error("공연-섹션 데이터 로딩 실패", e);
      throw e;
    }
  }
}
