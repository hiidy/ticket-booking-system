package com.seatwise.queue.domain;

import lombok.Getter;

@Getter
public enum MessageStatus {
  PENDING,
  COMPLETED,
  FAILED
}
