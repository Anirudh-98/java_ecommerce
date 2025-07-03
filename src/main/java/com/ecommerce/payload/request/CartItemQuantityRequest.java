package com.ecommerce.payload.request;

import javax.validation.constraints.Min;

public class CartItemQuantityRequest {

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity = 1; // Default to 1 if not provided, useful for "add to cart"

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
