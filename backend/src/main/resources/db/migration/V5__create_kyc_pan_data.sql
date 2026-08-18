CREATE TABLE retail_banking.kyc_pan_data
(

    id              BIGSERIAL PRIMARY KEY,

    kyc_document_id BIGINT       NOT NULL,

    pan_number      VARCHAR(20),

    full_name       VARCHAR(255),

    father_name     VARCHAR(255),

    date_of_birth   DATE,

    created_at      TIMESTAMP(6) NOT NULL,

    updated_at      TIMESTAMP(6),

    CONSTRAINT fk_kyc_pan_data_document
        FOREIGN KEY (kyc_document_id)
            REFERENCES retail_banking.kyc_documents (id),

    CONSTRAINT uq_kyc_pan_data_document
        UNIQUE (kyc_document_id)
);