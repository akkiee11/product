import Link from "next/link";

export default function Landing() {
  return (
    <main className="mx-auto max-w-3xl px-6 py-16">
      <header className="mb-12">
        <p className="text-sm font-medium text-accent">Rinmukt · ऋणमुक्त</p>
        <h1 className="mt-2 text-4xl font-bold tracking-tight md:text-5xl">
          Get out of debt without destroying your future.
        </h1>
        <p className="mt-4 text-lg text-gray-600">
          A free, honest Debt MRI for Indian borrowers. Compare 5 paths — including the one
          settlement companies don&rsquo;t want you to see.
        </p>
      </header>

      <section className="rounded-xl border border-gray-200 p-6 shadow-sm">
        <h2 className="text-xl font-semibold">What you get in 5 minutes</h2>
        <ul className="mt-4 space-y-2 text-gray-700">
          <li>• Personalised Debt Health Score (0–100)</li>
          <li>• 5 paths ranked by total cost AND credit impact</li>
          <li>• 36-month action plan with exact monthly EMIs</li>
          <li>• True cost of Freed-style settlement (incl. tax + CIBIL crash)</li>
          <li>• Phone scripts for negotiating with your banks</li>
        </ul>
        <Link
          href="/start"
          className="mt-6 inline-block rounded-lg bg-ink px-6 py-3 font-medium text-white hover:bg-black"
        >
          Start my Debt MRI →
        </Link>
        <p className="mt-3 text-xs text-gray-500">No login. No credit card. Free forever.</p>
      </section>

      <section className="mt-12 grid gap-6 md:grid-cols-3">
        <Stat n="₹22.6L" label="Avg debt our pilot users carry" />
        <Stat n="₹8L+" label="Saved vs Freed-style settlement" />
        <Stat n="0" label="CIBIL points lost on Smart Path" />
      </section>

      <section className="mt-12 border-t pt-8 text-sm text-gray-500">
        <p>
          Rinmukt is an information &amp; advisory tool. We are not a registered debt counsellor
          or lender. Always consult a CA or lawyer before settling any debt.
        </p>
      </section>
    </main>
  );
}

function Stat({ n, label }: { n: string; label: string }) {
  return (
    <div className="rounded-lg bg-gray-50 p-4 text-center">
      <p className="text-2xl font-bold text-ink">{n}</p>
      <p className="mt-1 text-sm text-gray-600">{label}</p>
    </div>
  );
}
