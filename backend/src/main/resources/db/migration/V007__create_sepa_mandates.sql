CREATE TABLE IF NOT EXISTS sepa_mandates (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    mandate_reference VARCHAR(50) NOT NULL UNIQUE,
    account_holder_name VARCHAR(150) NOT NULL,
    iban_masked VARCHAR(50) NOT NULL,
    iban_encrypted VARCHAR(255) NOT NULL,
    bic VARCHAR(20),
    bank_name VARCHAR(200),
    accepted_terms BOOLEAN NOT NULL DEFAULT FALSE,
    accepted_at TIMESTAMP NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
