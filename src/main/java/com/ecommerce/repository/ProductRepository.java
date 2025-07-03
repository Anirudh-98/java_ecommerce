package com.ecommerce.repository;

import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySeller(User seller);
    // Consider adding findBySellerId if you often have the ID and not the User object
    // List<Product> findBySellerId(Long sellerId);
}
