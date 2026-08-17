ALTER TABLE retail_banking.kyc_extracted_data
    ADD COLUMN IF NOT EXISTS pan_number VARCHAR(20);

ALTER TABLE retail_banking.kyc_extracted_data
    ADD COLUMN IF NOT EXISTS full_name VARCHAR(255);

ALTER TABLE retail_banking.kyc_extracted_data
    ADD COLUMN IF NOT EXISTS father_name VARCHAR(255);

ALTER TABLE retail_banking.kyc_extracted_data
    ADD COLUMN IF NOT EXISTS date_of_birth DATE;