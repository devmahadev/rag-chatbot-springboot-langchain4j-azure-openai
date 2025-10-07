// app/layout.tsx
import "./globals.css";
import { ThemeProvider } from "@/components/theme-provider";
import Header from "@/components/header";
import Footer from "@/components/footer";

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body>
<ThemeProvider attribute="class" defaultTheme="system" enableSystem disableTransitionOnChange>
         <Header />

          <main id="content" className="site-main">
            {children}
          </main>

              <Footer />
        </ThemeProvider>
      </body>
    </html>
  );
}