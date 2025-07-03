package com.ecommerce.service;

import com.ecommerce.model.*;
import com.ecommerce.payload.response.AdminDashboardAnalyticsResponse;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceDashboardTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private AdminService adminService;

    private User approvedSeller1;
    private User approvedSeller2;
    private User pendingSeller;
    private User regularUser;

    @BeforeEach
    void setUp() {
        approvedSeller1 = new User();
        approvedSeller1.setId(1L);
        approvedSeller1.setUsername("seller1");
        approvedSeller1.setStatus(UserStatus.APPROVED);
        Set<Role> seller1Roles = new HashSet<>();
        seller1Roles.add(Role.USER);
        seller1Roles.add(Role.SELLER);
        approvedSeller1.setRoles(seller1Roles);

        approvedSeller2 = new User();
        approvedSeller2.setId(2L);
        approvedSeller2.setUsername("seller2");
        approvedSeller2.setStatus(UserStatus.APPROVED);
        Set<Role> seller2Roles = new HashSet<>();
        seller2Roles.add(Role.USER);
        seller2Roles.add(Role.SELLER);
        approvedSeller2.setRoles(seller2Roles);

        pendingSeller = new User();
        pendingSeller.setId(3L);
        pendingSeller.setUsername("pendingSeller");
        pendingSeller.setStatus(UserStatus.PENDING);
        pendingSeller.setRoles(new HashSet<>(Set.of(Role.USER, Role.SELLER))); // Might have SELLER role but PENDING

        regularUser = new User();
        regularUser.setId(4L);
        regularUser.setUsername("user1");
        regularUser.setStatus(UserStatus.ACTIVE);
        regularUser.setRoles(new HashSet<>(Set.of(Role.USER)));
    }

    @Test
    void getDashboardAnalytics_calculatesCorrectly() {
        when(productRepository.count()).thenReturn(150L);

        List<User> allUserList = Arrays.asList(approvedSeller1, approvedSeller2, pendingSeller, regularUser);
        when(userRepository.findAll()).thenReturn(allUserList); // Used for counting approved sellers
        when(userRepository.count()).thenReturn((long) allUserList.size()); // Used for totalUsers

        when(orderRepository.sumTotalAmountByStatus(OrderStatus.COMPLETED)).thenReturn(BigDecimal.valueOf(12500.75));

        AdminDashboardAnalyticsResponse analytics = adminService.getDashboardAnalytics();

        assertNotNull(analytics);
        assertEquals(150L, analytics.getTotalProducts());
        assertEquals(2L, analytics.getTotalSellers()); // Only approved sellers
        assertEquals(BigDecimal.valueOf(12500.75), analytics.getTotalRevenue());
        assertEquals(4L, analytics.getTotalUsers()); // Total users from the list
    }

    @Test
    void getDashboardAnalytics_noCompletedOrders_revenueIsZero() {
        when(productRepository.count()).thenReturn(50L);
        List<User> allUserList = Arrays.asList(approvedSeller1);
        when(userRepository.findAll()).thenReturn(allUserList);
        when(userRepository.count()).thenReturn((long) allUserList.size());
        when(orderRepository.sumTotalAmountByStatus(OrderStatus.COMPLETED)).thenReturn(null); // Simulate no completed orders

        AdminDashboardAnalyticsResponse analytics = adminService.getDashboardAnalytics();

        assertNotNull(analytics);
        assertEquals(50L, analytics.getTotalProducts());
        assertEquals(1L, analytics.getTotalSellers());
        assertEquals(BigDecimal.ZERO, analytics.getTotalRevenue());
        assertEquals(1L, analytics.getTotalUsers());
    }

    @Test
    void getDashboardAnalytics_noApprovedSellers_countIsZero() {
        when(productRepository.count()).thenReturn(20L);
        List<User> allUserList = Arrays.asList(pendingSeller, regularUser); // No approved sellers
        when(userRepository.findAll()).thenReturn(allUserList);
        when(userRepository.count()).thenReturn((long) allUserList.size());
        when(orderRepository.sumTotalAmountByStatus(OrderStatus.COMPLETED)).thenReturn(BigDecimal.valueOf(100.00));

        AdminDashboardAnalyticsResponse analytics = adminService.getDashboardAnalytics();

        assertNotNull(analytics);
        assertEquals(20L, analytics.getTotalProducts());
        assertEquals(0L, analytics.getTotalSellers());
        assertEquals(BigDecimal.valueOf(100.00), analytics.getTotalRevenue());
        assertEquals(2L, analytics.getTotalUsers());
    }
}
