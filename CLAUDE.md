# Rinmukt — project memory for Claude Code

This file is auto-loaded by Claude Code (web and CLI) at the start of every
session in this repo. **Read it first** before doing anything in this codebase.

---

## What we're building

**Rinmukt** ("ऋणमुक्त" — debt-free) is an honest, free Debt MRI tool for
Indian borrowers. It compares 5 paths out of debt and ranks them by
**total cost AND CIBIL/tax impact** — not just cash paid. Positioned
explicitly as **the honest alternative to Freed** (the Indian debt-settlement
unicorn).

### Founder's thesis
- ~14 crore Indians have EMI stress; ~5–7 crore in active distress; <2% seek help.
- Existing players (Freed, Single Debt, Credit Sudhaar) push settlement,
  which crashes CIBIL for 7 years AND triggers a tax bomb on waived debt
  — costs that competitors hide.
- Most users (especially salaried, current on payments) have **better paths**
  they don't know about: convert credit-card revolving balance to EMI at
  16%, refinance, balance transfer, avalanche payoff.
- Rinmukt's edge: model all 5 paths transparently and recommend honestly.

### Target user (V0)
Salaried Indian, age 22–45, ₹50k–₹3L/mo income, ₹3L–₹50L total debt across
cards + personal loans, current on payments but stressed, CIBIL 600–750.

### Goals
- **3-month:** ₹50k–₹80k MRR via ₹499 1:1 consultations.
- **18-month:** ₹1 Cr MRR.
- **5-year:** "Aadhaar of financial recovery" for India.

---

## Repo layout

```
backend/    Spring Boot 3.3 + Java 21       (the calculation engine + REST API)
frontend/   Next.js 15 + TypeScript + Tailwind  (landing + form + report page)
DEPLOY.md   step-by-step Render + Vercel deploy guide
README.md   architecture overview + run-locally instructions
```

---

## Current state (as of last session)

- ✅ V0 backend: 5 strategies, REST API, 9 unit tests passing.
- ✅ V0 frontend: landing, form, report page, prod build clean.
- ✅ Deployment config: Dockerfile, render.yaml, env-driven CORS.
- ⏳ Not yet deployed publicly — sandbox network can't reach
  api.vercel.com or api.render.com, so the deploy must run from
  the founder's local CLI (`claude --teleport`) or via the UI.
- ❌ V1 features deferred: AI narrative, PDF export, Razorpay
  payments, Account Aggregator, voice negotiation agent.

The branch we work on is `claude/brainstorm-saas-product-cE0Hp`.

---

## The calculation engine — core IP

Lives in `backend/src/main/java/in/rinmukt/service/`. Five `DebtStrategy`
implementations:

| Strategy | What it does | Typical outcome for the target user |
|---|---|---|
| `StatusQuoStrategy` | Min CC payments, regular EMIs | 8–20 yrs, highest cash cost |
| `SmartPathStrategy` | **Recommended for most users.** Convert all CC outstanding to 16% EMI / 36 mo, then avalanche surplus to highest-rate active debt | ~3 yrs, CIBIL improves |
| `AggressiveSmartPathStrategy` | Smart Path + ₹20k/mo income increase | ~2.5 yrs |
| `DiySettlementStrategy` | Selectively settle CCs only | CIBIL drops to ~580 for 2–3 yrs, tax exposure |
| `FullSettlementStrategy` | "Freed-style" full settlement | Cheapest cash, worst CIBIL (~400 for 7 yrs), tax bomb on waived debt |

### Ranking logic (`CalculationEngine.scoreCost`)
```
score = totalCashOut + (750 - cibilAfter) × 5,000 + taxExposure
```
The CIBIL penalty (₹5k per point lost vs. 750) is the reason Freed-style
settlement does **not** win even when its raw cash number is lowest. This
is the philosophical heart of the product. Tune carefully; it's user-facing.

### Health Score (`CalculationEngine.healthScore`)
0–100 score derived from debt-to-income ratio, toxic-debt fraction
(rate >25%), surplus, CIBIL, and default flag. Labels:
- 75+ HEALTHY · 50–74 MANAGEABLE · 30–49 RISKY · <30 CRITICAL.

