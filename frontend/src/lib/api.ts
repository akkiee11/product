// Single API client. All calls flow through here so we can swap base URL via env.

const BASE = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export type DebtType =
  | "CREDIT_CARD"
  | "PERSONAL_LOAN"
  | "BNPL"
  | "HOME_LOAN"
  | "AUTO_LOAN"
  | "EDUCATION_LOAN"
  | "GOLD_LOAN"
  | "INFORMAL"
  | "OTHER";

export interface DebtInput {
  type: DebtType;
  lender: string;
  outstanding: number;
  interestRate: number;        // decimal: 0.42 for 42%
  emi: number;
  monthsLeft: number;
}

export interface MriRequest {
  monthlyIncome: number;
  monthlyExpenses: number;
  age?: number;
  cibilScore?: number;
  hasDefault?: boolean;
  canIncreaseIncome?: boolean;
  email?: string;
  phone?: string;
  debts: DebtInput[];
}

export interface PathResult {
  pathId: string;
  name: string;
  totalCashOut: number;
  totalInterestPaid: number;
  feesPaid: number;
  taxExposure: number;
  monthsToFreedom: number;
  cibilAfter: number;
  rank: number;
  recommended: boolean;
  summary: string;
  warning?: string | null;
}

export interface Report {
  id?: string;                    // present once persisted in prod; absent in dev
  healthScore: number;
  healthLabel: string;
  primaryConcern: string;
  totalDebt: number;
  monthlyOutflow: number;
  debtToIncomePercent: number;
  recommendedPathId: string;
  paths: PathResult[];
}

export async function generateMri(req: MriRequest): Promise<Report> {
  const res = await fetch(`${BASE}/api/mri`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(req),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`API error ${res.status}: ${text}`);
  }
  return res.json();
}

export async function fetchReport(id: string): Promise<Report> {
  const res = await fetch(`${BASE}/api/mri/${id}`);
  if (res.status === 404) throw new Error("This report doesn't exist or has expired.");
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`API error ${res.status}: ${text}`);
  }
  return res.json();
}
