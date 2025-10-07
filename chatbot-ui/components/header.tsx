"use client";

import React from "react";
import Link from "next/link";
import { ThemeToggle } from "./theme-toggle";

export default function Header() {
  return (
    <header className="site-header border-b py-3 flex flex-col items-center">
      <h1 className="text-2xl font-bold text-gray-800 dark:text-gray-200 mb-4 text-center">
        RAG Chatbot
      </h1>

      <nav className="nav flex gap-6 mb-4" aria-label="Main">
                {/* Prefetched when the link is hovered or enters the viewport */}
                <Link href="/home">Home</Link>
                <Link href="/about">About</Link>
        </nav>
      <div className="header-actions flex justify-center">


        <ThemeToggle />
      </div>
    </header>
  );
}