package com.seatwise.booking.entity;

import com.seatwise.core.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "booking")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JdbcTypeCode(SqlTypes.BINARY)
  @Column(name = "request_id", columnDefinition = "BINARY(16)")
  private UUID requestId;

  @Column(name = "member_id")
  private Long memberId;

  private int totalAmount;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private BookingStatus status;

  public Booking(UUID requestId, Long memberId, int totalAmount, BookingStatus status) {
    this.requestId = Objects.requireNonNull(requestId, "requestID는 필수");
    this.memberId = Objects.requireNonNull(memberId, "memberId는 필수");
    this.totalAmount = totalAmount;
    this.status = status != null ? status : BookingStatus.PENDING;
  }

  public static Booking createNew(UUID requestId, Long memberId, int totalAmount) {
    return new Booking(requestId, memberId, totalAmount, BookingStatus.PENDING);
  }

  public Booking markAsSuccess() {
    this.status = BookingStatus.SUCCESS;
    return this;
  }

  public Booking markAsFailed() {
    this.status = BookingStatus.FAILED;
    this.totalAmount = 0;
    return this;
  }

  public Booking markAsCancelled() {
    this.status = BookingStatus.CANCELLED;
    return this;
  }
}
