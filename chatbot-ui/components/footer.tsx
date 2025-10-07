// components/footer.tsx
import React from "react";

function GitHubIcon(props: React.SVGProps<SVGSVGElement>) {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      width="18"
      height="18"
      className="icon"
      {...props}
    >
      <path
        fill="currentColor"
        d="M12 .5a12 12 0 0 0-3.79 23.39c.6.11.82-.26.82-.58v-2.01c-3.34.73-4.04-1.61-4.04-1.61-.55-1.4-1.35-1.77-1.35-1.77-1.1-.76.08-.75.08-.75 1.22.09 1.86 1.26 1.86 1.26 1.08 1.85 2.82 1.31 3.5 1 .11-.79.42-1.31.76-1.61-2.66-.3-5.46-1.33-5.46-5.9 0-1.3.47-2.36 1.25-3.19-.13-.31-.54-1.57.12-3.26 0 0 1.02-.33 3.34 1.22.97-.27 2.02-.4 3.06-.4s2.09.13 3.06.4c2.32-1.55 3.34-1.22 3.34-1.22.66 1.69.25 2.95.12 3.26.78.83 1.25 1.89 1.25 3.19 0 4.58-2.8 5.59-5.47 5.88.43.37.81 1.1.81 2.22v3.29c0 .32.21.7.83.58A12 12 0 0 0 12 .5Z"
      />
    </svg>
  );
}

export default function Footer() {
  const year = new Date().getFullYear();

  return (
    <footer className="site-footer">
      <div className="shell footer-inner">
        <p className="footer-copy">
          © {year} <span className="brand-text-alt">Dev‑D</span>. All rights reserved.
        </p>

        <nav className="footer-links" aria-label="Footer">
          <a href="https://github.com/devmahadev/rag-chatbot-springboot-langchain4j-azure-openai" target="_blank" rel="noopener noreferrer">
                     <GitHubIcon /> <span>GitHub</span>
          </a>
        </nav>


      </div>
    </footer>
  );
}