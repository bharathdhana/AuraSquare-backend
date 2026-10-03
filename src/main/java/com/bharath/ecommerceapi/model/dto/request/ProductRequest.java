package com.bharath.ecommerceapi.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    @NotBlank(message = "title is mandatory")
    @Size(min = 2, max = 100, message = "title should be between 2 to 100 characters!")
    private String title;

    @NotBlank(message = "brand is mandatory")
    @Size(min = 2, max = 100, message = "brand should be between 2 to 100 characters!")
    private String brand;

    @NotBlank(message = "model is mandatory")
    @Size(min = 2, max = 100, message = "model should be between 2 to 100 characters!")
    private String model;

    @NotBlank(message = "description is mandatory")
    @Size(min = 10, max = 1000, message = "description should be between 10 to 100 characters!")
    private String description;

    @NotNull(message = "price is mandatory")
    @Min(value = 10, message = "price must be greater than 10")
    private double price;

    @NotNull(message = "stockQuantity is mandatory")
    @Min(value = 1, message = "stock quantity should be at least 1")
    private int stockQuantity;

    @NotBlank(message = "category is mandatory")
    private String category;

    @NotBlank(message = "imageUrl is mandatory")
    private String imageUrl;
}
