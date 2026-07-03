-- V1__init_identity_schema.sql
-- Identity bounded context - initial schema
-- PostgreSQL / Flyway

CREATE SCHEMA IF NOT EXISTS identity;

-- ── accounts ──────────────────────────────────────────────────────
CREATE TABLE identity.accounts (
    id                          UUID            NOT NULL,
    email                       VARCHAR(320)    NOT NULL,
    password_hash               TEXT            NOT NULL,
    status                      VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    failed_login_attempts       INT             NOT NULL DEFAULT 0,
    verification_token          VARCHAR(36),
    verification_token_expires_at TIMESTAMPTZ,
    reset_token                 VARCHAR(36),
    reset_token_expires_at      TIMESTAMPTZ,
    reset_token_used            BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    version                     BIGINT          NOT NULL DEFAULT 0,

    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT uq_accounts_email UNIQUE (email),
    CONSTRAINT chk_accounts_status
        CHECK (status IN ('PENDING', 'ACTIVE', 'LOCKED', 'SUSPENDED', 'DELETED')),
    CONSTRAINT chk_failed_attempts CHECK (failed_login_attempts >= 0)
);

-- ── account_roles ─────────────────────────────────────────────────
CREATE TABLE identity.account_roles (
    account_id  UUID        NOT NULL,
    role        VARCHAR(30) NOT NULL,

    CONSTRAINT pk_account_roles PRIMARY KEY (account_id, role),
    CONSTRAINT fk_account_roles_account
        FOREIGN KEY (account_id) REFERENCES identity.accounts(id) ON DELETE CASCADE,
    CONSTRAINT chk_role
        CHECK (role IN ('GUEST', 'ORGANIZER', 'ADMIN', 'COMPLIANCE'))
);

-- ── Indexes ───────────────────────────────────────────────────────
CREATE INDEX idx_accounts_email        ON identity.accounts(email);
CREATE INDEX idx_accounts_status       ON identity.accounts(status);
CREATE INDEX idx_accounts_vtok         ON identity.accounts(verification_token)
    WHERE verification_token IS NOT NULL;
CREATE INDEX idx_accounts_rtok         ON identity.accounts(reset_token)
    WHERE reset_token IS NOT NULL;
