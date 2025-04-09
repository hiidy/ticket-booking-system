package com.seatwise.inventory.domain;

import static org.assertj.core.api.Assertions.*;

import com.seatwise.seat.domain.SeatGrade;
import org.junit.jupiter.api.Test;

class ShowInventoryPkTest {

  @Test
  void givenTwoSamePkInstance_whenCheckEquals_thenReturnTrue() {

    // given
    ShowInventoryPk pk1 = new ShowInventoryPk(1L, SeatGrade.S);
    // when & then

    assertThat(pk1.equals(pk1)).isTrue();
  }

  @Test
  void givenTwoPkWithSameValues_whenCheckEquals_thenReturnTrue() {
    // given
    ShowInventoryPk pk1 = new ShowInventoryPk(1L, SeatGrade.S);
    ShowInventoryPk pk2 = new ShowInventoryPk(1L, SeatGrade.S);

    // when & then
    assertThat(pk1.equals(pk2)).isTrue();
  }

  @Test
  void givenPkAndNull_whenCheckEquals_thenReturnFalse() {
    // given
    ShowInventoryPk pk1 = new ShowInventoryPk(1L, SeatGrade.S);

    // when & then
    assertThat(pk1.equals(null)).isFalse();
  }

  @Test
  void givenPkAndOtherClass_whenCheckEquals_thenReturnFalse() {
    // given
    ShowInventoryPk pk1 = new ShowInventoryPk(1L, SeatGrade.S);
    Object o = new Object();

    // when & then
    assertThat(pk1.equals(o)).isFalse();
  }

  @Test
  void givenTwoPkWithDifferentShowId_whenCheckEquals_thenReturnFalse() {
    // given
    ShowInventoryPk pk1 = new ShowInventoryPk(1L, SeatGrade.S);
    ShowInventoryPk pk2 = new ShowInventoryPk(2L, SeatGrade.S);

    // when & then
    assertThat(pk1.equals(pk2)).isFalse();
  }

  @Test
  void givenTwoPkWithDifferentGrade_whenCheckEquals_thenReturnFalse() {
    // given
    ShowInventoryPk pk1 = new ShowInventoryPk(1L, SeatGrade.S);
    ShowInventoryPk pk2 = new ShowInventoryPk(1L, SeatGrade.VIP);

    // when & then
    assertThat(pk1.equals(pk2)).isFalse();
  }
}
