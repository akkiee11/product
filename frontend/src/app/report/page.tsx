"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import type { Report } from "@/lib/api";
import { lakh, months } from "@/lib/format";

export default function ReportPage() {
  const [report, setReport] = useState<Report | null>(null);

  useEffect(() => {
    const raw = sessionStorage.getItem("rinmukt:report");
    if (raw) setReport(JSON.parse(raw));
  }, []);

  if (!report) {
    return (
      <main className="mx-auto max-w-3xl p-10 text-center">
        <p>No report found. <Link className="underline" href="/start">Start a new MRI →</Link></p>
      </main>
    );
  }

  const r = report;
  const recommended = r.paths.find((p) => p.recommended) ?? r.paths[0];
  const scoreColor =
    r.healthScore >= 75 ? "text-money" : r.healthScore >= 50 ? "text-yellow-600" : "text-red-600";

  return (
    <main className="mx-auto max-w-4xl px-6 py-10">
      <header className="mb-8">
        <p className="text-sm text-gray-500">Your Debt MRI</p>
        <h1 className="mt-1 text-3xl font-bold">Health Score: <span className={scoreColor}>{r.healthScore}/100</span> ({r.healthLabel})</h1>
        <p className="mt-2 text-gray-700">{r.primaryConcern}</p>
      </header>

      <section className="grid gap-4 rounded-xl bg-gray-50 p-6 md:grid-cols-3">
        <Stat label="Total debt" value={lakh(r.totalDebt)} />
        <Stat label="Monthly outflow" value={lakh(r.monthlyOutflow)} />
        <Stat label="Debt-to-income" value={`${r.debtToIncomePercent.toFixed(0)}%`} />
      </section>

      <section className="mt-10">
        <h2 className="text-2xl font-bold">Recommended path</h2>
        <div className="mt-3 rounded-xl border-2 border-money bg-green-50 p-6">
          <p className="text-sm font-semibold uppercase text-money">#1 · {recommended.name}</p>
          <p className="mt-2 text-gray-800">{recommended.summary}</p>
          <div className="mt-4 grid gap-3 text-sm md:grid-cols-4">
            <Stat label="Total cash out" value={lakh(recommended.totalCashOut)} />
            <Stat label="Time to debt-free" value={months(recommended.monthsToFreedom)} />
            <Stat label="CIBIL after" value={String(recommended.cibilAfter)} />
            <Stat label="Tax exposure" value={lakh(recommended.taxExposure)} />
          </div>
        </div>
      </section>

      <section className="mt-10">
        <h2 className="text-2xl font-bold">All paths — compared honestly</h2>
        <div className="mt-4 overflow-x-auto">
          <table className="w-full border-collapse text-sm">
            <thead>
              <tr className="border-b bg-gray-50 text-left">
                <th className="p-3">Path</th>
                <th className="p-3">Total cost</th>
                <th className="p-3">Time</th>
                <th className="p-3">CIBIL</th>
                <th className="p-3">Tax</th>
              </tr>
            </thead>
            <tbody>
              {r.paths.map((p) => (
                <tr key={p.pathId} className="border-b align-top">
                  <td className="p-3">
                    <p className="font-medium">#{p.rank} {p.name}</p>
                    <p className="text-gray-500">{p.summary}</p>
                    {p.warning && (
                      <p className="mt-1 text-xs text-red-600">⚠ {p.warning}</p>
                    )}
                  </td>
                  <td className="p-3 font-medium">{lakh(p.totalCashOut)}</td>
                  <td className="p-3">{months(p.monthsToFreedom)}</td>
                  <td className="p-3">{p.cibilAfter}</td>
                  <td className="p-3">{p.taxExposure > 0 ? lakh(p.taxExposure) : "—"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <section className="mt-10 rounded-xl border border-gray-200 p-6">
        <h2 className="text-xl font-bold">Want a personalised 1-on-1?</h2>
        <p className="mt-2 text-gray-600">
          30-min call with the founder. Custom 36-month plan, exact phone scripts for your banks,
          tax planning. ₹499.
        </p>
        <button className="mt-4 rounded-lg bg-ink px-6 py-3 font-medium text-white">
          Book a session — coming soon
        </button>
      </section>

      <p className="mt-8 text-xs text-gray-500">
        Information &amp; advisory tool. Not a registered debt counsellor or lender. Verify all
        recommendations with a CA or qualified advisor before acting.
      </p>
    </main>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <p className="text-xs text-gray-500">{label}</p>
      <p className="text-lg font-semibold">{value}</p>
    </div>
  );
}
