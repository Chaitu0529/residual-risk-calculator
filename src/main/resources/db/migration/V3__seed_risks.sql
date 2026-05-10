-- V3__seed_risks.sql
-- Realistic seed data for Demo Day
-- Covers multiple categories, a range of risk scores, and varied descriptions

INSERT INTO risks (name, description, category, risk_score, is_deleted, created_by, created_at, updated_at)
VALUES

-- ── SECURITY ──────────────────────────────────────────────────────────────────
(
    'SQL Injection in Login Form',
    'The login endpoint does not use parameterized queries. An attacker can bypass authentication or dump the entire users table by injecting SQL into the username field.',
    'Security',
    92.5,
    FALSE,
    (SELECT id FROM users WHERE username = 'admin'),
    NOW() - INTERVAL '30 days',
    NOW() - INTERVAL '30 days'
),
(
    'Broken JWT Secret in Production',
    'The JWT signing secret is a weak, guessable string stored in plain text inside the application properties file. Any attacker with source access can forge valid tokens.',
    'Security',
    88.0,
    FALSE,
    (SELECT id FROM users WHERE username = 'admin'),
    NOW() - INTERVAL '28 days',
    NOW() - INTERVAL '28 days'
),
(
    'Unencrypted Sensitive Data at Rest',
    'Personally identifiable information (PII) including email addresses and risk scores are stored in the database without column-level encryption.',
    'Security',
    74.0,
    FALSE,
    (SELECT id FROM users WHERE username = 'admin'),
    NOW() - INTERVAL '25 days',
    NOW() - INTERVAL '25 days'
),
(
    'Missing Rate Limiting on Auth Endpoints',
    'The /api/auth/login and /api/auth/register endpoints have no rate limiting. This allows brute-force and credential-stuffing attacks without any throttling.',
    'Security',
    68.5,
    FALSE,
    (SELECT id FROM users WHERE username = 'admin'),
    NOW() - INTERVAL '22 days',
    NOW() - INTERVAL '22 days'
),
(
    'Cross-Site Scripting (XSS) in Risk Description',
    'Risk description fields are rendered in the frontend without sanitization. An attacker can inject malicious scripts that execute in other users'' browsers.',
    'Security',
    55.0,
    FALSE,
    (SELECT id FROM users WHERE username = 'admin'),
    NOW() - INTERVAL '20 days',
    NOW() - INTERVAL '20 days'
),

-- ── COMPLIANCE ────────────────────────────────────────────────────────────────
(
    'GDPR Data Retention Policy Not Enforced',
    'User data and risk records are retained indefinitely. GDPR Article 5(1)(e) requires data to be kept no longer than necessary. No automated purge job exists.',
    'Compliance',
    81.0,
    FALSE,
    (SELECT id FROM users WHERE username = 'admin'),
    NOW() - INTERVAL '18 days',
    NOW() - INTERVAL '18 days'
),
(
    'Audit Log Missing for Admin Actions',
    'Administrative actions such as user deletion and risk score modification are not logged. This violates SOC 2 Type II audit trail requirements.',
    'Compliance',
    76.5,
    FALSE,
    (SELECT id FROM users WHERE username = 'admin'),
    NOW() - INTERVAL '15 days',
    NOW() - INTERVAL '15 days'
),
(
    'No Data Processing Agreement with Third-Party Mail Provider',
    'The application sends user emails via a third-party SMTP provider without a signed Data Processing Agreement (DPA), violating GDPR Article 28.',
    'Compliance',
    62.0,
    FALSE,
    (SELECT id FROM users WHERE username = 'admin'),
    NOW() - INTERVAL '12 days',
    NOW() - INTERVAL '12 days'
),

