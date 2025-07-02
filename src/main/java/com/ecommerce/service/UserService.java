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
    public Cart addProductToCart(String username, Long productId) {
        User user = getCurrentUser(username);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart(user);
                    return cartRepository.save(newCart);
                });
        cart.addProduct(product);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeProductFromCart(String username, Long productId) {
        User user = getCurrentUser(username);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + username));
        cart.removeProduct(product);
        return cartRepository.save(cart);
    }

    public Set<Product> getCartProducts(String username) {
        User user = getCurrentUser(username);
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + username));
        return cart.getProducts();
    }
}
