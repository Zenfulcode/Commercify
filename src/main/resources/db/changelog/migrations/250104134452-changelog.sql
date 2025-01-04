-- liquibase formatted sql

-- changeset gkhaavik:1735994692204-5
CREATE TABLE user_roles
(
    user_id VARCHAR(255) NOT NULL,
    `role`  VARCHAR(255) NULL
);

-- changeset gkhaavik:1735994692204-6
CREATE TABLE users
(
    email       VARCHAR(255) NOT NULL,
    firstName   VARCHAR(255) NOT NULL,
    lastName    VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    phoneNumber VARCHAR(255) NULL,
    status      VARCHAR(255) NOT NULL,
    createdAt   datetime     NOT NULL,
    updatedAt   datetime     NOT NULL,
    lastLoginAt datetime     NULL,
    user_id     VARCHAR(255) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (user_id)
);

-- changeset gkhaavik:1735994692204-7
ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

-- changeset gkhaavik:1735994692204-9
ALTER TABLE orders
    ADD CONSTRAINT FK_ORDERS_ON_USER FOREIGN KEY (user_id) REFERENCES users (user_id);

-- changeset gkhaavik:1735994692204-10
ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_on_user FOREIGN KEY (user_id) REFERENCES users (user_id);

