import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import { Sidebar } from "@/components/sidebar";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "サラリーマン脱出計画",
  description: "退職・学習・転職・フリーランス準備を管理するツール",
};

// layout.tsx は画面を切り替えても再描画されないため、サイドバーの状態が保たれる
export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html
      lang="ja"
      className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}
    >
      <body className="flex min-h-full">
        <Sidebar />
        <main className="flex-1 px-10 py-10">
          <div className="mx-auto max-w-3xl">{children}</div>
        </main>
      </body>
    </html>
  );
}
