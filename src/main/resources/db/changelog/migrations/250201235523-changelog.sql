-- liquibase formatted sql

-- changeset gkhaavik:1738450523221-1
ALTER TABLE payments
    MODIFY amount DECIMAL;

-- changeset gkhaavik:1738450523221-3
ALTER TABLE payments
    MODIFY payment_method VARCHAR(255);

-- changeset gkhaavik:1738450523221-5
ALTER TABLE payments
    MODIFY payment_provider VARCHAR(255);

-- changeset gkhaavik:1738450523221-6
ALTER TABLE orders
    MODIFY shipping_cost DECIMAL;

-- changeset gkhaavik:1738450523221-8
ALTER TABLE orders
    MODIFY status VARCHAR(255);

-- changeset gkhaavik:1738450523221-10
ALTER TABLE payments
    MODIFY status VARCHAR(255);

-- changeset gkhaavik:1738450523221-11
ALTER TABLE orders
    MODIFY subtotal DECIMAL;

-- changeset gkhaavik:1738450523221-12
ALTER TABLE orders
    MODIFY tax DECIMAL;

-- changeset gkhaavik:1738450523221-13
ALTER TABLE orders
    MODIFY total_amount DECIMAL;

-- changeset gkhaavik:1738450523221-14
ALTER TABLE order_lines
    MODIFY unit_price DECIMAL;

-- changeset gkhaavik:1738450523221-15
ALTER TABLE product_variants
    MODIFY unit_price DECIMAL;

-- changeset gkhaavik:1738450523221-16
ALTER TABLE products
    MODIFY unit_price DECIMAL;

