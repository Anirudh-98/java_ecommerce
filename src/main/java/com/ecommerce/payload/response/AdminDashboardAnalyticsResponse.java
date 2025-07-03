package com.ecommerce.payload.response;

import java.math.BigDecimal;

public class AdminDashboardAnalyticsResponse {
    private long totalProducts;
    private long totalSellers;
    private BigDecimal totalRevenue;
    private long totalUsers; // New field

    public AdminDashboardAnalyticsResponse(long totalProducts, long totalSellers, BigDecimal totalRevenue, long totalUsers) {
        this.totalProducts = totalProducts;
        this.totalSellers = totalSellers;
        this.totalRevenue = totalRevenue;
        this.totalUsers = totalUsers;
    }

    // Getters
    public long getTotalProducts() {
        return totalProducts;
    }

    public long getTotalSellers() {
        return totalSellers;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    // Setters (optional, depending on usage)
    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public void setTotalSellers(long totalSellers) {
        this.totalSellers = totalSellers;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }
}
