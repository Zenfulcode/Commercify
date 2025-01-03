-- liquibase formatted sql

-- changeset gkhaavik:1735938302939-1
CREATE TABLE domain_events
(
    eventId       VARCHAR(255) NOT NULL,
    eventType     VARCHAR(255) NOT NULL,
    eventData     TEXT         NOT NULL,
    occurredOn    datetime     NOT NULL,
    aggregateId   VARCHAR(255) NULL,
    aggregateType VARCHAR(255) NULL,
    CONSTRAINT pk_domain_events PRIMARY KEY (eventId)
);

-- changeset gkhaavik:1735938302939-2
CREATE TABLE order_shipping_info
(
    id                  BIGINT AUTO_INCREMENT NOT NULL,
    customer_first_name VARCHAR(255)          NULL,
    customer_last_name  VARCHAR(255)          NULL,
    customer_email      VARCHAR(255)          NULL,
    customer_phone      VARCHAR(255)          NULL,
    shipping_street     VARCHAR(255)          NOT NULL,
    shipping_city       VARCHAR(255)          NOT NULL,
    shipping_state      VARCHAR(255)          NULL,
    shipping_zip        VARCHAR(255)          NOT NULL,
    shipping_country    VARCHAR(255)          NOT NULL,
    billing_street      VARCHAR(255)          NULL,
    billing_city        VARCHAR(255)          NULL,
    billing_state       VARCHAR(255)          NULL,
    billing_zip         VARCHAR(255)          NULL,
    billing_country     VARCHAR(255)          NULL,
    CONSTRAINT pk_order_shipping_info PRIMARY KEY (id)
);

-- changeset gkhaavik:1735938302939-3
CREATE TABLE products
(
    product_id  VARCHAR(255)   NOT NULL,
    name        VARCHAR(255)   NOT NULL,
    description VARCHAR(255)   NULL,
    stock       INT            NOT NULL,
    image_url   VARCHAR(255)   NULL,
    active      BIT(1)         NOT NULL,
    unit_price  DECIMAL(19, 2) NULL,
    currency    VARCHAR(255)   NULL,
    category_id VARCHAR(255)   NULL,
    CONSTRAINT pk_products PRIMARY KEY (product_id)
);

-- changeset gkhaavik:1735938302939-4
CREATE TABLE product_variants
(
    id         VARCHAR(255)   NOT NULL,
    product_id VARCHAR(255)   NOT NULL,
    sku        VARCHAR(255)   NOT NULL,
    stock      INT            NULL,
    image_url  VARCHAR(255)   NULL,
    unit_price DECIMAL(19, 2) NULL,
    currency   VARCHAR(255)   NULL,
    CONSTRAINT pk_product_variants PRIMARY KEY (id),
    CONSTRAINT uc_product_variants_sku UNIQUE (sku)
);

-- changeset gkhaavik:1735938302939-5
CREATE TABLE variant_options
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    name               VARCHAR(255)          NOT NULL,
    value              VARCHAR(255)          NOT NULL,
    product_variant_id VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_variant_options PRIMARY KEY (id)
);

-- changeset gkhaavik:1735938302939-6
CREATE TABLE orders
(
    order_id               VARCHAR(255)   NOT NULL,
    user_id                VARCHAR(255)   NULL,
    status                 VARCHAR(255)   NOT NULL,
    currency               VARCHAR(255)   NULL,
    subtotal               DECIMAL(19, 2) NULL,
    shipping_cost          DECIMAL(19, 2) NULL,
    tax                    DECIMAL(19, 2) NULL,
    total_amount           DECIMAL(19, 2) NULL,
    order_shipping_info_id BIGINT         NULL,
    created_at             datetime       NOT NULL,
    updated_at             datetime       NULL,
    CONSTRAINT pk_orders PRIMARY KEY (order_id)
);

-- changeset gkhaavik:1735938302939-7
CREATE TABLE order_lines
(
    orderline_id       VARCHAR(255)   NOT NULL,
    order_id           VARCHAR(255)   NOT NULL,
    product_id         VARCHAR(255)   NOT NULL,
    product_variant_id VARCHAR(255)   NULL,
    quantity           INT            NOT NULL,
    unit_price         DECIMAL(19, 2) NULL,
    currency           VARCHAR(255)   NULL,
    CONSTRAINT pk_order_lines PRIMARY KEY (orderline_id)
);

-- changeset gkhaavik:1735938302939-8
ALTER TABLE product_variants
    ADD CONSTRAINT FK_PRODUCT_VARIANTS_ON_PRODUCT
        FOREIGN KEY (product_id)
            REFERENCES products (product_id);

-- changeset gkhaavik:1735938302939-9
ALTER TABLE variant_options
    ADD CONSTRAINT FK_VARIANT_OPTIONS_ON_VARIANT
        FOREIGN KEY (product_variant_id)
            REFERENCES product_variants (id);

-- changeset gkhaavik:1735938302939-10
ALTER TABLE orders
    ADD CONSTRAINT FK_ORDERS_ON_ORDER_SHIPPING_INFO
        FOREIGN KEY (order_shipping_info_id)
            REFERENCES order_shipping_info (id);

-- changeset gkhaavik:1735938302939-11
ALTER TABLE order_lines
    ADD CONSTRAINT FK_ORDER_LINES_ON_ORDER
        FOREIGN KEY (order_id)
            REFERENCES orders (order_id);

-- changeset gkhaavik:1735938302939-12
ALTER TABLE order_lines
    ADD CONSTRAINT FK_ORDER_LINES_ON_VARIANT
        FOREIGN KEY (product_variant_id)
            REFERENCES product_variants (id);

-- changeset gkhaavik:1735938302939-13
ALTER TABLE order_lines
    ADD CONSTRAINT FK_ORDER_LINES_ON_PRODUCT
        FOREIGN KEY (product_id)
            REFERENCES products (product_id);