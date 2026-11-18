package com.nimbleways.springboilerplate.utils;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;

import java.time.LocalDate;

public final class ProductFixtures {

    private ProductFixtures() {
    }

    public static Product normal(int available, int leadTime) {
        return new Product(null, leadTime, available, ProductType.NORMAL, "USB Cable", null, null, null);
    }

    public static Product seasonal(int available, int leadTime, LocalDate seasonStart, LocalDate seasonEnd) {
        return new Product(null, leadTime, available, ProductType.SEASONAL, "Watermelon", null, seasonStart, seasonEnd);
    }

    public static Product expirable(int available, int leadTime, LocalDate expiryDate) {
        return new Product(null, leadTime, available, ProductType.EXPIRABLE, "Milk", expiryDate, null, null);
    }
}
