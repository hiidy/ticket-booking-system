package com.seatwise.queue.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stream_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StreamMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String streamName;

  private String messageId;

  @Enumerated(value = EnumType.STRING)
  private MessageStatus status;

  public StreamMessage(String streamName, String messageId, MessageStatus status) {
    this.streamName = streamName;
    this.messageId = messageId;
    this.status = status;
  }

  public static StreamMessage failed(String streamName, String messageId) {
    return new StreamMessage(streamName, messageId, MessageStatus.FAILED);
  }
}
