package com.inventoryservice.inventoryservice.Services;

import com.inventoryservice.inventoryservice.Dto.StockAdjustment;
import com.inventoryservice.inventoryservice.Entities.Inventory;

import java.util.Optional;

public interface InventoryService {
    void adjustStock(StockAdjustment adjustment);

    Inventory createInventory(Inventory inventory);

    Optional<Inventory> getInventoryByProduct(String productId);

    void deleteInventoryByProductId(String productId);
}
