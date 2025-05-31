package com.seatwise.queue.entity;

import lombok.Getter;

@Getter
public enum MessageStatus {
  PENDING,
  COMPLETED,
  FAILED
}
