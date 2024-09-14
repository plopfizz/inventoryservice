package com.inventoryservice.inventoryservice.Dto;



import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class ProductEvent implements Serializable {
    private String actionType;
    private String productId;


}
