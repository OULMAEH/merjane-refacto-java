package com.nimbleways.springboilerplate.services.implementations;

import static com.nimbleways.springboilerplate.utils.ProductFixtures.expirable;
import static com.nimbleways.springboilerplate.utils.ProductFixtures.normal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@UnitTest
class ProductServiceTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private ProductService productService;

    @Test
    void decrementStock_decrementsAvailableAndSaves() {
        Product product = normal(30, 15);

        productService.decrementStock(product);

        assertEquals(29, product.getAvailable());
        verify(productRepository).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    void markUnavailable_setsAvailableToZeroAndSaves() {
        Product product = normal(30, 15);

        productService.markUnavailable(product);

        assertEquals(0, product.getAvailable());
        verify(productRepository).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    void notifyDelay_savesProductAndSendsDelayNotification() {
        Product product = normal(0, 15);

        productService.notifyDelay(product.getLeadTime(), product);

        assertEquals(0, product.getAvailable());
        assertEquals(15, product.getLeadTime());
        verify(productRepository).save(product);
        verify(notificationService).sendDelayNotification(15, "USB Cable");
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void notifyOutOfStock_sendsOutOfStockNotificationOnly() {
        Product product = normal(0, 15);

        productService.notifyOutOfStock(product);

        verify(notificationService).sendOutOfStockNotification("USB Cable");
        verifyNoMoreInteractions(notificationService);
        verifyNoInteractions(productRepository);
    }

    @Test
    void notifyExpiration_sendsExpirationNotificationWithExpiryDate() {
        LocalDate expiry = LocalDate.now().minusDays(1);
        Product product = expirable(5, 15, expiry);

        productService.notifyExpiration(product);

        verify(notificationService).sendExpirationNotification("Milk", expiry);
        verifyNoMoreInteractions(notificationService);
        verifyNoInteractions(productRepository);
    }
}
