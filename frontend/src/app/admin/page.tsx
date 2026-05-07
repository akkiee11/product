"use client";

import { useEffect, useState } from "react";
import {
  fetchAdminSessions,
  fetchAdminStats,
  type AdminSession,
  type AdminStats,
} from "@/lib/api";
import { lakh } from "@/lib/format";

const TOKEN_KEY = "rinmukt:admin-token";

export default function AdminPage() {
  const [token, setToken] = useState<string | null>(null);
  const [tokenInput, setTokenInput] = useState("");
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [sessions, setSessions] = useState<AdminSession[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (typeof window === "undefined") return;
    const saved = window.localStorage.getItem(TOKEN_KEY);
    if (saved) setToken(saved);
  }, []);

  useEffect(() => {
    if (!token) return;
    let cancelled = false;
    setLoading(true);
    setError(null);
    Promise.all([fetchAdminStats(token), fetchAdminSessions(token)])
      .then(([s, list]) => {
        if (cancelled) return;
        setStats(s);
        setSessions(list);
      })
      .catch((e: unknown) => {
        if (cancelled) return;
        setError(e instanceof Error ? e.message : "Failed to load");
        if (e instanceof Error && e.message.toLowerCase().includes("invalid")) {
          window.localStorage.removeItem(TOKEN_KEY);
          setToken(null);
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [token]);

  const onSubmitToken = (e: React.FormEvent) => {
    e.preventDefault();
    const t = tokenInput.trim();
    if (!t) return;
    window.localStorage.setItem(TOKEN_KEY, t);
    setToken(t);
  };

  const onLogout = () => {
    window.localStorage.removeItem(TOKEN_KEY);
    setToken(null);
    setStats(null);
    setSessions([]);
    setTokenInput("");
  };

  if (!token) {
    return (
      <main className="mx-auto flex min-h-screen max-w-md flex-col justify-center px-6">
        <h1 className="text-2xl font-bold">Rinmukt admin</h1>
        <p className="mt-2 text-sm text-gray-600">Enter your admin token to continue.</p>
        <form onSubmit={onSubmitToken} className="mt-6 space-y-3">
          <input
            type="password"
            value={tokenInput}
            onChange={(e) => setTokenInput(e.target.value)}
            placeholder="X-Admin-Token"
            className="w-full rounded-lg border border-gray-300 px-3 py-2.5 focus:border-accent focus:outline-none focus:ring-1 focus:ring-accent"
            autoFocus
          />
          <button
            type="submit"
            className="w-full rounded-lg bg-ink py-2.5 font-semibold text-white"
          >
            Sign in
          </button>
        </form>
        {error && <p className="mt-3 text-sm text-red-600">{error}</p>}
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-6xl px-6 py-10">
      <header className="mb-8 flex items-baseline justify-between">
        <div>
          <p className="text-sm font-semibold text-accent">Rinmukt · ऋणमुक्त</p>
          <h1 className="mt-1 text-3xl font-bold">Founder dashboard</h1>
        </div>
        <button onClick={onLogout} className="text-sm text-gray-500 hover:text-ink">
          Sign out
        </button>
      </header>

      {loading && !stats && <p className="text-gray-500">Loading…</p>}
      {error && <p className="text-red-600">{error}</p>}

      {stats && (
        <>
          <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <Tile label="Reports today" value={stats.today.toString()} />
            <Tile label="Last 7 days" value={stats.last7Days.toString()} />
            <Tile label="Total reports" value={stats.total.toString()} />
            <Tile
              label="Email capture rate"
              value={pct(stats.withEmail, Math.max(stats.last7Days, 1))}
              hint={`${stats.withEmail} with email of last ${Math.min(stats.last7Days, 1000)} sessions`}
            />
          </section>

          <section className="mt-8 grid gap-6 md:grid-cols-2">
            <Breakdown title="By recommended path" data={stats.byPath} />
            <Breakdown title="By health label" data={stats.byHealthLabel} />
          </section>

          <section className="mt-8 grid gap-4 rounded-xl border border-gray-200 p-5 sm:grid-cols-3">
            <KPI label="Welcome emails sent" value={stats.welcomeSent} />
            <KPI label="Day-7 follow-ups sent" value={stats.day7Sent} />
            <KPI label="Pending day-7 (estimate)" value={Math.max(stats.welcomeSent - stats.day7Sent, 0)} />
          </section>

          <section className="mt-10">
            <h2 className="mb-4 text-xl font-bold">Recent reports</h2>
            <div className="overflow-x-auto rounded-xl border border-gray-200">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b bg-gray-50 text-left text-xs uppercase tracking-wide text-gray-500">
                    <th className="px-3 py-2">When</th>
                    <th className="px-3 py-2">Email</th>
                    <th className="px-3 py-2">Score</th>
                    <th className="px-3 py-2">Total debt</th>
                    <th className="px-3 py-2">Recommended</th>
                    <th className="px-3 py-2">Emails</th>
                    <th className="px-3 py-2">Link</th>
                  </tr>
                </thead>
                <tbody>
                  {sessions.map((s) => (
                    <tr key={s.id} className="border-b align-top last:border-0">
                      <td className="px-3 py-2 text-gray-700">{formatTime(s.createdAt)}</td>
                      <td className="px-3 py-2 text-gray-700">{s.email ?? "—"}</td>
                      <td className="px-3 py-2 font-medium">
                        {s.healthScore ?? "—"}
                        {s.healthLabel && (
                          <span className="ml-1 text-xs text-gray-500">({s.healthLabel})</span>
                        )}
                      </td>
                      <td className="px-3 py-2">
                        {s.totalDebt != null ? lakh(s.totalDebt) : "—"}
                      </td>
                      <td className="px-3 py-2 text-gray-700">{s.recommendedPath ?? "—"}</td>
                      <td className="px-3 py-2">
                        <span className={s.welcomeSent ? "text-money" : "text-gray-400"}>W</span>
                        <span className="mx-1 text-gray-300">·</span>
                        <span className={s.day7Sent ? "text-money" : "text-gray-400"}>D7</span>
                      </td>
                      <td className="px-3 py-2">
                        <a
                          href={`/r/${s.id}`}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-accent hover:underline"
                        >
                          open →
                        </a>
                      </td>
                    </tr>
                  ))}
                  {sessions.length === 0 && (
                    <tr>
                      <td colSpan={7} className="px-3 py-6 text-center text-gray-500">
                        No reports yet.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </section>
        </>
      )}
    </main>
  );
}

function Tile({ label, value, hint }: { label: string; value: string; hint?: string }) {
  return (
    <div className="rounded-xl border border-gray-200 p-5">
      <p className="text-xs uppercase tracking-wide text-gray-500">{label}</p>
      <p className="mt-1 text-3xl font-bold">{value}</p>
      {hint && <p className="mt-1 text-xs text-gray-500">{hint}</p>}
    </div>
  );
}

function KPI({ label, value }: { label: string; value: number }) {
  return (
    <div>
      <p className="text-xs text-gray-500">{label}</p>
      <p className="text-xl font-semibold">{value}</p>
    </div>
  );
}

function Breakdown({ title, data }: { title: string; data: Record<string, number> }) {
  const total = Object.values(data).reduce((a, b) => a + b, 0);
  const entries = Object.entries(data).sort((a, b) => b[1] - a[1]);
  return (
    <div className="rounded-xl border border-gray-200 p-5">
      <h3 className="text-sm font-semibold uppercase tracking-wide text-gray-500">{title}</h3>
      {entries.length === 0 && <p className="mt-3 text-sm text-gray-500">No data yet.</p>}
      <ul className="mt-3 space-y-2">
        {entries.map(([k, v]) => (
          <li key={k}>
            <div className="flex items-baseline justify-between text-sm">
              <span className="text-gray-700">{k}</span>
              <span className="font-medium">
                {v} ({total > 0 ? Math.round((v / total) * 100) : 0}%)
              </span>
            </div>
            <div className="mt-1 h-1.5 w-full rounded-full bg-gray-100">
              <div
                className="h-1.5 rounded-full bg-accent"
                style={{ width: total > 0 ? `${(v / total) * 100}%` : "0%" }}
              />
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}

function pct(num: number, den: number) {
  if (!den) return "—";
  return `${Math.round((num / den) * 100)}%`;
}

function formatTime(iso: string | null) {
  if (!iso) return "—";
  try {
    const d = new Date(iso);
    return d.toLocaleString("en-IN", {
      day: "numeric",
      month: "short",
      hour: "2-digit",
      minute: "2-digit",
    });
  } catch {
    return iso;
  }
}
