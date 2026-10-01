package com.bharath.ecommerceapi.repo;

import com.bharath.ecommerceapi.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist,Long> {
    Optional<Wishlist> findFirstByUserIdOrderByIdAsc(Long currentUser);
}
