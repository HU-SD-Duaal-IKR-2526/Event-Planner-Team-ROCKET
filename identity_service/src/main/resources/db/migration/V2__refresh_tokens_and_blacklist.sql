-- V2__refresh_tokens_and_blacklist.sql
-- Adds refresh token storage and token blacklist tables
-- Architecture doc: Identity is responsible for token-blacklist

-- ── refresh_tokens ────────────────────────────────────────────────
CREATE TABLE identity.refresh_tokens (
    id          UUID        NOT NULL,
    token_hash  VARCHAR(64) NOT NULL,   -- SHA-256 hex of raw token
    account_id  UUID        NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uq_refresh_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_account
        FOREIGN KEY (account_id) REFERENCES identity.accounts(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_hash      ON identity.refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_account   ON identity.refresh_tokens(account_id);
CREATE INDEX idx_refresh_tokens_expires   ON identity.refresh_tokens(expires_at)
    WHERE revoked = FALSE;

-- ── token_blacklist ───────────────────────────────────────────────
-- Blacklisted access tokens (JWT jti claims) — used for logout invalidation
-- Entries are automatically cleaned up after the token's expiry
CREATE TABLE identity.token_blacklist (
    id              UUID        NOT NULL,
    jti             VARCHAR(36) NOT NULL,   -- JWT ID claim
    account_id      UUID        NOT NULL,
    expires_at      TIMESTAMPTZ NOT NULL,   -- when the original JWT expires (safe to delete after)
    blacklisted_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_token_blacklist PRIMARY KEY (id),
    CONSTRAINT uq_token_blacklist_jti UNIQUE (jti)
);

CREATE INDEX idx_blacklist_jti      ON identity.token_blacklist(jti);
CREATE INDEX idx_blacklist_expires  ON identity.token_blacklist(expires_at);
