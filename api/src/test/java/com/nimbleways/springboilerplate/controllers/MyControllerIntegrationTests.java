package com.nimbleways.springboilerplate.controllers;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.Assert.assertEquals;

// import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Specify the controller class you want to test
// This indicates to spring boot to only load UsersController into the context
// Which allows a better performance and needs to do less mocks
@SpringBootTest
@AutoConfigureMockMvc
public class MyControllerIntegrationTests {
        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private NotificationService notificationService;

        @Autowired
        private OrderRepository orderRepository;

        @Autowired
        private ProductRepository productRepository;

         // ---------- NORMAL ----------
        @Test
        void should_decrement_stock_when_normal_product_is_in_stock() throws Exception {
            // GIVEN : un seul produit NORMAL avec 30 en stock
            Product product = productRepository.save(
                    new Product(null, 15, 30, "NORMAL", "USB C", null, null, null));
            Order order = orderRepository.save(createOrder(Set.of(product)));

            // WHEN
            mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // THEN
            Product result = productRepository.findById(product.getId()).get();

            Assertions.assertEquals(29, result.getAvailable());

            Mockito.verifyNoInteractions(notificationService);

        }
        @Test
        void should_notify_delay_when_normal_product_is_out_of_stock() throws Exception {
            Product product = productRepository.save(
                    new Product(null, 10, 0, "NORMAL", "USB Hub", null, null, null));

            Product result = processSingleProductOrder(product);

            Assertions.assertEquals(0, result.getAvailable());
            Assertions.assertEquals(10, result.getLeadTime());
            Mockito.verify(notificationService).sendDelayNotification(10, "USB Hub");
            Mockito.verifyNoMoreInteractions(notificationService);
        }

        @Test
        void should_do_nothing_when_normal_product_is_out_of_stock_and_lead_time_is_zero() throws Exception {
            // Comportement legacy : aucune notification on fait rien si leadTime = 0
            Product product = productRepository.save(
                    new Product(null, 0, 0, "NORMAL", "USB C", null, null, null));

            Product result = processSingleProductOrder(product);

            Assertions.assertEquals(0, result.getAvailable());
            Mockito.verifyNoInteractions(notificationService);
        }

        @Test
        public void processOrderShouldReturn() throws Exception {
                List<Product> allProducts = createProducts();
                Set<Product> orderItems = new HashSet<Product>(allProducts);
                Order order = createOrder(orderItems);
                productRepository.saveAll(allProducts);
                order = orderRepository.save(order);
                mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                                .contentType("application/json"))
                                .andExpect(status().isOk());
                Order resultOrder = orderRepository.findById(order.getId()).get();
                assertEquals(resultOrder.getId(), order.getId());
        }

        private Product processSingleProductOrder(Product product) throws Exception {
            Order order = orderRepository.save(createOrder(Set.of(product)));
            mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
            return productRepository.findById(product.getId()).get();
        }

        private static Order createOrder(Set<Product> products) {
                Order order = new Order();
                order.setItems(products);
                return order;
        }

        private static List<Product> createProducts() {
                List<Product> products = new ArrayList<>();
                products.add(new Product(null, 15, 30, "NORMAL", "USB Cable", null, null, null));
                products.add(new Product(null, 10, 0, "NORMAL", "USB Dongle", null, null, null));
                products.add(new Product(null, 15, 30, "EXPIRABLE", "Butter", LocalDate.now().plusDays(26), null,
                                null));
                products.add(new Product(null, 90, 6, "EXPIRABLE", "Milk", LocalDate.now().minusDays(2), null, null));
                products.add(new Product(null, 15, 30, "SEASONAL", "Watermelon", null, LocalDate.now().minusDays(2),
                                LocalDate.now().plusDays(58)));
                products.add(new Product(null, 15, 30, "SEASONAL", "Grapes", null, LocalDate.now().plusDays(180),
                                LocalDate.now().plusDays(240)));
                return products;
        }
}
