package com.nimbleways.springboilerplate.services.handlers;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.services.implementations.ProductService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExpirableProductHandler implements ProductHandler {

    private final ProductService productService;

    @Override
    public ProductType supportedType() {
        return ProductType.EXPIRABLE;
    }

    @Override
    public void handle(Product product) {
        if (product.isInStock() && !product.isExpired(LocalDate.now())) {
            productService.decrementStock(product);
        } else {
            productService.notifyExpiration(product);
            productService.markUnavailable(product);
        }
    }
}
