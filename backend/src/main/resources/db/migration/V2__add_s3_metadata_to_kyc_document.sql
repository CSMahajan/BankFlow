ALTER TABLE retail_banking.kyc_documents
    ADD COLUMN IF NOT EXISTS storage_type VARCHAR(50);

UPDATE retail_banking.kyc_documents
SET storage_type = 'LOCAL'
WHERE storage_type IS NULL;

ALTER TABLE retail_banking.kyc_documents
    ALTER COLUMN storage_type SET NOT NULL;


ALTER TABLE retail_banking.kyc_documents
    ADD COLUMN IF NOT EXISTS s3_bucket VARCHAR(255);

ALTER TABLE retail_banking.kyc_documents
    ADD COLUMN IF NOT EXISTS s3_object_key VARCHAR(500);

ALTER TABLE retail_banking.kyc_documents
    ADD COLUMN IF NOT EXISTS encryption_type VARCHAR(50);

ALTER TABLE retail_banking.kyc_documents
    ADD COLUMN IF NOT EXISTS checksum VARCHAR(100);