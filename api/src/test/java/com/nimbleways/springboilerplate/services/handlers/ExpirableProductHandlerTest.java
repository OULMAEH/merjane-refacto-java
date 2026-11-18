package com.nimbleways.springboilerplate.services.handlers;

import static com.nimbleways.springboilerplate.utils.ProductFixtures.expirable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.services.implementations.ProductService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpirableProductHandlerTest {

    private final LocalDate today = LocalDate.now();

    @Mock
    private ProductService productService;

    @InjectMocks
    private ExpirableProductHandler handler;

    @Test
    void supportsExpirableProducts() {
        assertEquals(ProductType.EXPIRABLE, handler.supportedType());
    }

    @Test
    void inStockAndNotExpired_decrementsStock() {
        Product product = expirable(30, 15, today.plusDays(26));

        handler.handle(product);

        verify(productService).decrementStock(product);
        verifyNoMoreInteractions(productService);
    }

    @Test
    void expired_notifiesExpirationAndMarksUnavailable() {
        Product product = expirable(6, 90, today.minusDays(2));

        handler.handle(product);

        verify(productService).notifyExpiration(product);
        verify(productService).markUnavailable(product);
        verifyNoMoreInteractions(productService);
    }

    @Test
    void expiringToday_isConsideredExpired() {
        Product product = expirable(6, 90, today);

        handler.handle(product);

        verify(productService).notifyExpiration(product);
        verify(productService).markUnavailable(product);
        verifyNoMoreInteractions(productService);
    }

    @Test
    void outOfStockButNotExpired_notifiesExpirationAndMarksUnavailable() {
        Product product = expirable(0, 15, today.plusDays(10));

        handler.handle(product);

        verify(productService).notifyExpiration(product);
        verify(productService).markUnavailable(product);
        verifyNoMoreInteractions(productService);
    }
}
