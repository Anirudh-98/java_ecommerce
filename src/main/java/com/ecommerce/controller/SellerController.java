package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.payload.request.ProductRequest;
import com.ecommerce.payload.response.MessageResponse;
import com.ecommerce.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/seller")
@PreAuthorize("hasRole('SELLER')") // Base authorization for the controller
public class SellerController {

    @Autowired
    private SellerService sellerService;

    private String getCurrentUsername(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    @PostMapping("/products/add")
    public ResponseEntity<?> addProduct(@Valid @RequestBody ProductRequest productRequest, Authentication authentication) {
        try {
            String username = getCurrentUsername(authentication);
            Product product = sellerService.addProduct(productRequest, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(product);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error adding product: " + e.getMessage()));
        }
    }

    @PutMapping("/products/update/{productId}")
    public ResponseEntity<?> updateProduct(@PathVariable Long productId,
                                           @Valid @RequestBody ProductRequest productRequest,
                                           Authentication authentication) {
        try {
            String username = getCurrentUsername(authentication);
            Product updatedProduct = sellerService.updateProduct(productId, productRequest, username);
            return ResponseEntity.ok(updatedProduct);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse("Error: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/products/delete/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId, Authentication authentication) {
        try {
            String username = getCurrentUsername(authentication);
            sellerService.deleteProduct(productId, username);
            return ResponseEntity.ok(new MessageResponse("Product deleted successfully!"));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse("Error: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/products")
    public ResponseEntity<?> viewMyProducts(Authentication authentication) {
        try {
            String username = getCurrentUsername(authentication);
            List<Product> products = sellerService.viewMyProducts(username);
            return ResponseEntity.ok(products);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse("Error: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<?> getMyProductById(@PathVariable Long productId, Authentication authentication) {
        try {
            String username = getCurrentUsername(authentication);
            Product product = sellerService.getMyProductById(productId, username);
            return ResponseEntity.ok(product);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse("Error: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
