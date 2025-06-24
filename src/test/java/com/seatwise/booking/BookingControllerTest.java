package com.seatwise.booking;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seatwise.booking.dto.request.BookingRequest;
import com.seatwise.booking.dto.response.BookingResponse;
import com.seatwise.booking.messaging.BookingMessageProducer;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.request.async.DeferredResult;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

  private static final UUID IDEMPOTENCY_KEY = UUID.randomUUID();

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private BookingResponseManager waitService;
  @MockBean private BookingMessageProducer bookingMessageProducer;
  @MockBean private BookingService bookingService;

  @Test
  void shouldReturn200Ok_whenCreateBookingWithValidIdempotencyKey() throws Exception {
    // given
    BookingRequest bookingRequest = new BookingRequest(1L, List.of(1001L), 200L);
    BookingResponse result = BookingResponse.success(999L, IDEMPOTENCY_KEY);
    DeferredResult<BookingResponse> deferredResult = new DeferredResult<>();
    deferredResult.setResult(result);

    given(waitService.createPendingResponse(IDEMPOTENCY_KEY)).willReturn(deferredResult);

    // when & then
    mockMvc
        .perform(
            post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", IDEMPOTENCY_KEY)
                .content(objectMapper.writeValueAsString(bookingRequest)))
        .andExpect(status().isOk());

    verify(waitService).createPendingResponse(IDEMPOTENCY_KEY);
  }
}
