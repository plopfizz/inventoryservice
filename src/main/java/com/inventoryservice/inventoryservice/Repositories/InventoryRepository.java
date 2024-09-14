package com.inventoryservice.inventoryservice.Repositories;

import com.inventoryservice.inventoryservice.Entities.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    Optional<Inventory> findByProductId(String productId);
}
