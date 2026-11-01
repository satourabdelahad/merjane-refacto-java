package com.nimbleways.springboilerplate.services.handlers;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
public class ExpirableProductHandler implements ProductOrderHandler {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final Clock clock;

    public ExpirableProductHandler(ProductRepository productRepository,
                                   NotificationService notificationService,
                                   Clock clock) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    @Override
    public ProductType supportedType() {
        return ProductType.EXPIRABLE;
    }

    @Override
    public void handle(Product product) {
        LocalDate today = LocalDate.now(clock);

        if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(today)) {
            product.setAvailable(product.getAvailable() - 1);
            productRepository.save(product);
        } else {

            notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
            product.setAvailable(0);
            productRepository.save(product);
        }
    }
}
