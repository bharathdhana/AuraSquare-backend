package com.bharath.ecommerceapi.repo;

import com.bharath.ecommerceapi.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findFirstByCartIdAndProductIdOrderByIdAsc(Long id, Long id1);
}
