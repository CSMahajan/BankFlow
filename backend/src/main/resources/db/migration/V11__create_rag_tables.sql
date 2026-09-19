CREATE TABLE retail_banking.rag_sources
(
    id           BIGSERIAL PRIMARY KEY,

    source_path  VARCHAR(500) NOT NULL,
    source_type  VARCHAR(50)  NOT NULL,
    title        VARCHAR(255),

    content_hash VARCHAR(64)  NOT NULL,

    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,

    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_rag_sources_path
        UNIQUE (source_path),

    CONSTRAINT chk_rag_sources_type
        CHECK (
            source_type IN (
                            'MARKDOWN',
                            'OPENAPI',
                            'DRAWIO_XML'
                )
            )
);

CREATE TABLE retail_banking.rag_chunks
(
    id           BIGSERIAL PRIMARY KEY,

    source_id    BIGINT      NOT NULL,

    chunk_index  INTEGER     NOT NULL,

    section      VARCHAR(500),

    content      TEXT        NOT NULL,

    audience     VARCHAR(20) NOT NULL,

    content_hash VARCHAR(64) NOT NULL,

    embedding    vector(1536),

    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_rag_chunks_source
        FOREIGN KEY (source_id)
            REFERENCES retail_banking.rag_sources (id)
            ON DELETE CASCADE,

    CONSTRAINT uk_rag_chunks_source_index
        UNIQUE (source_id, chunk_index),

    CONSTRAINT chk_rag_chunks_audience
        CHECK (
            audience IN (
                         'SHARED',
                         'CUSTOMER',
                         'ADMIN'
                )
            )
);

CREATE INDEX idx_rag_chunks_source_id
    ON retail_banking.rag_chunks (source_id);

CREATE INDEX idx_rag_chunks_audience
    ON retail_banking.rag_chunks (audience);