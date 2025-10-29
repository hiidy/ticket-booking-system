package com.seatwise;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.topic")
public record KafkaTopicProperties(
    @Min(value = 1, message = "Partitions must be at least 1") int partitions,
    @Min(value = 1, message = "Replicas must be at least 1") short replicas,
    @NotBlank(message = "Booking request topic name is required") String bookingRequest,
    @NotBlank(message = "ticket state topic is required") String ticketState,
    @NotBlank(message = "booking command topic is required") String bookingCommand,
    @NotBlank(message = "booking result topic is required") String bookingResult,
    @NotBlank(message = "booking completed topic is required") String bookingCompleted,
    @NotBlank(message = "ticket init data topic is required") String ticketInit) {}
