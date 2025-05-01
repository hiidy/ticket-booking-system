package com.seatwise.booking.domain;

import com.seatwise.common.domain.BaseEntity;
import com.seatwise.member.domain.Member;
import com.seatwise.show.domain.ShowSeat;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "booking")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String requestId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @OneToMany(mappedBy = "booking")
  private List<ShowSeat> showSeats = new ArrayList<>();

  private int totalAmount;

  public Booking(String requestId, Member member, int totalAmount) {
    this.requestId = requestId;
    this.member = member;
    this.totalAmount = totalAmount;
  }
}
