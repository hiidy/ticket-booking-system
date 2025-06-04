package com.seatwise.booking.config;

import com.seatwise.booking.messaging.MessagingProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MessagingProperties.class)
public class BookingMessagingConfig {}
