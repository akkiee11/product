import type { Metadata } from "next";
import "./globals.css";

const title = "Rinmukt — Honest Debt MRI for Indians";
const description =
  "Free 5-minute Debt MRI. Compare 5 paths out of debt — ranked by total cost AND CIBIL/tax impact. The honest alternative to Freed.";

export const metadata: Metadata = {
  title,
  description,
  metadataBase: new URL("https://rinmukt.vercel.app"),
  openGraph: {
    type: "website",
    locale: "en_IN",
    url: "/",
    siteName: "Rinmukt",
    title,
    description,
  },
  twitter: {
    card: "summary_large_image",
    title,
    description,
  },
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className="min-h-screen antialiased">{children}</body>
    </html>
  );
}
