package com.sivalabs.bookmarks.services;

import java.math.BigDecimal;

public record OrderSummaryDTO(
    Integer orderId,
    String productName,
    Integer quantity,
    BigDecimal price,
    BigDecimal totalAmount,
    BigDecimal discountAmount,
    BigDecimal finalAmount
) {}