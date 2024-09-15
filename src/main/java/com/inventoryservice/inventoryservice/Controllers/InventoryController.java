package com.inventoryservice.inventoryservice.Controllers;



import com.inventoryservice.inventoryservice.Dto.StockAdjustment;
import com.inventoryservice.inventoryservice.Entities.Inventory;
import com.inventoryservice.inventoryservice.Services.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping
    public Inventory createInventory(@RequestBody Inventory inventory) {
        return inventoryService.createInventory(inventory);
    }

    @GetMapping("/product/{productId}")
    public Optional<Inventory> getInventoryByProduct(@PathVariable String productId) {
        return inventoryService.getInventoryByProduct(productId);
    }

    @PostMapping("/adjust")
    public void adjustStock(@RequestBody StockAdjustment adjustment) {
        inventoryService.adjustStock(adjustment);
    }

}
