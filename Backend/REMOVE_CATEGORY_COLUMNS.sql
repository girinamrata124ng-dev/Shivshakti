-- Remove category_fk column from product table
-- Run this in your MySQL database (e.g., using MySQL Workbench, phpMyAdmin, or command line)

USE cafe_management;

-- First, disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Drop the foreign key constraint first
ALTER TABLE product DROP FOREIGN KEY FK275nu1ncohhfur6qhxiwrm3go;

-- Now drop the category_fk column from product table
ALTER TABLE product DROP COLUMN category_fk;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Verify the column is removed
DESCRIBE product;