-- ── OPERATIONAL ───────────────────────────────────────────────────────────────
(
    'Single Point of Failure — No Database Replica',
    'The PostgreSQL instance runs as a single node with no read replica or failover. Any database outage results in complete application downtime.',
    'Operational',
    85.0,
    FALSE,
    (SELECT id FROM users WHERE username = 'admin'),
    NOW() - INTERVAL '10 days',
    NOW() - INTERVAL '10 days'
),
(
    'Redis Cache Not Persisted',
    'Redis is configured without AOF or RDB persistence. A Redis restart flushes all cached data, causing a thundering-herd problem as all requests hit the database simultaneously.',
    'Operational',
    58.0,
    FALSE,
    (SELECT id FROM users WHERE username = 'admin'),
    NOW() - INTERVAL '9 days',
    NOW() - INTERVAL '9 days'
),
(
    'No Health Check on Downstream Email Service',
    'The application does not verify SMTP connectivity at startup. Email failures are silently swallowed, and operations teams have no alerting when the mail service is down.',
    'Operational',
    45.0,
    FALSE,
    (SELECT id FROM users WHERE username = 'admin'),
    NOW() - INTERVAL '8 days',
    NOW() - INTERVAL '8 days'
),
(
    'Insufficient Container Resource Limits',
    'Docker containers are deployed without CPU and memory limits. A single misbehaving container can starve other services on the same host.',
    'Operational',
    40.0,
    FALSE,
    (SELECT id FROM users WHERE username = 'admin'),
    NOW() - INTERVAL '7 days',
    NOW() - INTERVAL '7 days'
),

-- ── FINANCIAL ─────────────────────────────────────────────────────────────────
(
    'Uncontrolled Cloud Cost Escalation',
    'Auto-scaling is enabled without an upper bound on instance count. A traffic spike or DDoS attack can cause unbounded cloud spend before any alert fires.',
    'Financial',
    70.0,
    FALSE,
    (SELECT id FROM users WHERE username = 'admin'),
    NOW() - INTERVAL '6 days',
    NOW() - INTERVAL '6 days'
),
(
    'No Budget Alert for Third-Party API Usage',
    'The application consumes a paid AI scoring API with no usage cap or budget alert configured. Unexpected traffic can result in significant unplanned expenditure.',
    'Financial',
    52.0,
    FALSE,
    (SELECT id FROM users WHERE username = 'admin'),
    NOW() - INTERVAL '5 days',
    NOW() - INTERVAL '5 days'
),

-- ── REPUTATIONAL ──────────────────────────────────────────────────────────────
(
    'Public Exposure of Internal Risk Scores',
    'Risk scores for internal projects are accessible via the public API without authentication when the JWT filter is misconfigured. Competitors could access sensitive strategic data.',
    'Reputational',
    78.0,
    FALSE,
    (SELECT id FROM users WHERE username = 'admin'),
    NOW() - INTERVAL '4 days',
    NOW() - INTERVAL '4 days'
),
(
    'Misleading Risk Score Calculation',
    'The residual risk score algorithm does not account for existing controls. Scores are presented to stakeholders as final residual risk, potentially leading to under-investment in mitigations.',
    'Reputational',
    65.0,
    FALSE,
    (SELECT id FROM users WHERE username = 'admin'),
    NOW() - INTERVAL '3 days',
    NOW() - INTERVAL '3 days'
),

-- ── TECHNICAL DEBT ────────────────────────────────────────────────────────────
(
    'Deprecated Library Dependencies',
    'Three direct dependencies have known CVEs and have not been updated in over 12 months. The security team has flagged these in the last two vulnerability scans.',
    'Technical Debt',
    60.0,
    FALSE,
    (SELECT id FROM users WHERE username = 'admin'),
    NOW() - INTERVAL '2 days',
    NOW() - INTERVAL '2 days'
),
(
    'No API Versioning Strategy',
    'All endpoints are exposed under /api/ with no version prefix. Any breaking change to the API contract will immediately impact all consumers with no migration path.',
    'Technical Debt',
    35.0,
    FALSE,
    (SELECT id FROM users WHERE username = 'admin'),
    NOW() - INTERVAL '1 day',
    NOW() - INTERVAL '1 day'
),

-- ── MITIGATED (soft-deleted) — shows soft delete works ────────────────────────
(
    'Plaintext Passwords in Legacy Import',
    'A legacy data import script stored passwords in plaintext. This has been remediated — all passwords re-hashed with BCrypt.',
    'Security',
    95.0,
    TRUE,
    (SELECT id FROM users WHERE username = 'admin'),
    NOW() - INTERVAL '60 days',
    NOW() - INTERVAL '45 days'
);
