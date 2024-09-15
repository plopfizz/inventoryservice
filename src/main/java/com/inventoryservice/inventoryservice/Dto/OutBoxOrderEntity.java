package com.inventoryservice.inventoryservice.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutBoxOrderEntity {

   private Long id;
   private Integer quantity;
   private String productId;
   private Long orderId;
   private StockAdjustmentActionEnum stockAdjustmentEnum;

}
