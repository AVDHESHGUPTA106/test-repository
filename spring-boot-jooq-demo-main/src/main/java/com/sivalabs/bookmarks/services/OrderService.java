package com.sivalabs.bookmarks.services;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.sivalabs.bookmarks.jooq.Tables.*;

// Spring Boot Service using jOOQ
@Service
public class OrderService {

    private final DSLContext dsl;

    public OrderService(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<OrderDetailDTO> getOrderDetails(Integer orderId) {
        return dsl.select(
                ORDERS.ORDER_ID,
                CUSTOMERS.CUSTOMER_NAME,
                PRODUCTS.PRODUCT_NAME,
                ORDER_ITEMS.QUANTITY,
                PRODUCTS.PRICE,
                DISCOUNT_RULES.DISCOUNT_PERCENT
            )
            .from(ORDERS)
            .join(CUSTOMERS).on(ORDERS.CUSTOMER_ID.eq(CUSTOMERS.CUSTOMER_ID))
            .join(ORDER_ITEMS).on(ORDERS.ORDER_ID.eq(ORDER_ITEMS.ORDER_ID))
            .join(PRODUCTS).on(ORDER_ITEMS.PRODUCT_ID.eq(PRODUCTS.PRODUCT_ID))
            .join(DISCOUNT_RULES).on(DISCOUNT_RULES.CUSTOMER_TYPE.eq(CUSTOMERS.CUSTOMER_TYPE))
                .and(DISCOUNT_RULES.PRODUCT_CATEGORY.eq(PRODUCTS.CATEGORY))
                .and(ORDER_ITEMS.QUANTITY.between(DISCOUNT_RULES.MIN_QTY, DISCOUNT_RULES.MAX_QTY))
            .where(ORDERS.ORDER_ID.eq(orderId))
            .fetchInto(OrderDetailDTO.class); // Maps directly to a Java Record
    }

    public BigDecimal getOrderDiscount(Integer orderId) {
        return dsl.select(
                DISCOUNT_RULES.DISCOUNT_PERCENT
            )
            .from(ORDERS)
            .join(CUSTOMERS).on(ORDERS.CUSTOMER_ID.eq(CUSTOMERS.CUSTOMER_ID))
            .join(ORDER_ITEMS).on(ORDERS.ORDER_ID.eq(ORDER_ITEMS.ORDER_ID))
            .join(PRODUCTS).on(ORDER_ITEMS.PRODUCT_ID.eq(PRODUCTS.PRODUCT_ID))
            .join(DISCOUNT_RULES).on(DISCOUNT_RULES.CUSTOMER_TYPE.eq(CUSTOMERS.CUSTOMER_TYPE))
                .and(DISCOUNT_RULES.PRODUCT_CATEGORY.eq(PRODUCTS.CATEGORY))
                .and(ORDER_ITEMS.QUANTITY.between(DISCOUNT_RULES.MIN_QTY, DISCOUNT_RULES.MAX_QTY))
            .where(ORDERS.ORDER_ID.eq(orderId))
            .limit(1)
            .fetchOneInto(BigDecimal.class);
    }

}