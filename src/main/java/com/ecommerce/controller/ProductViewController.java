package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.service.UserService; // Can reuse UserService for public product viewing
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/products") // Public base path for products
public class ProductViewController {

    @Autowired
    private UserService userService; // Or a dedicated ProductService if preferred

    @GetMapping("/view") // Publicly accessible endpoint
    public ResponseEntity<List<Product>> viewAllProducts() {
        List<Product> products = userService.getAllProducts();
        return ResponseEntity.ok(products);
    }
}
