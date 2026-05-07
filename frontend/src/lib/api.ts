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

export interface AdminStats {
  total: number;
  today: number;
  last7Days: number;
  withEmail: number;
  welcomeSent: number;
  day7Sent: number;
  byPath: Record<string, number>;
  byHealthLabel: Record<string, number>;
}

export interface AdminSession {
  id: string;
  email: string | null;
  createdAt: string | null;
  healthScore: number | null;
  healthLabel?: string | null;
  totalDebt?: number;
  recommendedPath: string | null;
  welcomeSent: boolean;
  day7Sent: boolean;
}

async function adminFetch<T>(path: string, token: string): Promise<T> {
  const res = await fetch(`${BASE}/api/admin/${path}`, {
    headers: { "X-Admin-Token": token },
  });
  if (res.status === 401) throw new Error("Invalid admin token");
  if (res.status === 503) throw new Error("Admin endpoints not enabled on the server (ADMIN_TOKEN unset).");
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Admin API ${res.status}: ${text}`);
  }
  return res.json();
}

export const fetchAdminStats = (token: string) => adminFetch<AdminStats>("stats", token);
export const fetchAdminSessions = (token: string) =>
  adminFetch<{ sessions: AdminSession[] }>("sessions", token).then((r) => r.sessions);
