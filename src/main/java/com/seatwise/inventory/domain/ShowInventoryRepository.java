package com.seatwise.inventory.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowInventoryRepository extends JpaRepository<ShowInventory, Long> {

  List<ShowInventory> findByShowId(Long showId);
}
