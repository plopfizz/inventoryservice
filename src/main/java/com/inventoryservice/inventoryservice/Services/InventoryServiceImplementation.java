package com.inventoryservice.inventoryservice.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventoryservice.inventoryservice.Dto.ProductEvent;
import com.inventoryservice.inventoryservice.Dto.StockAdjustment;
import com.inventoryservice.inventoryservice.Dto.StockAdjustmentActionEnum;
import com.inventoryservice.inventoryservice.Entities.BelowThresholdProductQuantity;
import com.inventoryservice.inventoryservice.Entities.Inventory;
import com.inventoryservice.inventoryservice.Repositories.BelowThresholdProductsRepository;
import com.inventoryservice.inventoryservice.Repositories.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class InventoryServiceImplementation implements InventoryService {
    @Autowired
    private InventoryRepository inventoryRepository;
    private final Integer threshold = 100;
    @Autowired
    private BelowThresholdProductsRepository belowThresholdProductsRepository;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @KafkaListener(topics = "product_events", groupId = "inventory_group")
    public void handleProductEvents(JsonNode event) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductEvent productEvent = objectMapper.treeToValue(event, ProductEvent.class);

        try {
            switch (productEvent.getActionType()) {
                case "CREATE":
                    createInventory( new Inventory(null,productEvent.getProductId(),1000, LocalDateTime.now()));
                    break;
                case "DELETE":
                    deleteInventoryByProductId(productEvent.getProductId());
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override

    public void adjustStock(StockAdjustment adjustment) {
        Optional<Inventory> inventoryOptional = inventoryRepository.findByProductId(adjustment.getProductId());
        if (inventoryOptional.isPresent()) {
            Inventory inventory = inventoryOptional.get();
            if (StockAdjustmentActionEnum.INCREASE.equals(adjustment.getStockAdjustmentAction())) {
                inventory.setQuantity(inventory.getQuantity() + adjustment.getQuantity());
            } else if (StockAdjustmentActionEnum.DECREASE.equals(adjustment.getStockAdjustmentAction())) {
                if (inventory.getQuantity() >= adjustment.getQuantity()) {
                    inventory.setQuantity(inventory.getQuantity() - adjustment.getQuantity());
                    if(threshold > inventory.getQuantity()){
                        String message = "Product with ID: " + inventory.getProductId() + " is low in stock!";
                        kafkaTemplate.send("low_stock_alerts", message);
                        Optional<BelowThresholdProductQuantity> products =belowThresholdProductsRepository.findByProductId(inventory.getProductId());
                        if(products.isPresent()){
                            BelowThresholdProductQuantity existingProductWithLessInventory = products.get();
                            existingProductWithLessInventory.setQuantity(inventory.getQuantity());
                            belowThresholdProductsRepository.save(existingProductWithLessInventory);
                        }
                        else {
                            belowThresholdProductsRepository.save(new BelowThresholdProductQuantity(null, inventory.getProductId(), inventory.getQuantity(), LocalDateTime.now()));
                        }
                    }


                } else {
                    throw new RuntimeException("Insufficient stock for product");
                }
            } else {
                throw new RuntimeException("Invalid stock adjustment action");
            }
    inventoryRepository.save(inventory);

        } else {
            // Throw an exception if product is not found
            throw new RuntimeException("Product not found in inventory");
        }
    }


    @Override
    public Inventory createInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    @Override
    public Optional<Inventory> getInventoryByProduct(String productId) {
        return inventoryRepository.findByProductId(productId);
    }

    @Override
    public void deleteInventoryByProductId(String productId) {
        Optional<Inventory> inventoryOptional = inventoryRepository.findByProductId(productId);
        if(inventoryOptional.isPresent()) {
            Inventory inventory = inventoryOptional.get();
             inventoryRepository.delete(inventory);
        }else {
            throw new RuntimeException("Product not found in inventory");
        }
    }
}
