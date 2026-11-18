package com.nimbleways.springboilerplate.controllers;

import static com.nimbleways.springboilerplate.utils.ProductFixtures.expirable;
import static com.nimbleways.springboilerplate.utils.ProductFixtures.normal;
import static com.nimbleways.springboilerplate.utils.ProductFixtures.seasonal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTests {
    private static final LocalDate TODAY = LocalDate.now();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    // ---------- NORMAL ----------

    @Test
    void normalProductInStock_decrementsStock() throws Exception {
        Product result = processOrderWith(normal(30, 15));

        assertEquals(29, result.getAvailable());
        verifyNoInteractions(notificationService);
    }

    @Test
    void normalProductOutOfStock_notifiesDelay() throws Exception {
        Product result = processOrderWith(normal(0, 10));

        assertEquals(0, result.getAvailable());
        verify(notificationService).sendDelayNotification(10, "USB Cable");
    }

    @Test
    void normalProductOutOfStockWithoutLeadTime_doesNothing() throws Exception {
        Product result = processOrderWith(normal(0, 0));

        assertEquals(0, result.getAvailable());
        verifyNoInteractions(notificationService);
    }

    // ---------- SEASONAL ----------

    @Test
    void seasonalProductInSeasonAndInStock_decrementsStock() throws Exception {
        Product result = processOrderWith(seasonal(30, 15, TODAY.minusDays(2), TODAY.plusDays(58)));

        assertEquals(29, result.getAvailable());
        verifyNoInteractions(notificationService);
    }

    @Test
    void seasonalProductOutOfStock_restockWithinSeason_notifiesDelay() throws Exception {
        Product result = processOrderWith(seasonal(0, 15, TODAY.minusDays(2), TODAY.plusDays(58)));

        assertEquals(0, result.getAvailable());
        verify(notificationService).sendDelayNotification(15, "Watermelon");
    }

    @Test
    void seasonalProduct_restockAfterSeasonEnd_isUnavailable() throws Exception {
        Product result = processOrderWith(seasonal(30, 15, TODAY.minusDays(60), TODAY.minusDays(1)));

        assertEquals(0, result.getAvailable());
        verify(notificationService).sendOutOfStockNotification("Watermelon");
    }

    @Test
    void seasonalProduct_seasonNotStarted_notifiesOutOfStock() throws Exception {
        Product result = processOrderWith(seasonal(30, 15, TODAY.plusDays(180), TODAY.plusDays(240)));

        assertEquals(30, result.getAvailable());
        verify(notificationService).sendOutOfStockNotification("Watermelon");
    }

    // ---------- EXPIRABLE ----------

    @Test
    void expirableProductNotExpiredAndInStock_decrementsStock() throws Exception {
        Product result = processOrderWith(expirable(30, 15, TODAY.plusDays(26)));

        assertEquals(29, result.getAvailable());
        verifyNoInteractions(notificationService);
    }

    @Test
    void expirableProductExpired_notifiesExpirationAndIsUnavailable() throws Exception {
        LocalDate expiry = TODAY.minusDays(2);
        Product result = processOrderWith(expirable(6, 90, expiry));

        assertEquals(0, result.getAvailable());
        verify(notificationService).sendExpirationNotification("Milk", expiry);
    }

    @Test
    void expirableProductOutOfStock_notifiesExpiration() throws Exception {
        LocalDate expiry = TODAY.plusDays(10);
        Product result = processOrderWith(expirable(0, 15, expiry));

        assertEquals(0, result.getAvailable());
        verify(notificationService).sendExpirationNotification("Milk", expiry);
    }

    // ---------- ORDER ----------

    @Test
    void orderWithMixedProducts_appliesTheRuleOfEachProductType() throws Exception {
        LocalDate expiry = TODAY.minusDays(2);
        Product normal = productRepository.save(normal(30, 15));
        Product seasonal = productRepository.save(seasonal(30, 15, TODAY.minusDays(2), TODAY.plusDays(58)));
        Product expired = productRepository.save(expirable(6, 90, expiry));

        processOrder(Set.of(normal, seasonal, expired));

        assertEquals(29, reload(normal).getAvailable());
        assertEquals(29, reload(seasonal).getAvailable());
        assertEquals(0, reload(expired).getAvailable());
        verify(notificationService).sendExpirationNotification("Milk", expiry);
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void unknownOrder_returnsNotFound() throws Exception {
        mockMvc.perform(post("/orders/{orderId}/processOrder", 999_999L))
                .andExpect(status().isNotFound());
    }

    // ---------- helpers ----------

    private Product processOrderWith(Product product) throws Exception {
        Product saved = productRepository.save(product);
        processOrder(Set.of(saved));
        return reload(saved);
    }

    private void processOrder(Set<Product> products) throws Exception {
        Order order = new Order();
        order.setItems(new HashSet<>(products));
        order = orderRepository.save(order);

        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()));
    }

    private Product reload(Product product) {
        return productRepository.findById(product.getId()).orElseThrow();
    }
}
