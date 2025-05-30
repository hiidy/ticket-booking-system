package com.seatwise;

import com.seatwise.queue.QueueProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(QueueProperties.class)
public class TicketBookingSystemApplication {

  public static void main(String[] args) {
    SpringApplication.run(TicketBookingSystemApplication.class, args);
  }
}
