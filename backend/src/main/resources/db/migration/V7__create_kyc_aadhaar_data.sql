CREATE TABLE retail_banking.kyc_aadhaar_data
(
    id              BIGSERIAL PRIMARY KEY,

    kyc_document_id BIGINT       NOT NULL,

    aadhaar_number  VARCHAR(20),

    full_name       VARCHAR(255),

    date_of_birth   DATE,

    gender          VARCHAR(20),

    address         TEXT,

    mobile_number   VARCHAR(20),

    created_at      TIMESTAMP(6) NOT NULL,

    updated_at      TIMESTAMP(6),

    CONSTRAINT fk_kyc_aadhaar_data_document
        FOREIGN KEY (kyc_document_id)
            REFERENCES retail_banking.kyc_documents (id),

    CONSTRAINT uq_kyc_aadhaar_data_document
        UNIQUE (kyc_document_id)
);