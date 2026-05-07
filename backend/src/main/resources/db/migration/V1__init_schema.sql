-- V0 schema: lightweight, append-only.
-- We can't use Account Aggregator yet, so this captures manually-entered data.

CREATE TABLE leads (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email       VARCHAR(255),
    phone       VARCHAR(20),
    source      VARCHAR(50),
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_leads_email ON leads(email);

CREATE TABLE mri_sessions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lead_id         UUID REFERENCES leads(id),
    request_json    JSONB NOT NULL,
    report_json     JSONB,
    health_score    INT,
    recommended_path VARCHAR(50),
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_sessions_lead ON mri_sessions(lead_id);
CREATE INDEX idx_sessions_created ON mri_sessions(created_at);

CREATE TABLE consultations (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id          UUID REFERENCES mri_sessions(id),
    razorpay_payment_id VARCHAR(100),
    razorpay_order_id   VARCHAR(100),
    amount              DECIMAL(10,2),
    status              VARCHAR(20) DEFAULT 'pending',
    scheduled_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_consultations_session ON consultations(session_id);
CREATE INDEX idx_consultations_status ON consultations(status);
