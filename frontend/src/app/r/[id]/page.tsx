"use client";

import { use, useEffect, useState } from "react";
import Link from "next/link";
import { fetchReport, type Report } from "@/lib/api";
import ReportView from "@/components/ReportView";

export default function SavedReportPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const [report, setReport] = useState<Report | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    fetchReport(id)
      .then((r) => {
        if (!cancelled) setReport(r);
      })
      .catch((err: unknown) => {
        if (!cancelled) setError(err instanceof Error ? err.message : "Could not load report");
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

  if (error) {
    return (
      <main className="mx-auto max-w-3xl p-10 text-center">
        <p className="text-red-600">{error}</p>
        <p className="mt-3">
          <Link className="underline" href="/start">
            Start a new MRI →
          </Link>
        </p>
      </main>
    );
  }

  if (!report) {
    return (
      <main className="mx-auto max-w-3xl p-10 text-center text-gray-500">Loading your report…</main>
    );
  }

  return <ReportView report={report} />;
}
