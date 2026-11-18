package com.nimbleways.springboilerplate.services.handlers;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.services.implementations.ProductService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeasonalProductHandler implements ProductHandler {

    private final ProductService productService;

    @Override
    public ProductType supportedType() {
        return ProductType.SEASONAL;
    }

    @Override
    public void handle(Product product) {
        LocalDate today = LocalDate.now();

        if (product.isInSeason(today) && product.isInStock()) {
            productService.decrementStock(product);
        } else if (product.isRestockAfterSeasonEnd(today)) {
            productService.notifyOutOfStock(product);
            productService.markUnavailable(product);
        } else if (product.isSeasonNotStarted(today)) {
            productService.notifyOutOfStock(product);
        } else {
            productService.notifyDelay(product.getLeadTime(), product);
        }
    }
}
