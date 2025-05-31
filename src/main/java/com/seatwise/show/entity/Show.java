package com.seatwise.show.entity;

import com.seatwise.common.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "`show`")
@NoArgsConstructor
@Getter
public class Show extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;

  private String description;

  @Enumerated(EnumType.STRING)
  private ShowType type;

  @Builder
  public Show(String title, String description, ShowType type) {
    this.title = title;
    this.description = description;
    this.type = type;
  }
}
