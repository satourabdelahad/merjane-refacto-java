package com.nimbleways.springboilerplate.services.handlers;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import com.nimbleways.springboilerplate.services.implementations.ProductService;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
public class SeasonalProductHandler implements ProductOrderHandler {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final ProductService productService;
    private final Clock clock;

    public SeasonalProductHandler(ProductRepository productRepository,
                                  NotificationService notificationService,
                                  ProductService productService,
                                  Clock clock) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
        this.productService = productService;
        this.clock = clock;
    }

    @Override
    public ProductType supportedType() {
        return ProductType.SEASONAL;
    }

    @Override
    public void handle(Product product) {
        LocalDate today = LocalDate.now(clock);

        // Legacy: strict bounds, the product is not sold on the first or last day of the season
        boolean inSeason = today.isAfter(product.getSeasonStartDate())
                && today.isBefore(product.getSeasonEndDate());

        if (inSeason && product.getAvailable() > 0) {
            product.setAvailable(product.getAvailable() - 1);
            productRepository.save(product);
        } else if (today.plusDays(product.getLeadTime()).isAfter(product.getSeasonEndDate())) {
            notificationService.sendOutOfStockNotification(product.getName());
            product.setAvailable(0);
            productRepository.save(product);
        } else if (product.getSeasonStartDate().isAfter(today)) {
            notificationService.sendOutOfStockNotification(product.getName());
            productRepository.save(product);
        } else {
            productService.notifyDelay(product.getLeadTime(), product);
        }
    }
}
