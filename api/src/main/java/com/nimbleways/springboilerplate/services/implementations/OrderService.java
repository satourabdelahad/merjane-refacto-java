package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.services.handlers.ProductOrderHandler;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final Map<ProductType, ProductOrderHandler> handlersByType = new EnumMap<>(ProductType.class);


    public OrderService(OrderRepository orderRepository, List<ProductOrderHandler> handlers) {
        this.orderRepository = orderRepository;
        handlers.forEach(handler -> handlersByType.put(handler.supportedType(), handler));
    }

    public Long processOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).get();
        for (Product product : order.getItems()) {
            ProductOrderHandler handler = handlersByType.get(product.getType());

            if (handler != null) {
                handler.handle(product);
            }
        }
        return order.getId();
    }
}
