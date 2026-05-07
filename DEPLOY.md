# Deploy Rinmukt to the public internet — 20 minute guide

You will end with two URLs:
- **Frontend** (Vercel): `https://rinmukt-xyz.vercel.app`
- **Backend** (Render): `https://rinmukt-backend.onrender.com`

Both are free tiers. No credit card required for either.

---

## Prerequisites (5 min)

- [ ] **GitHub account** — already have it (the repo is in your account).
- [ ] The branch `claude/brainstorm-saas-product-cE0Hp` is pushed to GitHub. ✅

---

## Part 1 — Deploy backend on Render (8 min)

### 1.1 Sign up
1. Open https://render.com
2. Click **Get Started** → **GitHub** (auth with your GitHub account)
3. Approve Render to access your repos

### 1.2 Create the web service
1. Dashboard → **New +** → **Web Service**
2. Click **Connect GitHub** if prompted, then select **`akshaypokharkar11/product`**
3. Configure:
   - **Name:** `rinmukt-backend`
   - **Branch:** `claude/brainstorm-saas-product-cE0Hp`
   - **Root Directory:** `backend`
   - **Runtime:** `Docker` (auto-detected from `backend/Dockerfile`)
   - **Plan:** `Free`
4. Scroll to **Environment Variables** → Add:
   - `SPRING_PROFILES_ACTIVE` = `dev`
   - `CORS_ALLOWED_ORIGINS` = `http://localhost:3000,https://*.vercel.app`
5. Click **Create Web Service**

### 1.3 Wait for build (~5 min)
Watch the logs. Done when you see:
```
Started Application in X.XXX seconds
==> Your service is live 🎉
```

### 1.4 Verify
```bash
curl https://rinmukt-backend.onrender.com/health
# Expected: {"service":"rinmukt-backend","status":"ok"}
```

**Copy your Render URL.** You'll need it for Vercel in a moment.

> **Free tier note:** Render free services sleep after 15 min of inactivity. First request after sleep takes ~30 sec to wake up. Fine for V0 — upgrade to Starter ($7/mo) before launching publicly.

---

## Part 2 — Deploy frontend on Vercel (5 min)

### 2.1 Sign up
1. Open https://vercel.com/signup
2. Click **Continue with GitHub** (auth with your GitHub account)
3. Approve Vercel access

### 2.2 Import the project
1. Dashboard → **Add New...** → **Project**
2. Find **`akshaypokharkar11/product`** → **Import**
3. Configure:
   - **Framework Preset:** Next.js (auto-detected)
   - **Root Directory:** click **Edit** → set to `frontend`
   - **Build Command:** leave default (`next build`)
   - **Output Directory:** leave default
4. Expand **Environment Variables** → Add:
   - **Key:** `NEXT_PUBLIC_API_URL`
   - **Value:** *paste your Render URL from Part 1.4*
5. Click **Deploy**

### 2.3 Wait for build (~2 min)
Done when Vercel shows the confetti screen with your URL.

### 2.4 Verify
1. Open the URL Vercel gave you (e.g. `https://rinmukt-xyz.vercel.app`)
2. Click **Start my Debt MRI** → fill in test data → submit
3. Report page should appear with 5 paths

> **First request is slow** because the Render backend may be cold. Subsequent requests are instant.

---

## Part 3 — Lock down CORS (2 min)

Right now backend allows all `*.vercel.app`. For your final domain:

1. Note your Vercel URL (e.g. `rinmukt-xyz.vercel.app`)
2. Render dashboard → `rinmukt-backend` → **Environment** → edit `CORS_ALLOWED_ORIGINS`:
   ```
   https://rinmukt-xyz.vercel.app,https://*.vercel.app
   ```
3. Save → service auto-redeploys (~2 min)

---

## Part 4 — Custom domain (optional, 5 min)

When you buy `rinmukt.in`:

### Frontend (Vercel)
1. Vercel project → **Settings** → **Domains** → add `rinmukt.in` and `www.rinmukt.in`
2. Update DNS at your registrar (Vercel shows the exact records)

### Backend (Render)
1. Buy `api.rinmukt.in` as a subdomain → Render service → **Settings** → **Custom Domains** → add `api.rinmukt.in`
2. Update CNAME at your registrar

### Update env vars
- Vercel `NEXT_PUBLIC_API_URL` → `https://api.rinmukt.in`
- Render `CORS_ALLOWED_ORIGINS` → `https://rinmukt.in,https://www.rinmukt.in`

---

## Troubleshooting

| Symptom | Fix |
|---|---|
| Render build fails on `mvn dependency:go-offline` | Network blip — click **Manual Deploy** in Render |
| Frontend says "Failed to fetch" | Backend is sleeping — visit the `/health` URL once to wake it |
| CORS error in browser console | Add your Vercel URL to `CORS_ALLOWED_ORIGINS` env var on Render |
| Form submits but report is blank | Open browser console — usually a JSON parse error from a failed API call |
| Render free tier slow | Expected — upgrade to Starter ($7/mo) when you have real traffic |

---

## What to do RIGHT after deploy

1. Open the live URL on your phone, complete a Debt MRI for yourself
2. Screenshot the report
3. Post the screenshots + URL on LinkedIn with your founder story
4. Watch your inbox

When the first DM lands → you have a startup.
