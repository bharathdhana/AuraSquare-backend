package com.bharath.ecommerceapi.service.impl;

import com.bharath.ecommerceapi.exception.BadRequestException;
import com.bharath.ecommerceapi.exception.ResourceNotFoundException;
import com.bharath.ecommerceapi.model.Cart;
import com.bharath.ecommerceapi.model.CartItem;
import com.bharath.ecommerceapi.model.Product;
import com.bharath.ecommerceapi.model.User;
import com.bharath.ecommerceapi.model.dto.request.CartItemRequest;
import com.bharath.ecommerceapi.model.dto.response.CartItemResponse;
import com.bharath.ecommerceapi.model.dto.response.CartResponse;
import com.bharath.ecommerceapi.repo.CartItemRepository;
import com.bharath.ecommerceapi.repo.CartRepository;
import com.bharath.ecommerceapi.repo.ProductRepository;
import com.bharath.ecommerceapi.service.inf.ICartService;
import com.bharath.ecommerceapi.service.inf.IUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final IUserService userService;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    @Transactional
    public String addToCart(CartItemRequest request) {
        User currentUser = userService.getCurrentUser();
        Cart cart = cartRepository.findFirstByUserIdOrderByIdAsc(currentUser.getId())
                .orElseGet(() -> createCartForUser(currentUser));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found for the Given ID: " + request.getProductId()));
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Product Stock Not Available at this Moment!");
        }
        CartItem cartItem = cartItemRepository.findFirstByCartIdAndProductIdOrderByIdAsc(cart.getId(), product.getId()).orElse(null);
        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        } else {
            cartItem = CartItem.builder()
                    .quantity(request.getQuantity())
                    .cart(cart)
                    .product(product)
                    .build();
            cart.addItem(cartItem);
        }

        cartRepository.save(cart);
        return "Product Added to Cart Successfully!";
    }

    @Override
    @Transactional
    public CartResponse updateCart(CartItemRequest request) {
        User currentUser = userService.getCurrentUser();
        Cart cart = cartRepository.findFirstByUserIdOrderByIdAsc(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart Not Found"));
        CartItem cartItem = cartItemRepository.findFirstByCartIdAndProductIdOrderByIdAsc(cart.getId(), request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found in Cart"));
        if (request.getQuantity() == 0) {
            cart.removeItem(cartItem);
            cartItemRepository.delete(cartItem);
        } else {
            if (cartItem.getProduct().getStockQuantity() < request.getQuantity()) {
                throw new BadRequestException("Product Stock Not Available at this Moment!");
            }
            cartItem.setQuantity(request.getQuantity());
        }
        cartRepository.save(cart);
        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse getCart() {
        User currentUser = userService.getCurrentUser();
        Cart cart = cartRepository.findFirstByUserIdOrderByIdAsc(currentUser.getId())
                .orElseGet(() -> createCartForUser(currentUser));
        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse removeFromCart(Long productId) {
        User currentUser = userService.getCurrentUser();
        Cart cart = cartRepository.findFirstByUserIdOrderByIdAsc(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart Not Found"));
        CartItem cartItem = cartItemRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found in Cart"));
        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);
        cartRepository.save(cart);
        return mapToCartResponse(cart);
    }

    private Cart createCartForUser(User currentUser) {
        Cart cart = Cart.builder()
                .user(currentUser)
                .build();
        cartRepository.save(cart);
        return cart;
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItem> items = cart.getItems();
        List<CartItemResponse> responseItems = cart.getItems().stream()
                .map(this::mapToCartItemResponse).toList();
        double total = 0.0;
        for (CartItem item : items) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        return CartResponse.builder()
                .id(cart.getId())
                .items(responseItems)
                .totalAmount(total)
                .build();
    }

    private CartItemResponse mapToCartItemResponse(CartItem cartItem) {
        double subtotal;
        subtotal = cartItem.getProduct().getPrice() * cartItem.getQuantity();
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .productId(cartItem.getProduct().getId())
                .quantity(cartItem.getQuantity())
                .productPrice(cartItem.getProduct().getPrice())
                .productTitle(cartItem.getProduct().getTitle())
                .productBrand(cartItem.getProduct().getBrand())
                .productModel(cartItem.getProduct().getModel())
                .subTotal(subtotal)
                .build();
    }
}
