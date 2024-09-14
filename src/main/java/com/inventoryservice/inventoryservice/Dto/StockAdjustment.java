package com.inventoryservice.inventoryservice.Dto;


import lombok.Data;

@Data
public class StockAdjustment {
    private String productId;
    private int quantity;
    private StockAdjustmentActionEnum stockAdjustmentAction;
}
