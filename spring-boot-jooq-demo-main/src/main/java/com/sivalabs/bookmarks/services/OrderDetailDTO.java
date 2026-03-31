package com.sivalabs.bookmarks.services;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record OrderDetailDTO(
    Integer orderId,
    String customerName,
    String productName,
    Integer quantity,
    BigDecimal price,
    BigDecimal discountPercent
) {
    // BUSINESS LOGIC IS NOW HERE - EASY TO UNIT TEST!
    public BigDecimal getTotalAmount() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal getDiscountAmount() {
        return getTotalAmount()
            .multiply(discountPercent)
            .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
    }

    public BigDecimal getFinalAmount() {
        return getTotalAmount().subtract(getDiscountAmount());
    }
}