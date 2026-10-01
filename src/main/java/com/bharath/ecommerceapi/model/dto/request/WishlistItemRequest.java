package com.bharath.ecommerceapi.model.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WishlistItemRequest {

    @NotNull(message = "product Id is mandatory")
    @JsonProperty("productId")
    @JsonAlias({"product_id", "productID", "id"})
    private Long productId;
}
