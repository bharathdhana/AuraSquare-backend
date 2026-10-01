package com.bharath.ecommerceapi.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long id;
    private Long productId;
    private int quantity;
    private Double productPrice;
    private String productTitle;
    private String productBrand;
    private String productModel;
    private double subTotal;
}
