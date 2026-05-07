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

// Typical APR for each debt type in India. User can override.
const DEFAULT_RATES: Record<DebtType, number> = {
  CREDIT_CARD: 0.42,
  PERSONAL_LOAN: 0.16,
  BNPL: 0.3,
  HOME_LOAN: 0.09,
  AUTO_LOAN: 0.1,
  EDUCATION_LOAN: 0.11,
  GOLD_LOAN: 0.12,
  INFORMAL: 0,
  OTHER: 0.15,
};

const inrFormat = new Intl.NumberFormat("en-IN");

const blankDebt = (type: DebtType = "CREDIT_CARD"): DebtInput => ({
  type,
  lender: "",
  outstanding: 0,
  interestRate: DEFAULT_RATES[type],
  emi: 0,
  monthsLeft: 0,
});

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
    debts: [blankDebt()],
  });

  const setDebt = (idx: number, patch: Partial<DebtInput>) => {
    setForm((f) => ({
      ...f,
      debts: f.debts.map((d, i) => {
        if (i !== idx) return d;
        const next = { ...d, ...patch };
        // When debt type changes, refresh interest rate to the new type's default
        if (patch.type && patch.type !== d.type) {
          next.interestRate = DEFAULT_RATES[patch.type];
        }
        return next;
      }),
    }));
  };

  const addDebt = () => setForm((f) => ({ ...f, debts: [...f.debts, blankDebt()] }));
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
          <MoneyField
            label="Monthly take-home"
            value={form.monthlyIncome}
            onChange={(v) => setForm((f) => ({ ...f, monthlyIncome: v }))}
          />
          <MoneyField
            label="Essential expenses"
            hint="Rent + food + family commitments"
            value={form.monthlyExpenses}
            onChange={(v) => setForm((f) => ({ ...f, monthlyExpenses: v }))}
          />
        </Section>

        <Section title="2. Your debts" subtitle="Add every debt — including BNPL and family loans.">
          <div className="space-y-4">
            {form.debts.map((d, idx) => (
              <DebtCard
                key={idx}
                idx={idx}
                debt={d}
                onChange={(patch) => setDebt(idx, patch)}
                onRemove={form.debts.length > 1 ? () => removeDebt(idx) : undefined}
              />
            ))}
          </div>
          <button
            type="button"
            onClick={addDebt}
            className="mt-4 w-full rounded-lg border-2 border-dashed border-gray-300 px-4 py-3 text-sm font-medium text-gray-600 hover:border-accent hover:text-accent"
          >
            + Add another debt
          </button>
        </Section>

        <Section title="3. About you">
          <div className="grid gap-4 md:grid-cols-2">
            <NumberField
              label="Your age"
              value={form.age ?? 0}
              onChange={(v) => setForm((f) => ({ ...f, age: v }))}
            />
            <NumberField
              label="CIBIL score"
              hint="300–900, leave 0 if unknown"
              value={form.cibilScore ?? 0}
              onChange={(v) => setForm((f) => ({ ...f, cibilScore: v }))}
            />
          </div>
          <Checkbox
            label="I can realistically increase my income in next 12 months"
            checked={!!form.canIncreaseIncome}
            onChange={(v) => setForm((f) => ({ ...f, canIncreaseIncome: v }))}
          />
          <Checkbox
            label="I have at least one account in default / 90+ days overdue"
            checked={!!form.hasDefault}
            onChange={(v) => setForm((f) => ({ ...f, hasDefault: v }))}
          />
        </Section>

        <Section title="4. Where to send your report (optional)">
          <input
            type="email"
            placeholder="you@example.com"
            value={form.email ?? ""}
            onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
            className="w-full rounded-lg border border-gray-300 px-3 py-2.5 focus:border-accent focus:outline-none focus:ring-1 focus:ring-accent"
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

function FieldShell({
  label,
  hint,
  children,
}: {
  label: string;
  hint?: string;
  children: React.ReactNode;
}) {
  return (
    <label className="block">
      <div className="flex items-baseline justify-between">
        <span className="text-sm font-medium text-gray-700">{label}</span>
        {hint && <span className="text-xs text-gray-400">{hint}</span>}
      </div>
      <div className="mt-1">{children}</div>
    </label>
  );
}

function NumberField({
  label,
  hint,
  value,
  onChange,
}: {
  label: string;
  hint?: string;
  value: number;
  onChange: (v: number) => void;
}) {
  return (
    <FieldShell label={label} hint={hint}>
      <input
        type="number"
        min={0}
        inputMode="numeric"
        value={value || ""}
        onChange={(e) => onChange(Number(e.target.value))}
        className="w-full rounded-lg border border-gray-300 px-3 py-2.5 focus:border-accent focus:outline-none focus:ring-1 focus:ring-accent"
      />
    </FieldShell>
  );
}