---

## Important sensitivity: the founder's own debt is in the test suite

`backend/src/test/java/in/rinmukt/service/FounderCaseTest.java` and
`FounderCaseSnapshotTest.java` encode the founder's actual debt situation
(₹22.6 L across 4 CCs and 4 PLs, age 26, CIBIL 651). This is intentional —
user-zero is a regression test — but if the repo ever goes **public**, scrub
or anonymize these files first. The engine works correctly without them;
they're tests, not runtime code.

---

## Key product decisions (don't unwind these without discussion)

1. **Free forever for the report.** Monetize via ₹499 1:1 consultations,
   later via outcome-based fees on actual settlements.
2. **Never recommend more debt to solve debt.** No upsell of personal loans,
   no affiliate kickbacks for credit cards.
3. **Show the truth about Freed.** Always include `FullSettlementStrategy`
   in the comparison with all hidden costs (fees, tax, CIBIL) priced in.
4. **CC-to-EMI is the primary wedge.** Most Indians don't know banks offer
   "balance to EMI" at ~16% on revolving CC outstanding. Educating them
   is the single highest-leverage thing the product does.
5. **Manual debt entry in V0.** Account Aggregator (regulated, requires FIU
   license) is V3. Don't waste time fighting compliance until product-market
   fit is proven.

---

## What to do next (priority order)

1. **Deploy V0 publicly.** See `DEPLOY.md`. Two options:
   - Founder teleports to local CLI (`claude --teleport`) and runs the
     deploy from there with full Vercel CLI / Render API access.
   - Click through Render and Vercel UIs.
2. **Founder's LinkedIn launch post.** Anonymized debt story + tool URL.
   Goal: 100 reports completed, 10 paid consultations in week 1.
3. **V1 build queue** (in this rough order):
   - PDF export of the report (Puppeteer or iText)
   - Claude Haiku integration for AI narrative + phone scripts
   - Razorpay integration for ₹499 consultation booking
   - WhatsApp community auto-add on form submit
4. **Brand work:** logo, domain (`rinmukt.in` preferred), favicon, OG image.

---

## Stack & conventions

- **Java:** 21 · **Spring Boot:** 3.3 · **Maven** · Lombok ok
- **Frontend:** Next.js 15 App Router · TypeScript · Tailwind · no UI lib yet
- **DB:** Postgres on Supabase (prod profile only); dev profile boots without DB
- **Tests:** JUnit 5 + AssertJ. Always update `FounderCaseTest` if engine
  semantics change — that's the contract with our user-zero.
- **Naming:** kebab-case for routes, camelCase TS, `in.rinmukt.*` Java packages.
- **Comments:** sparse. Only when WHY is non-obvious.

---

## How to run locally (quick)

```bash
# Terminal 1 — backend on :8080
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 2 — frontend on :3000
cd frontend
npm install && npm run dev
```

Then open http://localhost:3000.

Run tests:
```bash
cd backend && mvn test
```

---

## Useful past-session output (for new sessions joining)

The engine, run on the founder's actual debt:
```
Health: 36/100 (RISKY)
Recommended: AGGRESSIVE_SMART_PATH

#1 Aggressive Smart Path:    ₹25.7 L | 29 mo | CIBIL 771
#2 Smart Path:               ₹26.6 L | 33 mo | CIBIL 761
#3 DIY Settlement:           ₹25.0 L | 47 mo | CIBIL 580 (tax exposure)
#4 Full Settlement (Freed):  ₹18.0 L | 84 mo | CIBIL 400 (tax bomb + 7 yr credit hell)
#5 Status Quo:               ₹37.4 L | 240 mo | CIBIL 651
```

This output is regression-tested in `FounderCaseSnapshotTest`. If your
changes meaningfully shift these numbers, that's a signal — not a bug.

---

*Memory file maintained by the founder + Claude Code. Last updated: V0 complete,
deployment pending. Anything not in this file lives in the original chat at
`https://claude.ai/code/session_01RUk6W4x9454BUY1xJktNZr` or in commit
history.*
