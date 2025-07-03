package com.ecommerce.service;

import com.ecommerce.model.*;
import com.ecommerce.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceCartTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private Product product1;
    private Product product2;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRoles(Set.of(Role.USER));
        user.setStatus(UserStatus.ACTIVE);

        product1 = new Product();
        product1.setId(101L);
        product1.setName("Product 1");
        product1.setPrice(BigDecimal.TEN);

        product2 = new Product();
        product2.setId(102L);
        product2.setName("Product 2");
        product2.setPrice(BigDecimal.ONE);

        cart = new Cart(user);
        cart.setId(1L);
        cart.setItems(new HashSet<>()); // Initialize items set

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        // Ensure cartRepository.save returns the cart passed to it or a sensible mock
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

    }

    @Test
    void addProductToCart_newItem_success() {
        when(productRepository.findById(101L)).thenReturn(Optional.of(product1));
        // Simulate CartItemRepository save if it were directly called (though cascade handles it)
        // when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Cart resultCart = userService.addProductToCart("testuser", 101L, 2);

        assertEquals(1, resultCart.getItems().size());
        CartItem item = resultCart.getItems().iterator().next();
        assertEquals(product1, item.getProduct());
        assertEquals(2, item.getQuantity());
        // verify(cartItemRepository, times(1)).save(any(CartItem.class)); // if not relying purely on cascade for verification
        verify(cartRepository, times(1)).save(cart); // Cart is saved
    }

    @Test
    void addProductToCart_existingItem_increasesQuantity() {
        CartItem existingItem = new CartItem(cart, product1, 1);
        cart.addItem(existingItem); // Simulate item already in cart

        when(productRepository.findById(101L)).thenReturn(Optional.of(product1));
        // when(cartItemRepository.save(existingItem)).thenReturn(existingItem); // if save is called on existing item

        Cart resultCart = userService.addProductToCart("testuser", 101L, 3);

        assertEquals(1, resultCart.getItems().size());
        CartItem item = resultCart.getItems().iterator().next();
        assertEquals(product1, item.getProduct());
        assertEquals(4, item.getQuantity()); // 1 existing + 3 new
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void addProductToCart_quantityLessThanOne_throwsException() {
        when(productRepository.findById(101L)).thenReturn(Optional.of(product1));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.addProductToCart("testuser", 101L, 0);
        });
        assertEquals("Quantity must be at least 1.", exception.getMessage());
    }


    @Test
    void removeProductFromCart_itemExists_success() {
        CartItem itemToRemove = new CartItem(cart, product1, 2);
        cart.addItem(itemToRemove); // Item is in cart

        when(productRepository.findById(101L)).thenReturn(Optional.of(product1));
        // cartItemRepository.delete will be called
        doNothing().when(cartItemRepository).delete(any(CartItem.class));


        Cart resultCart = userService.removeProductFromCart("testuser", 101L);

        assertTrue(resultCart.getItems().isEmpty());
        verify(cartItemRepository, times(1)).delete(itemToRemove);
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void removeProductFromCart_itemNotExists_throwsException() {
        when(productRepository.findById(101L)).thenReturn(Optional.of(product1)); // Product exists
        // Cart is empty by default in this test path for this method

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.removeProductFromCart("testuser", 101L);
        });
        assertEquals("Product not found in cart.", exception.getMessage());
    }


    @Test
    void increaseCartItemQuantity_success() {
        CartItem item = new CartItem(cart, product1, 1);
        cart.addItem(item);
        when(productRepository.findById(101L)).thenReturn(Optional.of(product1));

        Cart resultCart = userService.increaseCartItemQuantity("testuser", 101L);

        assertEquals(2, resultCart.getItems().iterator().next().getQuantity());
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void increaseCartItemQuantity_itemNotFound_throwsException() {
        when(productRepository.findById(101L)).thenReturn(Optional.of(product1));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.increaseCartItemQuantity("testuser", 101L);
        });
        assertEquals("Product not found in cart to increase quantity.", exception.getMessage());
    }


    @Test
    void decreaseCartItemQuantity_quantityMoreThanOne_success() {
        CartItem item = new CartItem(cart, product1, 3);
        cart.addItem(item);
        when(productRepository.findById(101L)).thenReturn(Optional.of(product1));

        Cart resultCart = userService.decreaseCartItemQuantity("testuser", 101L);

        assertEquals(2, resultCart.getItems().iterator().next().getQuantity());
        verify(cartRepository, times(1)).save(cart);
        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }

    @Test
    void decreaseCartItemQuantity_quantityIsOne_removesItem() {
        CartItem item = new CartItem(cart, product1, 1);
        cart.addItem(item);
        when(productRepository.findById(101L)).thenReturn(Optional.of(product1));
        doNothing().when(cartItemRepository).delete(item);


        Cart resultCart = userService.decreaseCartItemQuantity("testuser", 101L);

        assertTrue(resultCart.getItems().isEmpty());
        verify(cartItemRepository, times(1)).delete(item);
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void decreaseCartItemQuantity_itemNotFound_throwsException() {
        when(productRepository.findById(101L)).thenReturn(Optional.of(product1));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.decreaseCartItemQuantity("testuser", 101L);
        });
        assertEquals("Product not found in cart to decrease quantity.", exception.getMessage());
    }


    @Test
    void getCartItems_success() {
        CartItem item1 = new CartItem(cart, product1, 2);
        CartItem item2 = new CartItem(cart, product2, 1);
        cart.addItem(item1);
        cart.addItem(item2);

        Set<CartItem> items = userService.getCartItems("testuser");

        assertEquals(2, items.size());
        assertTrue(items.contains(item1));
        assertTrue(items.contains(item2));
    }
}
