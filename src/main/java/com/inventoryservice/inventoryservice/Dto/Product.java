package com.inventoryservice.inventoryservice.Dto;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.List;

@Data

public class Product {

        @Id
        private String id;
        private String name;
        private String description;
        private int price;
        private Date createdAt;
        private Date updatedAt;
        private Boolean isAvailable;
        private Category category;
        private List<String> attributes;
        private List<String> imageUrls;

        private Double averageRating;
        private Integer totalReviews;




}
