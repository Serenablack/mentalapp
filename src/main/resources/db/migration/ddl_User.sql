CREATE TABLE users
(
    id                  BIGINT AUTO_INCREMENT NOT NULL,
    email               VARCHAR(255)          NOT NULL,
    username            VARCHAR(100)          NOT NULL,
    password_hash       VARCHAR(255)          NULL,
    profile_picture_url VARCHAR(500)          NULL,
    external_id         VARCHAR(255)          NULL,
    auth_provider       VARCHAR(50)           NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uc_users_username UNIQUE (username);