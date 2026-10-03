package com.bharath.ecommerceapi.service.impl;

import com.bharath.ecommerceapi.exception.ResourceNotFoundException;
import com.bharath.ecommerceapi.exception.UnAuthorizedException;
import com.bharath.ecommerceapi.model.Product;
import com.bharath.ecommerceapi.model.User;
import com.bharath.ecommerceapi.model.CartItem;
import com.bharath.ecommerceapi.model.OrderItem;
import com.bharath.ecommerceapi.model.WishlistItem;
import com.bharath.ecommerceapi.model.dto.request.ProductRequest;
import com.bharath.ecommerceapi.model.dto.response.ProductResponse;
import com.bharath.ecommerceapi.model.enums.Role;
import com.bharath.ecommerceapi.repo.ProductRepository;
import com.bharath.ecommerceapi.service.inf.IProductService;
import com.bharath.ecommerceapi.service.inf.IUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final IUserService userService;

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToProductResponse).collect(Collectors.toList());
    }

    @Override
    public ProductResponse getProductById(Long id) {
        return mapToProductResponse(productRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Product Not Found for the Given ID: " + id)));
    }

    @Override
    public List<ProductResponse> getProductsByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(this::mapToProductResponse).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByPriceRange(Double minPrice, Double maxPrice) {
        return productRepository.findByPriceRange(minPrice, maxPrice).stream()
                .map(this::mapToProductResponse).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> searchProducts(String keyword) {
        return productRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword).stream()
                .map(this::mapToProductResponse).collect(Collectors.toList());
    }

    @Override
    public String createProduct(ProductRequest request) {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getRole() != Role.SELLER) {
            throw new UnAuthorizedException("Access Denied! Only SELLER's can perform this operation");
        }

        Product product = Product.builder()
                .title(request.getTitle())
                .brand(request.getBrand())
                .model(request.getModel())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .seller(currentUser)
                .build();
        productRepository.save(product);
        return "Product Created Successfully";
    }

    @Override
    @Transactional
    public String updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Product Not Found for the Given ID: " + id));
        User currentUser = userService.getCurrentUser();
        if (!(product.getSeller().getId().equals(currentUser.getId())) && currentUser.getRole() != Role.ADMIN) {
            throw new UnAuthorizedException("Access Denied! Only SELLER's of this Product or ADMIN can ONLY perform this operation");
        }

        product.setTitle(request.getTitle());
        product.setBrand(request.getBrand());
        product.setModel(request.getModel());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(request.getCategory());
        product.setImageUrl(request.getImageUrl());

        return "Product Updated Successfully";
    }

    @Override
    @Transactional
    public String deleteProduct(Long id) {
        Product product = productRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Product Not Found for the Given ID: " + id));
        User currentUser = userService.getCurrentUser();
        if (!(product.getSeller().getId().equals(currentUser.getId())) && currentUser.getRole() != Role.ADMIN) {
            throw new UnAuthorizedException("Access Denied! Only SELLER's of this Product or ADMIN can ONLY perform this operation");
        }

        try {
            productRepository.dropNotNullConstraintOnOrderItems();
            productRepository.dropNotNullConstraintOnCartItems();
            productRepository.dropNotNullConstraintOnWishlistItems();
        } catch (Exception ignored) {}

        for (OrderItem item : product.getOrderItems()) {
            item.setProduct(null);
        }
        for (CartItem item : product.getCartItems()) {
            item.setProduct(null);
        }
        for (WishlistItem item : product.getWishlistItems()) {
            item.setProduct(null);
        }

        productRepository.delete(product);
        return "Product Deleted Successfully";
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .brand(product.getBrand())
                .model(product.getModel())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .sellerId(product.getSeller().getId())
                .sellerName(product.getSeller().getFirstName() + " " + product.getSeller().getLastName())
                .build();
    }
}
