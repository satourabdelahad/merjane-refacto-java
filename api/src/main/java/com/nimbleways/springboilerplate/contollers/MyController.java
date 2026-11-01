package com.nimbleways.springboilerplate.contollers;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.ProductService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class MyController {
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    Clock clock;

    @PostMapping("{orderId}/processOrder")
    @ResponseStatus(HttpStatus.OK)
    public ProcessOrderResponse processOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId).get();
        System.out.println(order);
        List<Long> ids = new ArrayList<>();
        ids.add(orderId);
        Set<Product> products = order.getItems();
        for (Product product : products) {
            if (product.getType().equals("NORMAL")) {
                if (product.getAvailable() > 0) {
                    product.setAvailable(product.getAvailable() - 1);
                    productRepository.save(product);
                } else {
                    int leadTime = product.getLeadTime();
                    if (leadTime > 0) {
                        productService.notifyDelay(leadTime, product);
                    }
                }
            } else if (product.getType().equals("SEASONAL")) {
                // Add new season rules
                if ((LocalDate.now(clock).isAfter(product.getSeasonStartDate()) && LocalDate.now(clock).isBefore(product.getSeasonEndDate())
                        && product.getAvailable() > 0)) {
                    product.setAvailable(product.getAvailable() - 1);
                    productRepository.save(product);
                } else {
                    productService.handleSeasonalProduct(product);
                }
            } else if (product.getType().equals("EXPIRABLE")) {
                if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(LocalDate.now(clock))) {
                    product.setAvailable(product.getAvailable() - 1);
                    productRepository.save(product);
                } else {
                    productService.handleExpiredProduct(product);
                }
            }
        }

        return new ProcessOrderResponse(order.getId());
    }
}
