package com.inventoryservice.inventoryservice.Dto;

import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.data.annotation.Id;

public class Category {
    @Id
    private String id;
    private String name;
}