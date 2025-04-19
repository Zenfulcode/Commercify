-- liquibase formatted sql

-- changeset gkhaavik:1737484993955-1
CREATE TABLE payment_attempts
(
    payment_id   VARCHAR(255) NOT NULL,
    attempt_time datetime     NULL,
    successful   BIT(1)       NULL,
    details      VARCHAR(255) NULL
);

-- changeset gkhaavik:1737484993955-2
CREATE TABLE payments
(
    status             VARCHAR(255) NOT NULL,
    payment_method     VARCHAR(255) NOT NULL,
    payment_provider   VARCHAR(255) NOT NULL,
    provider_reference VARCHAR(255) NULL,
    transaction_id     VARCHAR(255) NULL,
    error_message      VARCHAR(255) NULL,
    retry_count        INT          NULL,
    max_retries        INT          NULL,
    created_at         datetime     NULL,
    updated_at         datetime     NULL,
    completed_at       datetime     NULL,
    id                 VARCHAR(255) NOT NULL,
    amount             DECIMAL      NULL,
    currency           VARCHAR(255) NULL,
    order_id           VARCHAR(255) NULL,
    CONSTRAINT pk_payments PRIMARY KEY (id)
);

-- changeset gkhaavik:1737484993955-3
ALTER TABLE payments
    ADD CONSTRAINT FK_PAYMENTS_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id);

-- changeset gkhaavik:1737484993955-4
ALTER TABLE payment_attempts
    ADD CONSTRAINT fk_payment_attempts_on_payment FOREIGN KEY (payment_id) REFERENCES payments (id);

