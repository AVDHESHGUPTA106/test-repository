DELIMITER $$

CREATE PROCEDURE GetOrderDiscount(IN p_order_id INT)
BEGIN
SELECT
    o.order_id,
    c.customer_name,
    p.product_name,
    oi.quantity,
    p.price,

    -- Base amount
    (oi.quantity * p.price) AS total_amount,

    -- Discount %
    dr.discount_percent,

    -- Discount value
    ((oi.quantity * p.price) * dr.discount_percent / 100) AS discount_amount,

    -- Final price after discount
    ((oi.quantity * p.price) -
     ((oi.quantity * p.price) * dr.discount_percent / 100)) AS final_amount

FROM orders o
         JOIN customers c
              ON o.customer_id = c.customer_id

         JOIN order_items oi
              ON o.order_id = oi.order_id

         JOIN products p
              ON oi.product_id = p.product_id

         JOIN discount_rules dr
              ON dr.customer_type = c.customer_type
                  AND dr.product_category = p.category
                  AND oi.quantity BETWEEN dr.min_qty AND dr.max_qty

WHERE o.order_id = p_order_id;

END$$

DELIMITER ;