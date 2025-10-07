// app/FormClient.tsx
"use client";

import * as React from "react";
import { useActionState } from "react";
import { useFormStatus } from "react-dom";

type LoadState = { html: string; error?: string };
const initialState: LoadState = { html: "" };

function SubmitButton() {
  const { pending } = useFormStatus();
  return (
    <button
      className="btn btn-primary"
      type="submit"
      disabled={pending}
      aria-disabled={pending}
    >
      {pending ? (
        <>
          <svg className="spinner" viewBox="0 0 24 24" aria-hidden="true">
            <circle className="track" cx="12" cy="12" r="10" />
            <circle className="indicator" cx="12" cy="12" r="10" />
          </svg>
          Sending…
        </>
      ) : (
        <>Send</>
      )}
    </button>
  );
}

export default function FormClient({
  action,
}: {
  action: (state: LoadState, formData: FormData) => Promise<LoadState>;
}) {
  const [state, formAction] = useActionState(action, initialState);

  return (
    <div className="shell">


<div className="rag-info-container">
  <p>
    <strong>Retrieval-Augmented Generation (RAG)</strong> is revolutionizing how we build intelligent applications by combining the reasoning power of Large Language Models (LLMs) with custom, domain-specific knowledge bases. This project demonstrates how to build a document-aware chatbot using:
  </p>

  <ul>
    <li>Spring Boot for scalable backend architecture</li>
    <li>LangChain4j for agentic AI orchestration</li>
    <li>Azure OpenAI for embedding and chat completion</li>
    <li>Vector stores for semantic search and retrieval</li>
  </ul>

  <p>
    The chatbot can ingest documents (PDF, DOCX), extract meaningful content, and answer user queries using context-aware responses powered by RAG.
  </p>

  <h3>Prerequisites:</h3>
  <ul>
    <li>Java 25</li>
    <li>Maven</li>
    <li>Docker Compose</li>
    <li>Azure OpenAI access (embedding + chat models)</li>
  </ul>
</div>


      <header className="header">
        <h1 className="title">Upload a document and ask a question</h1>
        <p className="subtitle">
          Attach a file (PDF, DOCX, etc.) and type your prompt below.
        </p>
      </header>

      <section className="card">
        <form action={formAction} className="form" noValidate>
          <div className="field">
            <label className="label" htmlFor="message">
              Message
            </label>
            <textarea
              className="input textarea"
              id="message"
              name="message"
              rows={4}
              placeholder="What is the content of the document?"
              required
            />
            <p className="help">Be specific to get the best answer.</p>
          </div>

          <div className="field">
            <label className="label" htmlFor="file">
              File (optional)
            </label>
            <input className="input file" id="file" name="file" type="file" />
            <p className="help">Max ~25 MB. We’ll only use it to answer your question.</p>
          </div>

          <div className="actions">
            <SubmitButton />
          </div>
        </form>
      </section>

      {state.error ? (
        <div className="alert alert-error" role="alert" aria-live="polite">
          <svg className="alert-icon" viewBox="0 0 24 24" aria-hidden="true">
            <path
              d="M12 9v4m0 4h.01M10.29 3.86l-8.1 14A1.5 1.5 0 0 0 3.5 20h17a1.5 1.5 0 0 0 1.31-2.14l-8.1-14a1.5 1.5 0 0 0-2.62 0z"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
          <span className="alert-text">Error: {state.error}</span>
        </div>
      ) : null}

      {state.html ? (
        <section className="card">
          <h2 className="section-title">Response</h2>
          <div
            className="prose response"
            dangerouslySetInnerHTML={{ __html: state.html }}
          />
        </section>
      ) : null}
    </div>
  );
}