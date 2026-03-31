package com.sivalabs.bookmarks.services;

import com.sivalabs.bookmarks.jooq.tables.Orders;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.sivalabs.bookmarks.jooq.Routines.fnCalculateDiscount;
import static com.sivalabs.bookmarks.jooq.Tables.*;


@Service
public class OrderSummaryService {

    private final DSLContext dsl;

    public OrderSummaryService(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<OrderSummaryDTO> getOrderSummary(Integer orderId) {
        return dsl.select(
                        ORDERS.ORDER_ID,
                        PRODUCTS.PRODUCT_NAME,
                        ORDER_ITEMS.QUANTITY,
                        PRODUCTS.PRICE,
                        // Total Amount calculation
                        ORDER_ITEMS.QUANTITY.multiply(PRODUCTS.PRICE).as("total_amount"),
                
                // Calling the DB function via jOOQ generated code
                fnCalculateDiscount(
                        CUSTOMERS.CUSTOMER_TYPE,
                        PRODUCTS.CATEGORY,
                        ORDER_ITEMS.QUANTITY,
                        PRODUCTS.PRICE
                ).as("discount_amount"),

                // Final Amount calculation (Logic in SQL)
                        ORDER_ITEMS.QUANTITY.multiply(PRODUCTS.PRICE)
                    .minus(fnCalculateDiscount(
                            CUSTOMERS.CUSTOMER_TYPE,
                            PRODUCTS.CATEGORY,
                            ORDER_ITEMS.QUANTITY,
                            PRODUCTS.PRICE
                    )).as("final_amount")
            )
                .from(ORDERS)
                .join(CUSTOMERS).on(ORDERS.CUSTOMER_ID.eq(CUSTOMERS.CUSTOMER_ID))
                .join(ORDER_ITEMS).on(ORDERS.ORDER_ID.eq(ORDER_ITEMS.ORDER_ID))
                .join(PRODUCTS).on(ORDER_ITEMS.PRODUCT_ID.eq(PRODUCTS.PRODUCT_ID))
                .join(DISCOUNT_RULES).on(DISCOUNT_RULES.CUSTOMER_TYPE.eq(CUSTOMERS.CUSTOMER_TYPE))
                .and(DISCOUNT_RULES.PRODUCT_CATEGORY.eq(PRODUCTS.CATEGORY))
                .and(ORDER_ITEMS.QUANTITY.between(DISCOUNT_RULES.MIN_QTY, DISCOUNT_RULES.MAX_QTY))
            .where(ORDERS.ORDER_ID.eq(orderId))
            .fetchInto(OrderSummaryDTO.class);
    }
}