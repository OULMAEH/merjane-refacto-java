package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.exceptions.OrderNotFoundException;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.services.handlers.ProductHandler;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final Map<ProductType, ProductHandler> handlers = new EnumMap<>(ProductType.class);

    public OrderService(OrderRepository orderRepository, List<ProductHandler> productHandlers) {
        this.orderRepository = orderRepository;
        productHandlers.forEach(handler -> handlers.put(handler.supportedType(), handler));
    }

    @Transactional
    public Long processOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);

        order.getItems().forEach(this::handle);

        return order.getId();
    }

    private void handle(Product product) {
        ProductHandler handler = handlers.get(product.getType());
        if (handler == null) {
            throw new IllegalStateException("No handler for product type " + product.getType());
        }
        handler.handle(product);
    }
}
