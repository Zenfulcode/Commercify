-- liquibase formatted sql

-- changeset gkhaavik:1736722698857-1
CREATE TABLE domain_events
(
    event_id       VARCHAR(255) NOT NULL,
    event_type     VARCHAR(255) NOT NULL,
    event_data     LONGTEXT     NOT NULL,
    occurred_on    datetime     NOT NULL,
    aggregate_id   VARCHAR(255) NULL,
    aggregate_type VARCHAR(255) NULL,
    CONSTRAINT pk_domain_events PRIMARY KEY (event_id)
);

-- changeset gkhaavik:1736722698857-2
CREATE TABLE order_lines
(
    quantity           INT          NOT NULL,
    id                 VARCHAR(255) NOT NULL,
    unit_price         DECIMAL      NULL,
    currency           VARCHAR(255) NULL,
    order_id           VARCHAR(255) NOT NULL,
    product_id         VARCHAR(255) NOT NULL,
    product_variant_id VARCHAR(255) NULL,
    CONSTRAINT pk_order_lines PRIMARY KEY (id)
);

-- changeset gkhaavik:1736722698857-3
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

-- changeset gkhaavik:1736722698857-4
CREATE TABLE orders
(
    status                 VARCHAR(255) NOT NULL,
    currency               VARCHAR(255) NULL,
    order_shipping_info_id BIGINT       NULL,
    created_at             datetime     NULL,
    updated_at             datetime     NULL,
    id                     VARCHAR(255) NOT NULL,
    subtotal               DECIMAL      NULL,
    shipping_cost          DECIMAL      NULL,
    tax                    DECIMAL      NULL,
    total_amount           DECIMAL      NULL,
    user_id                VARCHAR(255) NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id)
);

-- changeset gkhaavik:1736722698857-5
CREATE TABLE product_variants
(
    sku        VARCHAR(255) NOT NULL,
    stock      INT          NULL,
    image_url  VARCHAR(255) NULL,
    id         VARCHAR(255) NOT NULL,
    unit_price DECIMAL      NULL,
    currency   VARCHAR(255) NULL,
    product_id VARCHAR(255) NOT NULL,
    CONSTRAINT pk_product_variants PRIMARY KEY (id)
);

-- changeset gkhaavik:1736722698857-6
CREATE TABLE products
(
    name          VARCHAR(255) NOT NULL,
    `description` VARCHAR(255) NULL,
    stock         INT          NOT NULL,
    image_url     VARCHAR(255) NULL,
    active        BIT(1)       NOT NULL,
    created_at    datetime     NULL,
    updated_at    datetime     NULL,
    id            VARCHAR(255) NOT NULL,
    unit_price    DECIMAL      NULL,
    currency      VARCHAR(255) NULL,
    category_id   VARCHAR(255) NULL,
    CONSTRAINT pk_products PRIMARY KEY (id)
);

-- changeset gkhaavik:1736722698857-7
CREATE TABLE user_roles
(
    user_id VARCHAR(255) NOT NULL,
    `role`  VARCHAR(255) NULL
);

-- changeset gkhaavik:1736722698857-8
CREATE TABLE users
(
    email         VARCHAR(255) NOT NULL,
    first_name    VARCHAR(255) NOT NULL,
    last_name     VARCHAR(255) NOT NULL,
    password      VARCHAR(255) NOT NULL,
    phone_number  VARCHAR(255) NULL,
    status        VARCHAR(255) NOT NULL,
    created_at    datetime     NULL,
    updated_at    datetime     NULL,
    last_login_at datetime     NULL,
    id            VARCHAR(255) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

-- changeset gkhaavik:1736722698857-9
CREATE TABLE variant_options
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    name               VARCHAR(255)          NOT NULL,
    value              VARCHAR(255)          NOT NULL,
    product_variant_id VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_variant_options PRIMARY KEY (id)
);

-- changeset gkhaavik:1736722698857-10
ALTER TABLE product_variants
    ADD CONSTRAINT uc_product_variants_sku UNIQUE (sku);

-- changeset gkhaavik:1736722698857-11
ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

-- changeset gkhaavik:1736722698857-12
ALTER TABLE orders
    ADD CONSTRAINT FK_ORDERS_ON_ORDER_SHIPPING_INFO FOREIGN KEY (order_shipping_info_id) REFERENCES order_shipping_info (id);

-- changeset gkhaavik:1736722698857-13
ALTER TABLE orders
    ADD CONSTRAINT FK_ORDERS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

-- changeset gkhaavik:1736722698857-14
ALTER TABLE order_lines
    ADD CONSTRAINT FK_ORDER_LINES_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id);

-- changeset gkhaavik:1736722698857-15
ALTER TABLE order_lines
    ADD CONSTRAINT FK_ORDER_LINES_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES products (id);

-- changeset gkhaavik:1736722698857-16
ALTER TABLE order_lines
    ADD CONSTRAINT FK_ORDER_LINES_ON_PRODUCT_VARIANT FOREIGN KEY (product_variant_id) REFERENCES product_variants (id);

-- changeset gkhaavik:1736722698857-17
ALTER TABLE product_variants
    ADD CONSTRAINT FK_PRODUCT_VARIANTS_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES products (id);

-- changeset gkhaavik:1736722698857-18
ALTER TABLE variant_options
    ADD CONSTRAINT FK_VARIANT_OPTIONS_ON_PRODUCT_VARIANT FOREIGN KEY (product_variant_id) REFERENCES product_variants (id);

-- changeset gkhaavik:1736722698857-19
ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_on_user FOREIGN KEY (user_id) REFERENCES users (id);

