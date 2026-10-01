package com.bharath.ecommerceapi.service.impl;

import com.bharath.ecommerceapi.exception.BadRequestException;
import com.bharath.ecommerceapi.exception.ForbiddenException;
import com.bharath.ecommerceapi.exception.ResourceNotFoundException;
import com.bharath.ecommerceapi.model.*;
import com.bharath.ecommerceapi.model.dto.request.OrderRequest;
import com.bharath.ecommerceapi.model.dto.request.OrderStatusUpdateRequest;
import com.bharath.ecommerceapi.model.dto.response.OrderItemResponse;
import com.bharath.ecommerceapi.model.dto.response.OrderResponse;
import com.bharath.ecommerceapi.model.enums.Role;
import com.bharath.ecommerceapi.model.enums.Status;
import com.bharath.ecommerceapi.repo.CartRepository;
import com.bharath.ecommerceapi.repo.OrderRepository;
import com.bharath.ecommerceapi.repo.ProductRepository;
import com.bharath.ecommerceapi.service.inf.IOrderService;
import com.bharath.ecommerceapi.service.inf.IUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final IUserService userService;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        User currentUser = userService.getCurrentUser();
        Cart cart = cartRepository.findFirstByUserIdOrderByIdAsc(currentUser.getId()).orElseThrow(
                () -> new ResourceNotFoundException("Cart not found"));
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }
        Order order = Order.builder()
                .user(currentUser)
                .shippingAddress(request.getShippingAddress())
                .totalAmount(0.0)
                .status(Status.PENDING)
                .build();

        double total = 0.0;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException("No Enough Stock for the Product: " + product.getTitle());
            }
            OrderItem orderItem = OrderItem.builder()
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .order(order)
                    .product(product)
                    .build();
            order.getItems().add(orderItem);

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
            total += product.getPrice() * cartItem.getQuantity();
        }
        order.setTotalAmount(total);
        Order savedOrder = orderRepository.save(order);
        cart.getItems().clear();
        cartRepository.save(cart);
        return mapToOrderResponse(savedOrder);
    }

    @Override
    public List<OrderResponse> getMyOrders() {
        User currentUser = userService.getCurrentUser();
        return orderRepository.findByUserId(currentUser.getId()).stream()
                .map(this::mapToOrderResponse).collect(Collectors.toList());
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        return mapToOrderResponse(orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order Not Found" + id)));
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(request.getId()).orElseThrow(
                () -> new ResourceNotFoundException("Order Not Found" + request.getId()));
        User currentUser = userService.getCurrentUser();

        if(!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getRole().equals(Role.SELLER)) {
            throw new ForbiddenException("Only Admin or Seller Can Perform this Operation!");
        }

        if(order.getStatus().equals(Status.DELIVERED) || order.getStatus().equals(Status.CANCELLED)) {
            throw new BadRequestException("Order Cannot be Modified In this Status");
        }

        Status newStatus;
        try{
            newStatus = Status.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e){
            throw new BadRequestException("Invalid Order Status: " + request.getStatus());
        }

        if(newStatus == Status.CANCELLED) {
            for (OrderItem orderItem : order.getItems()) {
                Product product = orderItem.getProduct();
                product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
                productRepository.save(product);
            }
        }
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        return mapToOrderResponse(updatedOrder);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> orderItems = order.getItems().stream()
                .map(this::mapToOrderItemResponse).toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .userId(order.getUser().getId())
                .items(orderItems)
                .build();
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem orderItem) {
        return OrderItemResponse.builder()
                .id(orderItem.getId())
                .quantity(orderItem.getQuantity())
                .priceAtPurchase(orderItem.getPriceAtPurchase())
                .productTitle(orderItem.getProduct().getTitle())
                .productBrand(orderItem.getProduct().getBrand())
                .productModel(orderItem.getProduct().getModel())
                .subTotal(orderItem.getQuantity() * orderItem.getPriceAtPurchase())
                .build();
    }
}
