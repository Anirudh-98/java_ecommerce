package com.ecommerce.service;

import com.ecommerce.model.Product;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.model.UserStatus;
import com.ecommerce.payload.request.ProductRequest;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SellerService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private User getSellerByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Seller not found with username: " + username));
        if (!user.getRoles().contains(Role.SELLER) || user.getStatus() != UserStatus.APPROVED) {
            throw new AccessDeniedException("User is not an approved seller.");
        }
        return user;
    }

    @Transactional
    public Product addProduct(ProductRequest productRequest, String sellerUsername) {
        User seller = getSellerByUsername(sellerUsername);

        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setSeller(seller); // Associate product with the seller
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long productId, ProductRequest productRequest, String sellerUsername) {
        User seller = getSellerByUsername(sellerUsername);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new AccessDeniedException("You are not authorized to update this product.");
        }

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long productId, String sellerUsername) {
        User seller = getSellerByUsername(sellerUsername);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this product.");
        }
        productRepository.deleteById(productId);
    }

    public List<Product> viewMyProducts(String sellerUsername) {
        User seller = getSellerByUsername(sellerUsername);
        return productRepository.findBySeller(seller);
    }

    public Product getMyProductById(Long productId, String sellerUsername) {
        User seller = getSellerByUsername(sellerUsername);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        if(!product.getSeller().getId().equals(seller.getId())){
            throw new AccessDeniedException("You are not authorized to view this product.");
        }
        return product;
    }
}
