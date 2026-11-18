package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public void decrementStock(Product product) {
        product.decrementAvailable();
        productRepository.save(product);
    }

    public void markUnavailable(Product product) {
        product.setAvailable(0);
        productRepository.save(product);
    }

    public void notifyDelay(int leadTime, Product product) {
        product.setLeadTime(leadTime);
        productRepository.save(product);
        notificationService.sendDelayNotification(leadTime, product.getName());
    }

    public void notifyOutOfStock(Product product) {
        notificationService.sendOutOfStockNotification(product.getName());
    }

    public void notifyExpiration(Product product) {
        notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
    }
}