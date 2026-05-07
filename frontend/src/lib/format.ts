// Indian-locale formatting helpers.

export function inr(n: number): string {
  return new Intl.NumberFormat("en-IN", { maximumFractionDigits: 0 }).format(Math.round(n));
}

export function lakh(n: number): string {
  if (n >= 10_000_000) return `₹${(n / 10_000_000).toFixed(2)} Cr`;
  if (n >= 100_000) return `₹${(n / 100_000).toFixed(2)} L`;
  return `₹${inr(n)}`;
}

export function months(n: number): string {
  if (n >= 12) {
    const years = Math.floor(n / 12);
    const m = n % 12;
    return m === 0 ? `${years} yr` : `${years}y ${m}m`;
  }
  return `${n} mo`;
}
