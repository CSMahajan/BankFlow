CREATE TABLE retail_banking.kyc_extracted_data (

                                                   id BIGSERIAL PRIMARY KEY,

                                                   kyc_document_id BIGINT NOT NULL UNIQUE,

                                                   extracted_text TEXT,

                                                   extraction_status VARCHAR(50) NOT NULL,

                                                   failure_reason VARCHAR(500),

                                                   created_at TIMESTAMP(6) NOT NULL,

                                                   updated_at TIMESTAMP(6),

                                                   CONSTRAINT fk_kyc_extracted_document
                                                       FOREIGN KEY (kyc_document_id)
                                                           REFERENCES retail_banking.kyc_documents(id),

                                                   CONSTRAINT kyc_extraction_status_check
                                                       CHECK (
                                                           extraction_status IN (
                                                                                 'PENDING',
                                                                                 'PROCESSING',
                                                                                 'SUCCESS',
                                                                                 'FAILED'
                                                               )
                                                           )
);