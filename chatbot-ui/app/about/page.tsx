"use client";
import React, { useState, useEffect } from "react";
import {
  Github,
  Linkedin,
  Twitter,
  Sun,
  Moon,
} from "lucide-react";

import dynamic from "next/dynamic";

export default function AboutPage() {

  return (
    <main className="about-main">
      <div className="about-container">
        {/* Header */}
        <div className="about-header">
          <h1 className="">About Me</h1>
        </div>

        {/* Profile Card */}
        <section className="bg-card text-card-foreground p-6 rounded-xl shadow-lg mb-10 flex flex-col items-center animate-fadeIn">

          <h2 className="text-2xl font-semibold mb-1">Deepak Hirenwal</h2>
          <p className="text-muted-foreground mb-4 text-center">
            Senior Consultant | AI Engineering Enthusiast | LangChain4j Explorer
          </p>
          <div className="flex gap-4">
            <a href="https://github.com/devmahadev/rag-chatbot-springboot-langchain4j-azure-openai" target="_blank" rel="noopener noreferrer">
              <Github className="hover:text-primary transition" />
            </a>
            <a href="https://www.linkedin.com/in/deepak-hirenwal-phd-645b8081/" target="_blank" rel="noopener noreferrer">
              <Linkedin className="hover:text-primary transition" />
            </a>
            <a href="https://twitter.com" target="_blank" rel="noopener noreferrer">
              <Twitter className="hover:text-primary transition" />
            </a>
          </div>
        </section>

        {/* Bio */}
        <section className="mb-10 bg-card p-6 rounded-lg shadow-md animate-fadeIn">
          <p className="text-lg leading-relaxed text-card-foreground">
            Hello! I'm a passionate <strong>Solution Architect and Software Architect</strong> with a developer's mindset,
            deeply engaged in exploring the capabilities of <strong>LangChain4j</strong>, agentic AI systems, and enterprise-grade AI integrations.
            I thrive on designing scalable architectures, orchestrating intelligent workflows, and building intuitive user interfaces that bridge human interaction with machine intelligence.
            My work blends strategic system thinking with hands-on experimentation—whether it's integrating LLMs into microservices,
            optimizing developer experience, or solving real-world problems through clean, maintainable code.
          </p>
        </section>

        {/* Skills */}
        <section className="bg-popover p-6 rounded-lg shadow-md animate-fadeIn">
          <h2 className="text-2xl font-semibold mb-4 text-popover-foreground">Skills & Interests</h2>
          <ul className="list-disc list-inside space-y-2 text-lg text-popover-foreground">
            <li>Solution Architect | Cloud-Native, DevOps Architecture & Agentic AI Expert</li>
            <li>Java | Python | JavaScript | React & Next.js</li>
            <li>Langchain4j & AI integrations</li>
            <li>Industry Expertise: Banking | IOT | Government & Research | Business IT/Services</li>
            <li>Open-source contributions</li>
          </ul>
        </section>

        {/* PDF Viewer */}
        <section className="rag-info-container animate-fadeIn">
          <h3 className="text-xl font-semibold mb-4">Projects & Experience</h3>
                  <iframe
                  src="/docs/deepak.pdf"
                  width="100%">
                   </iframe>
                         <p style={{ textAlign: "center", marginTop: "1rem" }}>
                           <a href="/docs/deepak.pdf"> Download Resume</a>
                         </p>

        </section>
      </div>
    </main>
  );
}
