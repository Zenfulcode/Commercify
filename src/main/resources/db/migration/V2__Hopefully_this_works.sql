CREATE TABLE addresses
(
    id                  BIGINT AUTO_INCREMENT NOT NULL,
    street              VARCHAR(255)          NOT NULL,
    city                VARCHAR(255)          NOT NULL,
    state               VARCHAR(255)          NOT NULL,
    postal_code         VARCHAR(255)          NOT NULL,
    country             VARCHAR(255)          NOT NULL,
    user_id             BIGINT                NOT NULL,
    is_billing_address  BIT(1)                NOT NULL,
    is_shipping_address BIT(1)                NOT NULL,
    created_at          datetime              NULL,
    updated_at          datetime              NULL,
    CONSTRAINT pk_addresses PRIMARY KEY (id)
);

CREATE TABLE order_lines
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    product_id BIGINT                NOT NULL,
    quantity   INT                   NOT NULL,
    unit_price DOUBLE                NOT NULL,
    currency   VARCHAR(255)          NOT NULL,
    order_id   BIGINT                NOT NULL,
    CONSTRAINT pk_order_lines PRIMARY KEY (id)
);

CREATE TABLE orders
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    user_id      BIGINT                NOT NULL,
    status       VARCHAR(255)          NOT NULL,
    currency     VARCHAR(255)          NULL,
    total_amount DOUBLE                NULL,
    created_at   datetime              NULL,
    updated_at   datetime              NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id)
);

CREATE TABLE payments
(
    id                    BIGINT AUTO_INCREMENT NOT NULL,
    stripe_payment_intent VARCHAR(255)          NULL,
    order_id              BIGINT                NOT NULL,
    total_amount          DOUBLE                NULL,
    payment_method        VARCHAR(255)          NULL,
    payment_provider      VARCHAR(255)          NULL,
    status                VARCHAR(255)          NULL,
    created_at            datetime              NULL,
    updated_at            datetime              NULL,
    CONSTRAINT pk_payments PRIMARY KEY (id)
);

CREATE TABLE products
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    name            VARCHAR(255)          NULL,
    `description`   VARCHAR(255)          NULL,
    stock           INT                   NULL,
    stripe_id       VARCHAR(255)          NULL,
    active          BIT(1)                NULL,
    image_url       VARCHAR(255)          NULL,
    currency        VARCHAR(255)          NULL,
    unit_price      DOUBLE                NULL,
    stripe_price_id VARCHAR(255)          NULL,
    created_at      datetime              NULL,
    updated_at      datetime              NULL,
    CONSTRAINT pk_products PRIMARY KEY (id)
);

CREATE TABLE user_roles
(
    id     BIGINT       NOT NULL,
    `role` VARCHAR(255) NULL
);

CREATE TABLE users
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    first_name VARCHAR(50)           NOT NULL,
    last_name  VARCHAR(50)           NOT NULL,
    email      VARCHAR(100)          NOT NULL,
    password   VARCHAR(255)          NOT NULL,
    created_at datetime              NULL,
    updated_at datetime              NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE addresses
    ADD CONSTRAINT FK_ADDRESSES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE order_lines
    ADD CONSTRAINT FK_ORDER_LINES_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_on_user_entity FOREIGN KEY (id) REFERENCES users (id);