ALTER TABLE retail_banking.kyc_extracted_data
DROP COLUMN IF EXISTS pan_number;

ALTER TABLE retail_banking.kyc_extracted_data
DROP COLUMN IF EXISTS full_name;

ALTER TABLE retail_banking.kyc_extracted_data
DROP COLUMN IF EXISTS father_name;

ALTER TABLE retail_banking.kyc_extracted_data
DROP COLUMN IF EXISTS date_of_birth;