CREATE TABLE retail_banking.refresh_tokens
(
    id          BIGSERIAL PRIMARY KEY,

    user_id     BIGINT       NOT NULL,

    token_hash  VARCHAR(64)  NOT NULL,

    expiry_date TIMESTAMP(6) NOT NULL,

    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,

    created_at  TIMESTAMP(6) NOT NULL,

    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id)
            REFERENCES retail_banking.users (id),

    CONSTRAINT uq_refresh_token_hash
        UNIQUE (token_hash)
);


CREATE INDEX idx_refresh_token_user
    ON retail_banking.refresh_tokens (user_id);