import { ImageResponse } from "next/og";

export const alt = "Rinmukt — Honest Debt MRI for Indians";
export const size = { width: 1200, height: 630 };
export const contentType = "image/png";

export default async function OGImage() {
  return new ImageResponse(
    (
      <div
        style={{
          width: "100%",
          height: "100%",
          background: "#0b1726",
          color: "#ffffff",
          display: "flex",
          flexDirection: "column",
          padding: "72px 80px",
          fontFamily: "sans-serif",
        }}
      >
        <div
          style={{
            display: "flex",
            alignItems: "center",
            gap: 16,
            color: "#ff6b35",
            fontSize: 32,
            fontWeight: 600,
            letterSpacing: 1,
          }}
        >
          <span>Rinmukt</span>
          <span style={{ color: "#3a4a5e" }}>·</span>
          <span>ऋणमुक्त</span>
        </div>

        <div
          style={{
            display: "flex",
            marginTop: 56,
            fontSize: 88,
            fontWeight: 800,
            lineHeight: 1.05,
            letterSpacing: -2,
          }}
        >
          Get out of debt without destroying your future.
        </div>

        <div
          style={{
            display: "flex",
            marginTop: "auto",
            gap: 24,
            flexWrap: "wrap",
            color: "#a3b4c9",
            fontSize: 28,
          }}
        >
          <Pill>Free Debt MRI</Pill>
          <Pill>5 paths compared honestly</Pill>
          <Pill accent>Real CIBIL + tax impact</Pill>
        </div>
      </div>
    ),
    size,
  );
}

function Pill({ children, accent }: { children: React.ReactNode; accent?: boolean }) {
  return (
    <div
      style={{
        display: "flex",
        padding: "12px 24px",
        borderRadius: 999,
        border: `2px solid ${accent ? "#ff6b35" : "#3a4a5e"}`,
        color: accent ? "#ff6b35" : "#a3b4c9",
      }}
    >
      {children}
    </div>
  );
}
