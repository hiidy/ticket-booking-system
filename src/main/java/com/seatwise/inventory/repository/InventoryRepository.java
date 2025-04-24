package com.seatwise.inventory.repository;

import com.seatwise.inventory.domain.ShowInventory;
import com.seatwise.inventory.domain.ShowInventoryPk;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<ShowInventory, ShowInventoryPk> {

  List<ShowInventory> findById_ShowId(Long showId);
}
