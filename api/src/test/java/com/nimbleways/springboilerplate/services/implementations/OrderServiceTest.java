package com.nimbleways.springboilerplate.services.implementations;

import static com.nimbleways.springboilerplate.utils.ProductFixtures.normal;
import static com.nimbleways.springboilerplate.utils.ProductFixtures.seasonal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.exceptions.OrderNotFoundException;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.services.handlers.ProductHandler;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductHandler normalHandler;
    @Mock
    private ProductHandler seasonalHandler;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        when(normalHandler.supportedType()).thenReturn(ProductType.NORMAL);
        when(seasonalHandler.supportedType()).thenReturn(ProductType.SEASONAL);
        orderService = new OrderService(orderRepository, List.of(normalHandler, seasonalHandler));
    }

    @Test
    void dispatchesEachItemToTheHandlerOfItsType() {
        Product normalProduct = normal(10, 5);
        Product seasonalProduct = seasonal(10, 5, LocalDate.now().minusDays(1), LocalDate.now().plusDays(10));
        Order order = new Order(1L, new HashSet<>(Set.of(normalProduct, seasonalProduct)));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Long result = orderService.processOrder(1L);

        assertEquals(1L, result);
        verify(normalHandler).handle(normalProduct);
        verify(seasonalHandler).handle(seasonalProduct);
    }

    @Test
    void unknownOrder_throwsOrderNotFound() {
        when(orderRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.processOrder(42L));
    }
}
