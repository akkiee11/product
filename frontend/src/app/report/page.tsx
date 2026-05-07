"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import type { Report } from "@/lib/api";
import ReportView from "@/components/ReportView";

export default function ReportPage() {
  const [report, setReport] = useState<Report | null>(null);

  useEffect(() => {
    const raw = sessionStorage.getItem("rinmukt:report");
    if (raw) setReport(JSON.parse(raw));
  }, []);

  if (!report) {
    return (
      <main className="mx-auto max-w-3xl p-10 text-center">
        <p>
          No report found.{" "}
          <Link className="underline" href="/start">
            Start a new MRI →
          </Link>
        </p>
      </main>
    );
  }

  return <ReportView report={report} />;
}
