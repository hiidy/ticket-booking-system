package com.seatwise;

import com.seatwise.booking.messaging.MessagingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MessagingProperties.class)
public class TicketBookingSystemApplication {

  public static void main(String[] args) {
    SpringApplication.run(TicketBookingSystemApplication.class, args);
  }
}
