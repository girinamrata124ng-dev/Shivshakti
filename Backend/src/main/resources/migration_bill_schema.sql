-- Migration Script: Remove email/paymentMethod and add billNo auto-increment
-- Run this SQL script in your MySQL database

-- Use the cafe database
USE cafe;

-- Step 1: Create new table with desired schema (preserving data)
CREATE TABLE bill_new (
    billNo INT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(255),
    name VARCHAR(255),
    contactnumber VARCHAR(255),
    total INT,
    productdetails JSON,
    createdby VARCHAR(255),
    status VARCHAR(255)
);

-- Step 2: Copy data from old table to new table (excluding email and paymentMethod)
-- Note: The old id will be lost, new billNo will be auto-generated
INSERT INTO bill_new (uuid, name, contactnumber, total, productdetails, createdby, status)
SELECT uuid, name, contactnumber, total, productdetails, createdby, status FROM bill;

-- Step 3: Drop the old table
DROP TABLE bill;

-- Step 4: Rename new table to original name
RENAME TABLE bill_new TO bill;

-- Alternative: If you want to keep old id as billNo (not auto-increment):
-- ALTER TABLE bill CHANGE COLUMN id billNo INT NOT NULL AUTO_INCREMENT;
-- ALTER TABLE bill DROP COLUMN email;
-- ALTER TABLE bill DROP COLUMN paymentmethod;

