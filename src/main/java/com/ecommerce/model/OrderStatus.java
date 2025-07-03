package com.ecommerce.model;

public enum OrderStatus {
    PENDING_PAYMENT,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    COMPLETED, // Typically means delivered and no further action needed from system for this order
    CANCELLED,
    REFUNDED
}
