package com.sivalabs.bookmarks;

import com.sivalabs.bookmarks.services.OrderDetailDTO;
import com.sivalabs.bookmarks.services.OrderService;
import com.sivalabs.bookmarks.services.OrderSummaryService;
import com.sivalabs.bookmarks.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigDecimal;
import java.util.List;

@SpringBootApplication
public class Application implements CommandLineRunner {

    private final UserService userService;

    private final OrderService orderService;

    private final OrderSummaryService  orderSummaryService;

    public Application(UserService userService, OrderService orderService, OrderSummaryService orderSummaryService) {
        this.userService = userService;
        this.orderService = orderService;
        this.orderSummaryService = orderSummaryService;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Override
    public void run(String... args) {
        System.out.println("=== UserRepository Access Demo ===");
        userService.demonstrateUserAccess();

        System.out.println("=== OrderService Access Demo ===");
        List<OrderDetailDTO> orderDetails = orderService.getOrderDetails(1);
        orderDetails.forEach(System.out::println);

        System.out.println("=== OrderSummaryService Access Demo ===");
        orderSummaryService.getOrderSummary(1).forEach(System.out::println);

        System.out.println("=== OrderService Discount Demo ===");
        BigDecimal orderDiscount = orderService.getOrderDiscount(1);
        System.out.println("=== Order orderDiscount ===");
        System.out.println(orderDiscount);

    }

}
