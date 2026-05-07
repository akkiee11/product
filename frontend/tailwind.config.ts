import type { Config } from "tailwindcss";

export default {
  content: ["./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        ink: "#0b1726",
        accent: "#ff6b35",
        money: "#0f9d58",
      },
    },
  },
  plugins: [],
} satisfies Config;
