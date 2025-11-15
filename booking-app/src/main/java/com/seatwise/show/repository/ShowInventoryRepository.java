package com.seatwise.show.repository;

import com.seatwise.show.entity.ShowInventory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowInventoryRepository extends JpaRepository<ShowInventory, Long> {

  List<ShowInventory> findByShowId(Long showId);
}
