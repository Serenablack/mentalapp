CREATE TABLE emotions
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    `key`      VARCHAR(100)          NOT NULL,
    label      VARCHAR(255)          NOT NULL,
    parent_key VARCHAR               NULL,
    created_at datetime              NOT NULL,
    updated_at datetime              NOT NULL,
    CONSTRAINT pk_emotions PRIMARY KEY (id)
);

ALTER TABLE emotions
    ADD CONSTRAINT uc_emotions_key UNIQUE (`key`);

ALTER TABLE emotions
    ADD CONSTRAINT FK_EMOTIONS_ON_PARENT_KEY FOREIGN KEY (parent_key) REFERENCES emotions (`key`);