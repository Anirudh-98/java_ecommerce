package com.ecommerce.service;

import com.ecommerce.model.*;
import com.ecommerce.payload.response.AdminDashboardAnalyticsResponse;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    // Product Management - Admin no longer adds products directly
    // Admin can still view all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long productId) { // Retain for viewing specific product details
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
    }

    // User Management & Seller Approval
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    public List<User> getPendingSellers() {
        return userRepository.findByStatus(UserStatus.PENDING);
    }

    @Transactional
    public User approveSeller(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        if (user.getStatus() != UserStatus.PENDING) {
            throw new RuntimeException("User is not pending approval.");
        }
        user.setStatus(UserStatus.APPROVED);
        user.getRoles().add(Role.SELLER); // Add SELLER role
        return userRepository.save(user);
    }

    @Transactional
    public User rejectSeller(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        if (user.getStatus() != UserStatus.PENDING) {
            throw new RuntimeException("User is not pending approval.");
        }
        user.setStatus(UserStatus.REJECTED);
        // Optionally, remove SELLER role if it was added at registration for PENDING users
        // user.getRoles().remove(Role.SELLER);
        return userRepository.save(user);
    }

    // Dashboard Analytics
    public AdminDashboardAnalyticsResponse getDashboardAnalytics() {
        long totalProducts = productRepository.count();

        long totalSellers = userRepository.findAll().stream()
                .filter(user -> user.getRoles().contains(Role.SELLER) && user.getStatus() == UserStatus.APPROVED)
                .count();

        long totalUsers = userRepository.count(); // Total number of all users

        BigDecimal totalRevenue = orderRepository.sumTotalAmountByStatus(OrderStatus.COMPLETED);
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        return new AdminDashboardAnalyticsResponse(totalProducts, totalSellers, totalRevenue, totalUsers);
    }
}
