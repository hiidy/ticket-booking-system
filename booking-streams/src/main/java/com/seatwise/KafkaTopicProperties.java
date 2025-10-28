package com.seatwise;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.topics")
public record KafkaTopicProperties(
    @NotBlank(message = "Booking request topic name is required") String bookingRequest,
    @Min(value = 1, message = "Partitions must be at least 1") int partitions,
    @Min(value = 1, message = "Replicas must be at least 1") short replicas) {}
