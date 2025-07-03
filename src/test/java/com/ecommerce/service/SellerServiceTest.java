package com.ecommerce.service;

import com.ecommerce.model.*;
import com.ecommerce.payload.request.ProductRequest;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SellerService sellerService;

    private User approvedSeller;
    private User pendingSeller;
    private User regularUser;
    private ProductRequest productRequest;
    private Product product;

    @BeforeEach
    void setUp() {
        approvedSeller = new User();
        approvedSeller.setId(1L);
        approvedSeller.setUsername("seller1");
        approvedSeller.setStatus(UserStatus.APPROVED);
        Set<Role> sellerRoles = new HashSet<>();
        sellerRoles.add(Role.USER);
        sellerRoles.add(Role.SELLER);
        approvedSeller.setRoles(sellerRoles);

        pendingSeller = new User();
        pendingSeller.setId(2L);
        pendingSeller.setUsername("pendingSeller");
        pendingSeller.setStatus(UserStatus.PENDING);
        pendingSeller.setRoles(new HashSet<>(Set.of(Role.USER)));


        regularUser = new User();
        regularUser.setId(3L);
        regularUser.setUsername("user1");
        regularUser.setStatus(UserStatus.ACTIVE);
        regularUser.setRoles(new HashSet<>(Set.of(Role.USER)));

        productRequest = new ProductRequest();
        productRequest.setName("Test Product");
        productRequest.setDescription("Test Description");
        productRequest.setPrice(BigDecimal.valueOf(100.00));

        product = new Product();
        product.setId(101L);
        product.setName("Test Product");
        product.setSeller(approvedSeller);
        product.setPrice(BigDecimal.valueOf(100.00));
    }

    @Test
    void addProduct_byApprovedSeller_success() {
        when(userRepository.findByUsername("seller1")).thenReturn(Optional.of(approvedSeller));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(102L); // Simulate save
            return p;
        });

        Product newProduct = sellerService.addProduct(productRequest, "seller1");

        assertNotNull(newProduct);
        assertEquals("Test Product", newProduct.getName());
        assertEquals(approvedSeller, newProduct.getSeller());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void addProduct_byPendingSeller_throwsAccessDenied() {
        when(userRepository.findByUsername("pendingSeller")).thenReturn(Optional.of(pendingSeller));

        Exception exception = assertThrows(AccessDeniedException.class, () -> {
            sellerService.addProduct(productRequest, "pendingSeller");
        });
        assertEquals("User is not an approved seller.", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void addProduct_byRegularUser_throwsAccessDenied() {
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(regularUser));

        Exception exception = assertThrows(AccessDeniedException.class, () -> {
            sellerService.addProduct(productRequest, "user1");
        });
        assertEquals("User is not an approved seller.", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_byOwnerSeller_success() {
        when(userRepository.findByUsername("seller1")).thenReturn(Optional.of(approvedSeller));
        when(productRepository.findById(101L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product); // Return the same product instance

        ProductRequest updatedRequest = new ProductRequest();
        updatedRequest.setName("Updated Product Name");
        updatedRequest.setPrice(BigDecimal.valueOf(120.00));

        Product updatedProduct = sellerService.updateProduct(101L, updatedRequest, "seller1");

        assertNotNull(updatedProduct);
        assertEquals("Updated Product Name", updatedProduct.getName());
        assertEquals(BigDecimal.valueOf(120.00), updatedProduct.getPrice());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void updateProduct_byNonOwnerSeller_throwsAccessDenied() {
        User anotherSeller = new User();
        anotherSeller.setId(4L);
        anotherSeller.setUsername("seller2");
        anotherSeller.setStatus(UserStatus.APPROVED);
        anotherSeller.setRoles(new HashSet<>(Set.of(Role.USER, Role.SELLER)));

        when(userRepository.findByUsername("seller2")).thenReturn(Optional.of(anotherSeller));
        when(productRepository.findById(101L)).thenReturn(Optional.of(product)); // product owned by approvedSeller (ID 1)

        ProductRequest updatedRequest = new ProductRequest();
        updatedRequest.setName("Attempted Update");

        Exception exception = assertThrows(AccessDeniedException.class, () -> {
            sellerService.updateProduct(101L, updatedRequest, "seller2");
        });
        assertEquals("You are not authorized to update this product.", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_byOwnerSeller_success() {
        when(userRepository.findByUsername("seller1")).thenReturn(Optional.of(approvedSeller));
        when(productRepository.findById(101L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).deleteById(101L);

        assertDoesNotThrow(() -> sellerService.deleteProduct(101L, "seller1"));
        verify(productRepository, times(1)).deleteById(101L);
    }

    @Test
    void deleteProduct_byNonOwnerSeller_throwsAccessDenied() {
        User anotherSeller = new User(); // Different from product.getSeller()
        anotherSeller.setId(5L);
        anotherSeller.setUsername("seller2");
        anotherSeller.setRoles(Set.of(Role.SELLER));
        anotherSeller.setStatus(UserStatus.APPROVED);

        when(userRepository.findByUsername("seller2")).thenReturn(Optional.of(anotherSeller));
        when(productRepository.findById(101L)).thenReturn(Optional.of(product)); // Product owned by 'approvedSeller'

        Exception exception = assertThrows(AccessDeniedException.class, () -> {
            sellerService.deleteProduct(101L, "seller2");
        });

        assertEquals("You are not authorized to delete this product.", exception.getMessage());
        verify(productRepository, never()).deleteById(anyLong());
    }


    @Test
    void viewMyProducts_success() {
        List<Product> productList = new ArrayList<>();
        productList.add(product);
        when(userRepository.findByUsername("seller1")).thenReturn(Optional.of(approvedSeller));
        when(productRepository.findBySeller(approvedSeller)).thenReturn(productList);

        List<Product> result = sellerService.viewMyProducts("seller1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        verify(productRepository, times(1)).findBySeller(approvedSeller);
    }
}
