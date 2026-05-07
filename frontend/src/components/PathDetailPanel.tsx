"use client";

import { useState } from "react";
import type { PathResult } from "@/lib/api";
import type { PathDetail } from "@/lib/pathDetails";
import { lakh, months } from "@/lib/format";

type Props = {
  path: PathResult;
  detail: PathDetail;
  defaultOpen: boolean;
  done: Record<string, boolean>;
  toggleStep: (stepKey: string) => void;
};

export default function PathDetailPanel({ path, detail, defaultOpen, done, toggleStep }: Props) {
  const [open, setOpen] = useState(defaultOpen);

  const stepKeys = detail.steps.map((s) => s.key);
  const completedCount = stepKeys.filter((k) => done[k]).length;
  const allDone = completedCount === stepKeys.length && stepKeys.length > 0;

  const accent = path.recommended ? "border-money" : "border-gray-200";
  const headerBg = path.recommended ? "bg-green-50" : "bg-white";

  return (
    <div className={`overflow-hidden rounded-xl border-2 ${accent}`}>
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        className={`flex w-full items-center justify-between gap-4 px-5 py-4 text-left ${headerBg}`}
      >
        <div className="flex flex-1 items-center gap-3">
          <span
            className={`flex h-7 w-7 shrink-0 items-center justify-center rounded-full text-xs font-bold ${
              path.recommended ? "bg-money text-white" : "bg-gray-100 text-gray-700"
            }`}
          >
            #{path.rank}
          </span>
          <div className="flex-1">
            <p className="font-semibold">{path.name}</p>
            <p className="mt-0.5 text-sm text-gray-600">{detail.oneLiner}</p>
          </div>
        </div>
        <div className="hidden items-center gap-3 text-xs text-gray-500 sm:flex">
          <span>{lakh(path.totalCashOut)}</span>
          <span>·</span>
          <span>{months(path.monthsToFreedom)}</span>
          <span>·</span>
          <span>CIBIL {path.cibilAfter}</span>
        </div>
        <div className="ml-2 flex items-center gap-3">
          {stepKeys.length > 0 && (
            <span
              className={`whitespace-nowrap rounded-full px-2.5 py-1 text-xs font-medium ${
                allDone
                  ? "bg-money text-white"
                  : completedCount > 0
                    ? "bg-yellow-100 text-yellow-800"
                    : "bg-gray-100 text-gray-600"
              }`}
            >
              {completedCount}/{stepKeys.length}
            </span>
          )}
          <span className="text-xl text-gray-400">{open ? "−" : "+"}</span>
        </div>
      </button>

      {open && (
        <div className="space-y-6 border-t border-gray-100 bg-white p-5">
          <p className="text-sm leading-relaxed text-gray-700">{detail.pitch}</p>

          {detail.warning && (
            <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-800">
              <p className="font-semibold">⚠ Read this before choosing this path</p>
              <p className="mt-1 leading-relaxed">{detail.warning}</p>
            </div>
          )}

          <section>
            <h3 className="mb-3 text-sm font-semibold uppercase tracking-wide text-gray-500">
              Your action plan
            </h3>
            <ol className="space-y-3">
              {detail.steps.map((step, idx) => (
                <li key={step.key} className="flex gap-3">
                  <input
                    type="checkbox"
                    id={`step-${path.pathId}-${step.key}`}
                    checked={!!done[step.key]}
                    onChange={() => toggleStep(step.key)}
                    className="mt-1 h-4 w-4 shrink-0 rounded border-gray-300 text-accent focus:ring-accent"
                  />
                  <label htmlFor={`step-${path.pathId}-${step.key}`} className="flex-1 cursor-pointer">
                    <div className="flex items-baseline gap-2">
                      <span className="text-xs font-semibold text-gray-400">
                        {idx + 1}.
                      </span>
                      {step.when && (
                        <span className="rounded bg-gray-100 px-1.5 py-0.5 text-[10px] font-medium uppercase tracking-wide text-gray-600">
                          {step.when}
                        </span>
                      )}
                      <span
                        className={`text-sm font-medium ${done[step.key] ? "text-gray-400 line-through" : "text-gray-900"}`}
                      >
                        {step.title}
                      </span>
                    </div>
                    {step.body && (
                      <p className="ml-5 mt-1 text-xs leading-relaxed text-gray-600">{step.body}</p>
                    )}
                  </label>
                </li>
              ))}
            </ol>
          </section>

          {detail.scripts && detail.scripts.length > 0 && (
            <section>
              <h3 className="mb-3 text-sm font-semibold uppercase tracking-wide text-gray-500">
                Phone scripts
              </h3>
              <div className="space-y-3">
                {detail.scripts.map((s, idx) => (
                  <ScriptCard key={idx} script={s} />
                ))}
              </div>
            </section>
          )}

          {detail.docs && detail.docs.length > 0 && (
            <section>
              <h3 className="mb-3 text-sm font-semibold uppercase tracking-wide text-gray-500">
                Documents you&apos;ll need
              </h3>
              <ul className="space-y-1.5 text-sm text-gray-700">
                {detail.docs.map((d) => (
                  <li key={d} className="flex gap-2">
                    <span className="text-gray-400">•</span>
                    <span>{d}</span>
                  </li>
                ))}
              </ul>
            </section>
          )}

          {detail.pushback && detail.pushback.length > 0 && (
            <section>
              <h3 className="mb-3 text-sm font-semibold uppercase tracking-wide text-gray-500">
                If they push back
              </h3>
              <div className="space-y-3">
                {detail.pushback.map((p, idx) => (
                  <div key={idx} className="rounded-lg bg-gray-50 p-4 text-sm">
                    <p className="text-gray-600">
                      <span className="font-semibold text-gray-800">If they say:</span>{" "}
                      &ldquo;{p.ifTheySay}&rdquo;
                    </p>
                    <p className="mt-2 text-gray-700">
                      <span className="font-semibold text-gray-800">You say:</span> {p.youSay}
                    </p>
                  </div>
                ))}
              </div>
            </section>
          )}
        </div>
      )}
    </div>
  );
}

function ScriptCard({ script }: { script: { title: string; intro: string; script: string } }) {
  const [copied, setCopied] = useState(false);

  const onCopy = async () => {
    await navigator.clipboard.writeText(script.script);
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  };

  return (
    <div className="overflow-hidden rounded-lg border border-gray-200">
      <div className="flex items-center justify-between border-b border-gray-200 bg-gray-50 px-4 py-2.5">
        <p className="text-sm font-semibold">{script.title}</p>
        <button
          type="button"
          onClick={onCopy}
          className="rounded border border-gray-300 bg-white px-2.5 py-1 text-xs font-medium text-gray-700 hover:bg-gray-100"
        >
          {copied ? "Copied!" : "Copy"}
        </button>
      </div>
      <div className="space-y-3 p-4 text-sm">
        <p className="text-gray-600">{script.intro}</p>
        <pre className="whitespace-pre-wrap rounded bg-gray-50 p-3 font-mono text-xs leading-relaxed text-gray-800">
          {script.script}
        </pre>
      </div>
    </div>
  );
}
