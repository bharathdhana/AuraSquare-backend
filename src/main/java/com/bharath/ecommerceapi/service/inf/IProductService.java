package com.bharath.ecommerceapi.service.inf;

import com.bharath.ecommerceapi.model.dto.request.ProductRequest;
import com.bharath.ecommerceapi.model.dto.response.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IProductService {
    List<ProductResponse> getAllProducts();
    ProductResponse getProductById(Long id);
    List<ProductResponse> getProductsByCategory(String category);
    List<ProductResponse> getProductsByPriceRange(Double minPrice, Double maxPrice);
    List<ProductResponse> searchProducts(String keyword);
    String createProduct(ProductRequest request);
    String updateProduct(Long id, ProductRequest request);
    String deleteProduct(Long id);
}
