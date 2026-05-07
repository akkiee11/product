import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Rinmukt — Honest Debt MRI for Indians",
  description:
    "Get your free Debt MRI report in 5 minutes. Compare 5 paths out of debt — without nuking your CIBIL like Freed.",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className="min-h-screen antialiased">{children}</body>
    </html>
  );
}
