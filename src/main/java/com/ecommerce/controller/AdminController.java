package com.ecommerce.controller;

import com.ecommerce.model.Product; // Keep if still used for viewing products
import com.ecommerce.model.User;
// Remove ProductRequest if admin no longer adds products
// import com.ecommerce.payload.request.ProductRequest;
import com.ecommerce.payload.response.AdminDashboardAnalyticsResponse;
import com.ecommerce.payload.response.MessageResponse;
import com.ecommerce.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // Product Viewing (Admin might still need to view all products)
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = adminService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable Long productId) {
        try {
            Product product = adminService.getProductById(productId);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }


    // User Management
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = adminService.getAllUsers();
        // Consider creating a UserResponse DTO to avoid exposing password hashes directly
        // For simplicity, returning User objects directly here.
        users.forEach(user -> user.setPassword(null)); // Mask password
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            User user = adminService.getUserById(userId);
            user.setPassword(null); // Mask password
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // Seller Management Endpoints
    @GetMapping("/sellers/pending")
    public ResponseEntity<List<User>> getPendingSellers() {
        List<User> pendingSellers = adminService.getPendingSellers();
        pendingSellers.forEach(user -> user.setPassword(null)); // Mask password
        return ResponseEntity.ok(pendingSellers);
    }

    @PostMapping("/sellers/approve/{userId}")
    public ResponseEntity<?> approveSeller(@PathVariable Long userId) {
        try {
            User approvedSeller = adminService.approveSeller(userId);
            approvedSeller.setPassword(null); // Mask password
            return ResponseEntity.ok(new MessageResponse("Seller approved successfully. User ID: " + approvedSeller.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/sellers/reject/{userId}")
    public ResponseEntity<?> rejectSeller(@PathVariable Long userId) {
        try {
            User rejectedSeller = adminService.rejectSeller(userId);
            rejectedSeller.setPassword(null); // Mask password
            return ResponseEntity.ok(new MessageResponse("Seller rejected successfully. User ID: " + rejectedSeller.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // Dashboard Analytics Endpoint
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardAnalyticsResponse> getDashboardAnalytics() {
        AdminDashboardAnalyticsResponse analytics = adminService.getDashboardAnalytics();
        return ResponseEntity.ok(analytics);
    }
}
