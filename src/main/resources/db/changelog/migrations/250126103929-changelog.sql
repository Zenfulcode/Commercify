-- liquibase formatted sql

-- changeset gkhaavik:1737884368941-1
CREATE TABLE webhook_config
(
    provider     VARCHAR(255) NOT NULL,
    callback_url VARCHAR(255) NOT NULL,
    secret       VARCHAR(255) NOT NULL,
    CONSTRAINT pk_webhook_config PRIMARY KEY (provider)
);

