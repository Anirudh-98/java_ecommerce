package com.ecommerce.service;

import com.ecommerce.model.*;
import com.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    private User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    // Wishlist operations
    @Transactional
    public Wishlist addProductToWishlist(String username, Long productId) {
        User user = getCurrentUser(username);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseGet(() -> {
                    Wishlist newWishlist = new Wishlist(user);
                    return wishlistRepository.save(newWishlist);
                });
        wishlist.addProduct(product);
        return wishlistRepository.save(wishlist);
    }

    @Transactional
    public Wishlist removeProductFromWishlist(String username, Long productId) {
        User user = getCurrentUser(username);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wishlist not found for user: " + username));
        wishlist.removeProduct(product);
        return wishlistRepository.save(wishlist);
    }

    public Set<Product> getWishlistProducts(String username) {
        User user = getCurrentUser(username);
        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wishlist not found for user: " + username));
        return wishlist.getProducts();
    }

    // Cart operations
    @Transactional
    public Cart addProductToCart(String username, Long productId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        User user = getCurrentUser(username);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart(user);
                    return cartRepository.save(newCart);
                });

        CartItem cartItem = cart.findItemByProduct(product);
        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            cartItem = new CartItem(cart, product, quantity);
            cart.addItem(cartItem); // This also sets cart in cartItem
        }
        // cartItemRepository.save(cartItem); // Cascade should handle this if cart is saved
        return cartRepository.save(cart); // Saving cart will cascade to items
    }

    @Transactional
    public Cart removeProductFromCart(String username, Long productId) {
        User user = getCurrentUser(username);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + username));

        CartItem cartItem = cart.findItemByProduct(product);
        if (cartItem != null) {
            cart.removeItem(cartItem); // This also sets cartItem.setCart(null)
            cartItemRepository.delete(cartItem); // Explicitly delete the orphan CartItem
                                                 // orphanRemoval=true on Cart.items should also handle this
                                                 // when cart is saved. Explicit delete is safer.
        } else {
            throw new RuntimeException("Product not found in cart.");
        }
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart increaseCartItemQuantity(String username, Long productId) {
        User user = getCurrentUser(username);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + username));

        CartItem cartItem = cart.findItemByProduct(product);
        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            // cartItemRepository.save(cartItem); // Cascade should handle
        } else {
            throw new RuntimeException("Product not found in cart to increase quantity.");
        }
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart decreaseCartItemQuantity(String username, Long productId) {
        User user = getCurrentUser(username);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + username));

        CartItem cartItem = cart.findItemByProduct(product);
        if (cartItem != null) {
            int newQuantity = cartItem.getQuantity() - 1;
            if (newQuantity < 1) {
                // Remove item from cart if quantity drops below 1
                cart.removeItem(cartItem);
                cartItemRepository.delete(cartItem); // Explicit delete
            } else {
                cartItem.setQuantity(newQuantity);
                // cartItemRepository.save(cartItem); // Cascade should handle
            }
        } else {
            throw new RuntimeException("Product not found in cart to decrease quantity.");
        }
        return cartRepository.save(cart);
    }


    public Set<CartItem> getCartItems(String username) {
        User user = getCurrentUser(username);
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + username));
        // Ensure items are fetched if LAZY loading
        // Forcing initialization if needed, though accessing cart.getItems() should trigger it.
        // Hibernate.initialize(cart.getItems()); // Example if direct access isn't enough
        return cart.getItems();
    }
}
