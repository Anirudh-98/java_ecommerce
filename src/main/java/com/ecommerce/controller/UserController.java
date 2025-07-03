package com.ecommerce.controller;

import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.payload.request.CartItemQuantityRequest;
import com.ecommerce.payload.response.MessageResponse;
import com.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> addProductToCart(Authentication authentication,
                                              @PathVariable Long productId,
                                              @RequestBody(required = false) CartItemQuantityRequest quantityRequest) {
        try {
            String username = getCurrentUsername(authentication);
            int quantity = (quantityRequest != null) ? quantityRequest.getQuantity() : 1;
            if (quantity < 1) {
                 return ResponseEntity.badRequest().body(new MessageResponse("Error: Quantity must be at least 1."));
            }
            userService.addProductToCart(username, productId, quantity);
            return ResponseEntity.ok(new MessageResponse("Product added/updated in cart successfully!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
        catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
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

    @PutMapping("/cart/increase/{productId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> increaseCartItemQuantity(Authentication authentication, @PathVariable Long productId) {
        try {
            userService.increaseCartItemQuantity(getCurrentUsername(authentication), productId);
            return ResponseEntity.ok(new MessageResponse("Item quantity increased successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/cart/decrease/{productId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> decreaseCartItemQuantity(Authentication authentication, @PathVariable Long productId) {
        try {
            userService.decreaseCartItemQuantity(getCurrentUsername(authentication), productId);
            return ResponseEntity.ok(new MessageResponse("Item quantity decreased successfully! Item removed if quantity was 1."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/cart")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getCartItems(Authentication authentication) {
        try {
            Set<CartItem> items = userService.getCartItems(getCurrentUsername(authentication));
            return ResponseEntity.ok(items);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