function MoneyField({
  label,
  hint,
  value,
  onChange,
}: {
  label: string;
  hint?: string;
  value: number;
  onChange: (v: number) => void;
}) {
  return (
    <FieldShell label={label} hint={hint}>
      <div className="relative">
        <span className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-gray-500">
          ₹
        </span>
        <input
          type="number"
          min={0}
          inputMode="numeric"
          value={value || ""}
          onChange={(e) => onChange(Number(e.target.value))}
          className="w-full rounded-lg border border-gray-300 py-2.5 pl-7 pr-3 focus:border-accent focus:outline-none focus:ring-1 focus:ring-accent"
        />
      </div>
      {value > 0 && (
        <p className="mt-1 text-xs text-gray-500">₹{inrFormat.format(value)}</p>
      )}
    </FieldShell>
  );
}

function PercentField({
  label,
  hint,
  value,
  onChange,
}: {
  label: string;
  hint?: string;
  value: number; // decimal (0.42)
  onChange: (decimal: number) => void;
}) {
  // User types percent (e.g. "42"), we store decimal.
  const display = value > 0 ? +(value * 100).toFixed(2) : "";
  return (
    <FieldShell label={label} hint={hint}>
      <div className="relative">
        <input
          type="number"
          min={0}
          step={0.5}
          inputMode="decimal"
          value={display}
          onChange={(e) => {
            const pct = Number(e.target.value);
            onChange(pct / 100);
          }}
          className="w-full rounded-lg border border-gray-300 py-2.5 pl-3 pr-8 focus:border-accent focus:outline-none focus:ring-1 focus:ring-accent"
        />
        <span className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-3 text-gray-500">
          %
        </span>
      </div>
    </FieldShell>
  );
}

function Checkbox({
  label,
  checked,
  onChange,
}: {
  label: string;
  checked: boolean;
  onChange: (v: boolean) => void;
}) {
  return (
    <label className="flex items-start gap-2 text-sm text-gray-700">
      <input
        type="checkbox"
        checked={checked}
        onChange={(e) => onChange(e.target.checked)}
        className="mt-0.5 h-4 w-4 rounded border-gray-300 text-accent focus:ring-accent"
      />
      <span>{label}</span>
    </label>
  );
}

function DebtCard({
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
  const isRevolvingCC = debt.type === "CREDIT_CARD";
  return (
    <div className="rounded-xl border border-gray-200 bg-gray-50 p-5">
      <div className="mb-4 flex items-center justify-between">
        <span className="rounded-md bg-ink px-2.5 py-1 text-xs font-semibold text-white">
          Debt #{idx + 1}
        </span>
        {onRemove && (
          <button
            type="button"
            onClick={onRemove}
            className="text-xs text-red-600 hover:underline"
          >
            Remove
          </button>
        )}
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <FieldShell label="Type">
          <select
            value={debt.type}
            onChange={(e) => onChange({ type: e.target.value as DebtType })}
            className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 focus:border-accent focus:outline-none focus:ring-1 focus:ring-accent"
          >
            {DEBT_TYPES.map((t) => (
              <option key={t.value} value={t.value}>
                {t.label}
              </option>
            ))}
          </select>
        </FieldShell>

        <FieldShell label="Lender">
          <input
            type="text"
            placeholder="e.g. HDFC, SBI, Bajaj Finserv"
            value={debt.lender}
            onChange={(e) => onChange({ lender: e.target.value })}
            className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 focus:border-accent focus:outline-none focus:ring-1 focus:ring-accent"
          />
        </FieldShell>

        <MoneyField
          label="Outstanding amount"
          value={debt.outstanding}
          onChange={(v) => onChange({ outstanding: v })}
        />

        <PercentField
          label="Interest rate (APR)"
          hint={`Typical: ${(DEFAULT_RATES[debt.type] * 100).toFixed(0)}%`}
          value={debt.interestRate}
          onChange={(v) => onChange({ interestRate: v })}
        />

        <MoneyField
          label={isRevolvingCC ? "Min payment / month" : "EMI / month"}
          value={debt.emi}
          onChange={(v) => onChange({ emi: v })}
        />

        {!isRevolvingCC && (
          <NumberField
            label="Months remaining"
            value={debt.monthsLeft}
            onChange={(v) => onChange({ monthsLeft: v })}
          />
        )}
      </div>
    </div>
  );
}
