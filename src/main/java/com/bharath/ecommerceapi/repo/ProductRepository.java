package com.bharath.ecommerceapi.repo;

import com.bharath.ecommerceapi.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);

    @Query("select p from Product p where p.price between ?1 and ?2")
    List<Product> findByPriceRange(Double minPrice, Double maxPrice);

    List<Product> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String keyword, String keyword1);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query(value = "ALTER TABLE order_items ALTER COLUMN product_id DROP NOT NULL", nativeQuery = true)
    void dropNotNullConstraintOnOrderItems();

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query(value = "ALTER TABLE cart_items ALTER COLUMN product_id DROP NOT NULL", nativeQuery = true)
    void dropNotNullConstraintOnCartItems();

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query(value = "ALTER TABLE wish_list_items ALTER COLUMN product_id DROP NOT NULL", nativeQuery = true)
    void dropNotNullConstraintOnWishlistItems();
}
