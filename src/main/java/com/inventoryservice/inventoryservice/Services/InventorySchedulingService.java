package com.inventoryservice.inventoryservice.Services;

import com.inventoryservice.inventoryservice.Entities.BelowThresholdProductQuantity;
import com.inventoryservice.inventoryservice.Entities.Inventory;
import com.inventoryservice.inventoryservice.Repositories.BelowThresholdProductsRepository;
import com.inventoryservice.inventoryservice.Repositories.InventoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventorySchedulingService {
    @Autowired
    private BelowThresholdProductsRepository belowThresholdProductsRepository;
    @Autowired
    private InventoryRepository inventoryRepository;

    @Scheduled(cron = "0 * * * * * ")
    @Transactional
    public void updateLowStockInventory() {
        List<BelowThresholdProductQuantity> lowStockProducts = belowThresholdProductsRepository.findAll();
    if(!lowStockProducts.isEmpty()) {
        for (BelowThresholdProductQuantity lowStock : lowStockProducts) {
            Inventory inventory = inventoryRepository.findByProductId(lowStock.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + lowStock.getProductId()));

            inventory.setQuantity(inventory.getQuantity() + 1000);
            inventoryRepository.save(inventory);
        }
        belowThresholdProductsRepository.deleteAll(lowStockProducts);
    }
    }
}
