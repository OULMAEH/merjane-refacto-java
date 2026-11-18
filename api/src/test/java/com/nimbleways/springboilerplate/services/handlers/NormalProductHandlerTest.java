package com.nimbleways.springboilerplate.services.handlers;

import static com.nimbleways.springboilerplate.utils.ProductFixtures.normal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.services.implementations.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NormalProductHandlerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private NormalProductHandler handler;

    @Test
    void supportsNormalProducts() {
        assertEquals(ProductType.NORMAL, handler.supportedType());
    }

    @Test
    void inStock_decrementsStock() {
        Product product = normal(30, 15);

        handler.handle(product);

        verify(productService).decrementStock(product);
        verifyNoMoreInteractions(productService);
    }

    @Test
    void outOfStockWithLeadTime_notifiesDelay() {
        Product product = normal(0, 10);

        handler.handle(product);

        verify(productService).notifyDelay(10, product);
        verifyNoMoreInteractions(productService);
    }

    @Test
    void outOfStockWithoutLeadTime_doesNothing() {
        Product product = normal(0, 0);

        handler.handle(product);

        verifyNoInteractions(productService);
    }
}
