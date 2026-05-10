-- V1__init.sql
-- Initial database schema for Residual Risk Calculator

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER',
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Indexes on users
CREATE INDEX IF NOT EXISTS idx_user_email    ON users (email);
CREATE INDEX IF NOT EXISTS idx_user_username ON users (username);

-- Create risks table
CREATE TABLE IF NOT EXISTS risks (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    category    VARCHAR(100) NOT NULL,
    risk_score  DOUBLE PRECISION NOT NULL,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMP,
    created_by  BIGINT REFERENCES users (id),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Indexes on risks
CREATE INDEX IF NOT EXISTS idx_risk_category ON risks (category);
CREATE INDEX IF NOT EXISTS idx_risk_name     ON risks (name);
CREATE INDEX IF NOT EXISTS idx_risk_score    ON risks (risk_score);
CREATE INDEX IF NOT EXISTS idx_risk_deleted  ON risks (is_deleted);
