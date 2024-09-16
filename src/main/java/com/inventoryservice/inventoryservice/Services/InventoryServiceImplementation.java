package com.inventoryservice.inventoryservice.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventoryservice.inventoryservice.Dto.OutBoxOrderEntity;
import com.inventoryservice.inventoryservice.Dto.ProductEvent;
import com.inventoryservice.inventoryservice.Dto.StockAdjustment;
import com.inventoryservice.inventoryservice.Dto.StockAdjustmentActionEnum;
import com.inventoryservice.inventoryservice.Entities.BelowThresholdProductQuantity;
import com.inventoryservice.inventoryservice.Entities.Inventory;
import com.inventoryservice.inventoryservice.Repositories.BelowThresholdProductsRepository;
import com.inventoryservice.inventoryservice.Repositories.InventoryRepository;
import jakarta.transaction.Transactional;
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
    private KafkaTemplate<String, Object> kafkaTemplate;


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

    @KafkaListener(topics = "update_order_quantity", groupId = "inventory_group")
    public void reserveOrderForProduct(JsonNode event) throws IOException {
        // Log the raw event to check its content
        System.out.println("Received event: " + event.toString()+" "+LocalDateTime.now());

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            OutBoxOrderEntity product = objectMapper.treeToValue(event, OutBoxOrderEntity.class);
            adjustStock(new StockAdjustment(product.getProductId(),product.getQuantity(),product.getStockAdjustmentEnum()));
            System.out.println("Product details: " + product.toString());
        } catch (JsonProcessingException e) {
            System.err.println("Error during deserialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override

    public void adjustStock(StockAdjustment adjustment) {
        Optional<Inventory> inventoryOptional = inventoryRepository.findByProductId(adjustment.getProductId());
        System.out.println("we are in adjustStock "+LocalDateTime.now());
        if (inventoryOptional.isPresent()) {
            Inventory inventory = inventoryOptional.get();
            if (StockAdjustmentActionEnum.INCREASE.equals(adjustment.getStockAdjustmentAction())) {
                inventory.setQuantity(inventory.getQuantity() + adjustment.getQuantity());
            } else if (StockAdjustmentActionEnum.DECREASE.equals(adjustment.getStockAdjustmentAction())) {
                if (inventory.getQuantity() >= adjustment.getQuantity()) {
                    inventory.setQuantity(inventory.getQuantity() - adjustment.getQuantity());
                    if(threshold > inventory.getQuantity()){
                        String message = "Product with ID: " + inventory.getProductId() + " is low in stock!";
                        //implement string object behaviour as a generalized view
                        ObjectMapper objectMapper = new ObjectMapper();
                        try {
                            JsonNode nodeMessage = objectMapper.readTree(message);
                            kafkaTemplate.send("low_stock_alerts", nodeMessage);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Optional<BelowThresholdProductQuantity> products = belowThresholdProductsRepository.findByProductId(inventory.getProductId());
                        if (products.isPresent()) {
                            BelowThresholdProductQuantity existingProductWithLessInventory = products.get();
                            existingProductWithLessInventory.setQuantity(inventory.getQuantity());
                            belowThresholdProductsRepository.save(existingProductWithLessInventory);
                        } else {
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

    @KafkaListener(topics = "reserve_inventory")
    @Transactional
    public void handleInventoryReservation(JsonNode jsonNode) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        OutBoxOrderEntity outBoxOrder = objectMapper.treeToValue(jsonNode,OutBoxOrderEntity.class);
        Optional<Inventory> inventory = inventoryRepository.findByProductId(outBoxOrder.getProductId());
        if (inventory.isPresent()) {
            Inventory inventory1 = inventory.get();
            if ( inventory1.getQuantity() >= outBoxOrder.getQuantity()) {

                inventory1.setQuantity(inventory1.getQuantity() - outBoxOrder.getQuantity());
                    if(threshold >= inventory1.getQuantity()){
                        String message = "Product with ID: " + inventory1.getProductId() + " is low in stock!";
                        //implement string object behaviour as a generalized view

                        try {
                            JsonNode nodeMessage = objectMapper.createObjectNode().put("message", message);;
                            kafkaTemplate.send("low_stock_alerts", nodeMessage);


                        } catch (Exception e) {
                            System.out.println("we are in low stock alert zone");
                            e.printStackTrace();
                        }
                        Optional<BelowThresholdProductQuantity> products = belowThresholdProductsRepository.findByProductId(inventory1.getProductId());
                        if (products.isPresent()) {
                            BelowThresholdProductQuantity existingProductWithLessInventory = products.get();
                            existingProductWithLessInventory.setQuantity(inventory1.getQuantity());
                            belowThresholdProductsRepository.save(existingProductWithLessInventory);
                        } else {
                            belowThresholdProductsRepository.save(new BelowThresholdProductQuantity(null, inventory1.getProductId(), inventory1.getQuantity(), LocalDateTime.now()));
                        }
                    }



                inventoryRepository.save(inventory1);
                try {
                    JsonNode outBoxOrderJson = objectMapper.valueToTree(outBoxOrder);
                    kafkaTemplate.send("inventory_reserved", outBoxOrderJson);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                try {
                    JsonNode outBoxOrderJson = objectMapper.valueToTree(outBoxOrder);
                    // Send failure event with JSON string
                    kafkaTemplate.send("inventory_not_available", outBoxOrderJson);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @KafkaListener(topics = "release_inventory")
    @Transactional
    public void handleReleaseInventory(JsonNode jsonNode) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        OutBoxOrderEntity outBoxOrder = objectMapper.treeToValue(jsonNode,OutBoxOrderEntity.class);
        Optional<Inventory> inventory = inventoryRepository.findByProductId(outBoxOrder.getProductId());
        if(inventory.isPresent()) {
            Inventory inventory1 = inventory.get();
            System.out.println("we are in the release inventory 1"+" "+ inventory1.getQuantity());

            inventory1.setQuantity(inventory1.getQuantity() + outBoxOrder.getQuantity());
            System.out.println("we are in the release inventory 2"+" "+ inventory1.getQuantity());

            inventoryRepository.save(inventory1);
        }
    }

}
