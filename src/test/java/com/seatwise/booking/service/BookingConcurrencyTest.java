package com.seatwise.booking.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.seatwise.booking.domain.Booking;
import com.seatwise.booking.repository.BookingRepository;
import com.seatwise.show.domain.ShowSeat;
import com.seatwise.show.repository.ShowSeatRepository;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test-mysql")
@Sql(scripts = "/sql/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Disabled
class BookingConcurrencyTest {

  private static final Logger log = LoggerFactory.getLogger(BookingConcurrencyTest.class);

  @Autowired BookingService bookingService;
  @Autowired ShowSeatRepository showSeatRepository;
  @Autowired BookingRepository bookingRepository;

  @RepeatedTest(1)
  void lostUpdateWhenAssignBooking() throws InterruptedException {
    log.info("=== 테스트 시작 ===");
    long startTime = System.currentTimeMillis();

    int numberOfThreads = 1000;
    ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch endLatch = new CountDownLatch(numberOfThreads);

    for (int i = 0; i < numberOfThreads; i++) {
      int threadNum = i + 1;
      List<Long> seatIds =
          switch (i) {
            case 0 -> List.of(1L, 2L, 3L);
            case 1 -> List.of(2L, 3L, 4L);
            case 2 -> List.of(3L, 4L, 5L);
            case 3 -> List.of(4L, 5L, 6L);
            case 4 -> List.of(5L, 6L, 7L);
            default -> List.of(1L);
          };

      executorService.submit(
          () -> {
            try {
              startLatch.await();
              bookingService.createBooking((long) threadNum, seatIds);
            } catch (Exception e) {
              log.error("Thread {} 예약 실패 - 좌석 {}", threadNum, seatIds, e);
            } finally {
              endLatch.countDown();
            }
          });
    }

    startLatch.countDown();
    endLatch.await(10, TimeUnit.SECONDS);
    executorService.shutdown();

    long endTime = System.currentTimeMillis();
    log.info("총 소요 시간: {}ms", endTime - startTime);

    // then
    List<ShowSeat> savedShowSeats = showSeatRepository.findAllByShowId(List.of(1L));
    List<Booking> bookings = bookingRepository.findAll();

    assertThat(bookings).hasSizeLessThanOrEqualTo(3);

    savedShowSeats.stream()
        .filter(seat -> seat.getBooking() != null)
        .forEach(
            seat -> {
              log.info("좌석 {} - 예약 ID: {}", seat.getSeat().getId(), seat.getBooking().getId());
            });
  }

  @RepeatedTest(1)
  void createBooking_WhenMultipleThreads_ShouldHandleConcurrency() throws InterruptedException {
    log.info("=== 동시성 테스트 시작 ===");
    long startTime = System.currentTimeMillis();

    int numberOfThreads = 10;
    ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch endLatch = new CountDownLatch(numberOfThreads);

    // 각 스레드가 동일한 좌석을 예약하도록 설정
    for (int i = 0; i < numberOfThreads; i++) {
      int threadNum = i + 1;
      executorService.submit(
          () -> {
            try {
              startLatch.await(); // 모든 스레드가 동시에 시작하도록 대기
              bookingService.createBooking(
                  (long) threadNum, // memberId
                  List.of(1L) // showId
                  );
              log.info("Thread {} 예약 성공", threadNum);
            } catch (Exception e) {
              log.error("Thread {} 예약 실패", threadNum, e);
            } finally {
              endLatch.countDown();
            }
          });
    }

    startLatch.countDown(); // 모든 스레드 시작
    endLatch.await(10, TimeUnit.SECONDS);
    executorService.shutdown();

    long endTime = System.currentTimeMillis();
    log.info("테스트 소요 시간: {}ms", endTime - startTime);

    // 검증
    List<ShowSeat> showSeats = showSeatRepository.findAllByShowId(List.of(1L));

    // 하나의 좌석은 한 번만 예약되어야 함
    assertThat(showSeats.stream().filter(seat -> seat.getBooking() != null).count())
        .isLessThanOrEqualTo(1);

    showSeats.stream()
        .filter(seat -> seat.getBooking() != null)
        .forEach(
            seat ->
                log.info(
                    "좌석 {} 최종 예약 상태: 예약자 ID {}",
                    seat.getSeat().getId(),
                    seat.getBooking().getMember().getId()));
  }
}
