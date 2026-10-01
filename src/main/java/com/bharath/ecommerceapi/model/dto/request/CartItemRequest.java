package com.bharath.ecommerceapi.model.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemRequest {
    @NotNull(message = "quantity is mandatory")
    @Min(value = 1, message = "quantity should be at least 1!")
    private Integer quantity;

    @NotNull(message = "product Id is mandatory")
    @JsonProperty("productId")
    @JsonAlias({"product_id", "productID", "id"})
    private Long productId;
}
