-- PostgreSQL Database Schema for BhoomiSetu (Phase 1)

CREATE TYPE user_role AS ENUM ('USER', 'SURVEYOR');
CREATE TYPE verification_status AS ENUM ('PENDING', 'VERIFIED', 'REJECTED');

CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) UNIQUE NOT NULL,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role user_role NOT NULL DEFAULT 'USER',
    created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)
);

CREATE TABLE surveyor_profiles (
    user_id VARCHAR(36) PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    license_number VARCHAR(50) UNIQUE NOT NULL,
    service_areas TEXT[] NOT NULL, -- Array of district names
    years_of_experience INT NOT NULL DEFAULT 0,
    id_proof_url TEXT,
    is_available BOOLEAN NOT NULL DEFAULT FALSE,
    verification_status verification_status NOT NULL DEFAULT 'PENDING'
);

CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_surveyor_license ON surveyor_profiles(license_number);
