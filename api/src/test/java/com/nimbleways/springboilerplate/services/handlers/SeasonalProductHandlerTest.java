package com.nimbleways.springboilerplate.services.handlers;

import static com.nimbleways.springboilerplate.utils.ProductFixtures.seasonal;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.services.implementations.ProductService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SeasonalProductHandlerTest {

    private final LocalDate today = LocalDate.now();

    @Mock
    private ProductService productService;

    @InjectMocks
    private SeasonalProductHandler handler;

    @Test
    void inSeasonAndInStock_decrementsStock() {
        Product product = seasonal(30, 15, today.minusDays(2), today.plusDays(58));

        handler.handle(product);

        verify(productService).decrementStock(product);
        verifyNoMoreInteractions(productService);
    }

    @Test
    void restockAfterSeasonEnd_notifiesOutOfStockAndMarksUnavailable() {
        Product product = seasonal(0, 15, today.minusDays(2), today.plusDays(5));

        handler.handle(product);

        verify(productService).notifyOutOfStock(product);
        verify(productService).markUnavailable(product);
        verifyNoMoreInteractions(productService);
    }

    @Test
    void seasonNotStarted_notifiesOutOfStock() {
        Product product = seasonal(30, 15, today.plusDays(180), today.plusDays(240));

        handler.handle(product);

        verify(productService).notifyOutOfStock(product);
        verifyNoMoreInteractions(productService);
    }

    @Test
    void outOfStockButRestockWithinSeason_notifiesDelay() {
        Product product = seasonal(0, 15, today.minusDays(2), today.plusDays(58));

        handler.handle(product);

        verify(productService).notifyDelay(15, product);
        verifyNoMoreInteractions(productService);
    }
}
