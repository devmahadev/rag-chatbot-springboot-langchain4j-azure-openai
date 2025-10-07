// app/actions.ts
"use server";

import { marked } from "marked";

export type LoadState = {
  html: string;
  error?: string;
};

export async function sendToLoad(
  _prevState: LoadState,
  formData: FormData
): Promise<LoadState> {
  try {
    const message = (formData.get("message") ?? "").toString();
    const file = formData.get("file") as File | null;

    const outgoing = new FormData();
    outgoing.set("message", message);
    if (file && file.size > 0) {
      // Do NOT set Content-Type manually; fetch will handle multipart boundary.
      outgoing.set("file", file, file.name);
    }

    // NEXT_PUBLIC_SPRING_LOAD_URL=http://localhost:8080/api/context-chatbot
    const upstreamUrl =
      process.env.NEXT_PUBLIC_SPRING_LOAD_URL ?? "http://localhost:8080/api/context-chatbot";

    const res = await fetch(upstreamUrl, {
      method: "POST",
      body: outgoing,
    });

    if (!res.ok) {
      const text = await res.text().catch(() => "");
      throw new Error(`Upstream failed: ${res.status} ${res.statusText} ${text}`);
    }

    const markdown = await res.text();
    const html = marked(markdown);
    return { html };
  } catch (err: any) {
    return { html: "", error: err?.message ?? "Unknown error" };
  }
}
