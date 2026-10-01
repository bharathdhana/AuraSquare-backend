package com.bharath.ecommerceapi.service.impl;

import com.bharath.ecommerceapi.exception.BadRequestException;
import com.bharath.ecommerceapi.exception.ResourceNotFoundException;
import com.bharath.ecommerceapi.model.*;
import com.bharath.ecommerceapi.model.dto.request.WishlistItemRequest;
import com.bharath.ecommerceapi.model.dto.response.ProductResponse;
import com.bharath.ecommerceapi.model.dto.response.WishlistItemResponse;
import com.bharath.ecommerceapi.model.dto.response.WishlistResponse;
import com.bharath.ecommerceapi.repo.ProductRepository;
import com.bharath.ecommerceapi.repo.WishlistItemRepository;
import com.bharath.ecommerceapi.repo.WishlistRepository;
import com.bharath.ecommerceapi.service.inf.IUserService;
import com.bharath.ecommerceapi.service.inf.IWishlistService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements IWishlistService {

    private final WishlistRepository wishlistRepository;
    private final IUserService userService;
    private final ProductRepository productRepository;
    private final WishlistItemRepository wishlistItemRepository;

    @Override
    @Transactional
    public String addToWishlist(WishlistItemRequest request) {
        User currentUser = userService.getCurrentUser();
        Wishlist wishlist =  wishlistRepository.findFirstByUserIdOrderByIdAsc(currentUser.getId())
                .orElseGet(()-> createWishlistForUser(currentUser));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found for the Given ID: " + request.getProductId()));

        boolean isAlreadyExist = wishlist.getWishlistItems().stream()
                .anyMatch(item -> item.getProduct().getId().equals(request.getProductId()));
        if(isAlreadyExist){
            throw new BadRequestException("Product Already Exists");
        }

        WishlistItem wishlistItem = WishlistItem.builder()
                .wishlist(wishlist)
                .product(product)
                .build();
        wishlist.getWishlistItems().add(wishlistItem);
        wishlistRepository.save(wishlist);
        return "Product Added to Wishlist Successfully!";
    }

    @Override
    public WishlistResponse getWishlist() {
        User currentUser = userService.getCurrentUser();
        Wishlist wishlist =  wishlistRepository.findFirstByUserIdOrderByIdAsc(currentUser.getId())
                .orElseGet(()-> createWishlistForUser(currentUser));
        return mapToWishlistResponse(wishlist);
    }

    @Override
    @Transactional
    public WishlistResponse removeFromWishlist(Long id) {
        User currentUser = userService.getCurrentUser();
        Wishlist wishlist = wishlistRepository.findFirstByUserIdOrderByIdAsc(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist Not Found"));
        WishlistItem wishlistItem = wishlistItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist item Not Found"));

        wishlist.getWishlistItems().remove(wishlistItem);
        wishlistItemRepository.delete(wishlistItem);
        wishlistRepository.save(wishlist);
        return mapToWishlistResponse(wishlist);
    }

    private WishlistResponse mapToWishlistResponse(Wishlist wishlist) {
        List<WishlistItemResponse> wishItems = wishlist.getWishlistItems().stream()
                .map(this::mapToWishlistItemResponse).toList();
        return WishlistResponse.builder().
                wishlistItems(wishItems)
                .build();
    }

    private WishlistItemResponse mapToWishlistItemResponse(WishlistItem wishlistItem) {
        return WishlistItemResponse.builder()
                .id(wishlistItem.getId())
                .product(mapToProductResponse(wishlistItem.getProduct()))
                .build();
    }

    private Wishlist createWishlistForUser(User currentUser) {
        Wishlist wishlist = Wishlist.builder()
                .user(currentUser)
                .build();
        wishlistRepository.save(wishlist);
        return wishlist;
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
