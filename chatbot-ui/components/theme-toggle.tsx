"use client";

import * as React from "react";
import { useTheme } from "next-themes";

export function ThemeToggle() {
  const { theme, systemTheme, setTheme } = useTheme();
  const [mounted, setMounted] = React.useState(false);

  React.useEffect(() => setMounted(true), []);

  // Avoid SSR mismatch by deferring UI until mounted
  if (!mounted) {
    return (
      <button
        aria-hidden="true"
        style={{
          width: 98,
          height: 38,
          borderRadius: 8,
          opacity: 0,
          pointerEvents: "none",
          border: "1px solid transparent",
        }}
      />
    );
  }

  const current = theme === "system" ? systemTheme : theme;
  const isDark = current === "dark";

  return (
    <button
      type="button"
      onClick={() => setTheme(isDark ? "light" : "dark")}
      aria-pressed={isDark}
      aria-label={isDark ? "Switch to light theme" : "Switch to dark theme"}
      style={{
        padding: "8px 12px",
        borderRadius: 10,
        border: "1px solid var(--border, #d0d5e2)",
        background: "transparent",
        color: "inherit",
        cursor: "pointer",
      }}
    >
      {isDark ? "🌞 Light" : "🌙 Dark"}
    </button>
  );
}
