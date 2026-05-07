# Rinmukt — Debt MRI

An honest, free-forever Debt MRI for Indian borrowers. Compares 5 paths out of debt
(Smart Path, Aggressive Smart, DIY Settlement, Status Quo, Freed-style Full Settlement)
ranked by **total cost AND credit impact** — not just cash paid.

V0 ships:
- Calculation engine (5 strategies, deterministic, unit-tested on the founder's own debt)
- REST API (`POST /api/mri`)
- Next.js landing + multi-section form + report page
- Postgres schema (Flyway), but persistence is opt-in for production

Deferred to V1+: Account Aggregator, AI narrative, PDF generation, payments, WhatsApp bot.

## Repo layout

```
backend/    Spring Boot 3.3 + Java 21
frontend/   Next.js 15 + TypeScript + Tailwind
```

## Run locally

### Backend (port 8080)
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# dev profile disables JPA/Flyway so it boots with no DB
```

Verify:
```bash
curl http://localhost:8080/health
```

Run tests:
```bash
mvn test                                   # all tests
mvn test -Dtest=FounderCaseTest            # the regression suite
mvn test -Dtest=FounderCaseSnapshotTest    # prints engine output for the founder case
```

### Frontend (port 3000)
```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:3000.

The frontend calls `NEXT_PUBLIC_API_URL` (defaults to `http://localhost:8080`).

## Deploying

**Frontend → Vercel** — connect repo, set root to `frontend/`, set
`NEXT_PUBLIC_API_URL=https://api.rinmukt.in`.

**Backend → Render (free tier)** — set root to `backend/`, build command
`mvn -DskipTests package`, start command
`java -jar target/rinmukt-backend-0.0.1-SNAPSHOT.jar`.
Set `SPRING_PROFILES_ACTIVE=prod` and the `DATABASE_*` env vars from Supabase.

**Database → Supabase Postgres** — Flyway runs migrations from
`backend/src/main/resources/db/migration/` automatically on first boot.

## Architecture

```
Browser (Vercel)
    │  POST /api/mri  { income, expenses, debts[], cibil, ... }
    ▼
Spring Boot (Render)
    │
    ├── CalculationEngine
    │       ├── StatusQuoStrategy
    │       ├── SmartPathStrategy            ← recommended for most users
    │       ├── AggressiveSmartPathStrategy
    │       ├── DiySettlementStrategy
    │       └── FullSettlementStrategy       ← "Freed-style", honest costing
    │
    ├── (V1+) AIService → Claude Haiku for narrative
    ├── (V1+) PdfService
    └── Postgres (sessions, leads, consultations)
```

## The calculation engine

Every strategy implements `DebtStrategy.calculate(profile) → PathResult`.
The engine ranks paths by `totalCashOut + cibilPenalty + taxExposure`, where the
penalty is `(750 - cibilAfter) × ₹5,000` per CIBIL point lost. This is what
prevents the engine from naively recommending Freed-style settlement just because
the *cash* number is lower.

Edit `CalculationEngine.scoreCost(...)` to tune the model.

## Why the founder case is in the test suite

`FounderCaseTest` encodes the founder's own debt situation — ₹22.6 L across 4 CCs and
4 PLs at age 26 with CIBIL 651. If a refactor ever breaks the recommended path for
this profile, CI fails. User-zero is a regression test.

## Roadmap

- **V0 (now):** engine + form + report. ✅
- **V1:** persistence, AI narrative, PDF, Razorpay (₹499 consultations).
- **V2:** WhatsApp community, phone scripts auto-generated, balance-transfer marketplace.
- **V3:** Account Aggregator integration, auto-CIBIL pull, voice negotiation agent.

## Disclaimer

Rinmukt is an information & advisory tool. We are not a registered debt counsellor,
banking entity, or NBFC. Always consult a qualified CA or lawyer before settling
or restructuring any debt.
