package com.inventoryservice.inventoryservice.Dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockAdjustment {
    private String productId;
    private int quantity;
    private StockAdjustmentActionEnum stockAdjustmentAction;
}
