package com.seatwise.inventory.repository;

import com.seatwise.inventory.entity.ShowInventory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<ShowInventory, Long> {

  List<ShowInventory> findByShowId(Long showId);
}
