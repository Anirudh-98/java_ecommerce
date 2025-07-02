package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.payload.response.MessageResponse;
import com.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    private String getCurrentUsername(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    // Product Listing (also accessible publicly via a different endpoint if needed)
    @GetMapping("/products")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = userService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    // Wishlist Management
    @PostMapping("/wishlist/add/{productId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> addProductToWishlist(Authentication authentication, @PathVariable Long productId) {
        try {
            userService.addProductToWishlist(getCurrentUsername(authentication), productId);
            return ResponseEntity.ok(new MessageResponse("Product added to wishlist successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/wishlist/remove/{productId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> removeProductFromWishlist(Authentication authentication, @PathVariable Long productId) {
        try {
            userService.removeProductFromWishlist(getCurrentUsername(authentication), productId);
            return ResponseEntity.ok(new MessageResponse("Product removed from wishlist successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/wishlist")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getWishlistProducts(Authentication authentication) {
        try {
            Set<Product> products = userService.getWishlistProducts(getCurrentUsername(authentication));
            return ResponseEntity.ok(products);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // Cart Management
    @PostMapping("/cart/add/{productId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> addProductToCart(Authentication authentication, @PathVariable Long productId) {
        try {
            userService.addProductToCart(getCurrentUsername(authentication), productId);
            return ResponseEntity.ok(new MessageResponse("Product added to cart successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/cart/remove/{productId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> removeProductFromCart(Authentication authentication, @PathVariable Long productId) {
        try {
            userService.removeProductFromCart(getCurrentUsername(authentication), productId);
            return ResponseEntity.ok(new MessageResponse("Product removed from cart successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/cart")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getCartProducts(Authentication authentication) {
        try {
            Set<Product> products = userService.getCartProducts(getCurrentUsername(authentication));
            return ResponseEntity.ok(products);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
