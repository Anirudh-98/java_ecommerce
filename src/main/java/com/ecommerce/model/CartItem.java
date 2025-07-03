package com.ecommerce.model;

import javax.persistence.*;
import javax.validation.constraints.Min;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER) // Eager fetch product for easier access
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    // Constructors
    public CartItem() {
    }

    public CartItem(Cart cart, Product product, int quantity) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // hashCode and equals based on cart and product for Set operations if needed elsewhere,
    // but primarily for JPA identity.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CartItem cartItem = (CartItem) o;

        if (id != null ? !id.equals(cartItem.id) : cartItem.id != null) return false;
        if (cart != null ? !cart.getId().equals(cartItem.cart.getId()) : cartItem.cart != null) return false; // Compare IDs to avoid circular issues
        return product != null ? product.getId().equals(cartItem.product.getId()) : cartItem.product == null; // Compare IDs
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        // Avoid using full cart/product objects in hashCode if they can cause deep graph traversal
        // or if their own hashCodes are complex/mutable in ways that affect this item's identity in a Set.
        // Using IDs is safer if items are uniquely identified by cart and product within that cart.
        result = 31 * result + (cart != null && cart.getId() != null ? cart.getId().hashCode() : 0);
        result = 31 * result + (product != null && product.getId() != null ? product.getId().hashCode() : 0);
        return result;
    }
}
