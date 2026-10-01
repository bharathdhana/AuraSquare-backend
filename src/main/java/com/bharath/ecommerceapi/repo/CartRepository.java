package com.bharath.ecommerceapi.repo;

import com.bharath.ecommerceapi.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findFirstByUserIdOrderByIdAsc(Long id);
}
