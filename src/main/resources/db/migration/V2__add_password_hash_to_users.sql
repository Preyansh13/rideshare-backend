-- Add a column to store BCrypt password hashes.
-- Nullable initially because existing users do not have passwords yet.
ALTER TABLE users
ADD COLUMN password_hash VARCHAR(255);