package com.inventoryservice.inventoryservice.Repositories;

import com.inventoryservice.inventoryservice.Entities.BelowThresholdProductQuantity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BelowThresholdProductsRepository extends JpaRepository<BelowThresholdProductQuantity,Long> {
    Optional<BelowThresholdProductQuantity> findByProductId(String productId);
}
