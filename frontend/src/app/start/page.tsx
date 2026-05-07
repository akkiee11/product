"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import type { DebtInput, DebtType, MriRequest } from "@/lib/api";
import { generateMri } from "@/lib/api";

const DEBT_TYPES: { value: DebtType; label: string }[] = [
  { value: "CREDIT_CARD", label: "Credit Card" },
  { value: "PERSONAL_LOAN", label: "Personal Loan" },
  { value: "BNPL", label: "BNPL (Lazypay/Simpl)" },
  { value: "HOME_LOAN", label: "Home Loan" },
  { value: "AUTO_LOAN", label: "Car/Bike Loan" },
  { value: "EDUCATION_LOAN", label: "Education Loan" },
  { value: "GOLD_LOAN", label: "Gold Loan" },
  { value: "INFORMAL", label: "Family / Friends" },
  { value: "OTHER", label: "Other" },
];

const blankDebt: DebtInput = {
  type: "CREDIT_CARD",
  lender: "",
  outstanding: 0,
  interestRate: 0.42,
  emi: 0,
  monthsLeft: 0,
};

export default function StartPage() {
  const router = useRouter();
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [form, setForm] = useState<MriRequest>({
    monthlyIncome: 0,
    monthlyExpenses: 0,
    age: undefined,
    cibilScore: undefined,
    hasDefault: false,
    canIncreaseIncome: false,
    email: "",
    debts: [{ ...blankDebt }],
  });

  const setDebt = (idx: number, patch: Partial<DebtInput>) => {
    setForm((f) => ({
      ...f,
      debts: f.debts.map((d, i) => (i === idx ? { ...d, ...patch } : d)),
    }));
  };

  const addDebt = () => setForm((f) => ({ ...f, debts: [...f.debts, { ...blankDebt }] }));
  const removeDebt = (idx: number) =>
    setForm((f) => ({ ...f, debts: f.debts.filter((_, i) => i !== idx) }));

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const report = await generateMri(form);
      sessionStorage.setItem("rinmukt:report", JSON.stringify(report));
      router.push("/report");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong");
      setSubmitting(false);
    }
  };

  return (
    <main className="mx-auto max-w-3xl px-6 py-10">
      <h1 className="text-3xl font-bold">Your Debt MRI</h1>
      <p className="mt-2 text-gray-600">
        Fill in honestly. Your data stays on your device until you submit. Free, no login.
      </p>

      <form onSubmit={onSubmit} className="mt-8 space-y-8">
        <Section title="1. Income & expenses">
          <NumberField
            label="Monthly take-home (₹)"
            value={form.monthlyIncome}
            onChange={(v) => setForm((f) => ({ ...f, monthlyIncome: v }))}
          />
          <NumberField
            label="Essential expenses (rent + food + family) (₹)"
            value={form.monthlyExpenses}
            onChange={(v) => setForm((f) => ({ ...f, monthlyExpenses: v }))}
          />
        </Section>

        <Section title="2. Your debts" subtitle="Add every debt — including BNPL and family loans.">
          {form.debts.map((d, idx) => (
            <DebtRow
              key={idx}
              idx={idx}
              debt={d}
              onChange={(patch) => setDebt(idx, patch)}
              onRemove={form.debts.length > 1 ? () => removeDebt(idx) : undefined}
            />
          ))}
          <button
            type="button"
            onClick={addDebt}
            className="rounded border border-dashed border-gray-400 px-4 py-2 text-sm hover:bg-gray-50"
          >
            + Add another debt
          </button>
        </Section>

        <Section title="3. About you">
          <NumberField
            label="Your age"
            value={form.age ?? 0}
            onChange={(v) => setForm((f) => ({ ...f, age: v }))}
          />
          <NumberField
            label="CIBIL score (300-900, 0 if unknown)"
            value={form.cibilScore ?? 0}
            onChange={(v) => setForm((f) => ({ ...f, cibilScore: v }))}
          />
          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={!!form.canIncreaseIncome}
              onChange={(e) => setForm((f) => ({ ...f, canIncreaseIncome: e.target.checked }))}
            />
            I can realistically increase my income in next 12 months
          </label>
          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={!!form.hasDefault}
              onChange={(e) => setForm((f) => ({ ...f, hasDefault: e.target.checked }))}
            />
            I have at least one account in default / 90+ days overdue
          </label>
        </Section>

        <Section title="4. Where to send your report (optional)">
          <input
            type="email"
            placeholder="you@example.com"
            value={form.email ?? ""}
            onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
            className="w-full rounded border border-gray-300 px-3 py-2"
          />
        </Section>

        {error && (
          <div className="rounded border border-red-300 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-lg bg-ink py-3 font-semibold text-white disabled:opacity-50"
        >
          {submitting ? "Analysing your debt…" : "Generate My Debt MRI →"}
        </button>
      </form>
    </main>
  );
}

function Section({
  title,
  subtitle,
  children,
}: {
  title: string;
  subtitle?: string;
  children: React.ReactNode;
}) {
  return (
    <div className="rounded-xl border border-gray-200 p-6">
      <h2 className="text-lg font-semibold">{title}</h2>
      {subtitle && <p className="mt-1 text-sm text-gray-500">{subtitle}</p>}
      <div className="mt-4 space-y-3">{children}</div>
    </div>
  );
}

function NumberField({
  label,
  value,
  onChange,
}: {
  label: string;
  value: number;
  onChange: (v: number) => void;
}) {
  return (
    <label className="block">
      <span className="text-sm text-gray-700">{label}</span>
      <input
        type="number"
        min={0}
        value={value || ""}
        onChange={(e) => onChange(Number(e.target.value))}
        className="mt-1 w-full rounded border border-gray-300 px-3 py-2"
      />
    </label>
  );
}

function DebtRow({
  idx,
  debt,
  onChange,
  onRemove,
}: {
  idx: number;
  debt: DebtInput;
  onChange: (patch: Partial<DebtInput>) => void;
  onRemove?: () => void;
}) {
  return (
    <div className="rounded border border-gray-200 p-3">
      <div className="mb-2 flex items-center justify-between">
        <span className="text-sm font-medium">Debt #{idx + 1}</span>
        {onRemove && (
          <button type="button" onClick={onRemove} className="text-xs text-red-600 hover:underline">
            Remove
          </button>
        )}
      </div>
      <div className="grid gap-2 md:grid-cols-2">
        <select
          value={debt.type}
          onChange={(e) => onChange({ type: e.target.value as DebtType })}
          className="rounded border border-gray-300 px-3 py-2"
        >
          {DEBT_TYPES.map((t) => (
            <option key={t.value} value={t.value}>
              {t.label}
            </option>
          ))}
        </select>
        <input
          type="text"
          placeholder="Lender (e.g. HDFC CC)"
          value={debt.lender}
          onChange={(e) => onChange({ lender: e.target.value })}
          className="rounded border border-gray-300 px-3 py-2"
        />
        <input
          type="number"
          placeholder="Outstanding (₹)"
          value={debt.outstanding || ""}
          onChange={(e) => onChange({ outstanding: Number(e.target.value) })}
          className="rounded border border-gray-300 px-3 py-2"
        />
        <input
          type="number"
          step="0.01"
          placeholder="Interest rate (e.g. 0.42 for 42%)"
          value={debt.interestRate || ""}
          onChange={(e) => onChange({ interestRate: Number(e.target.value) })}
          className="rounded border border-gray-300 px-3 py-2"
        />
        <input
          type="number"
          placeholder="EMI / Min due (₹)"
          value={debt.emi || ""}
          onChange={(e) => onChange({ emi: Number(e.target.value) })}
          className="rounded border border-gray-300 px-3 py-2"
        />
        <input
          type="number"
          placeholder="Months left (0 for revolving CC)"
          value={debt.monthsLeft || ""}
          onChange={(e) => onChange({ monthsLeft: Number(e.target.value) })}
          className="rounded border border-gray-300 px-3 py-2"
        />
      </div>
    </div>
  );
}
