import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import { cookies } from "next/headers";
import "./globals.css";
import { Sidebar } from "@/components/sidebar";
import { SIDEBAR_COOKIE } from "@/lib/sidebar";

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
export default async function RootLayout({ children }: LayoutProps<"/">) {
  // 開閉状態をサーバー側で決める。クライアントで読むと最初の描画に間に合わず、
  // 閉じているはずのサイドバーが一瞬開いて見える
  const cookieStore = await cookies();
  const isCollapsed = cookieStore.get(SIDEBAR_COOKIE)?.value === "true";

  return (
    <html
      lang="ja"
      className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}
    >
      <body className="flex min-h-full">
        <Sidebar defaultCollapsed={isCollapsed} />
        <main className="flex-1 px-10 py-10">
          <div className="mx-auto max-w-3xl">{children}</div>
        </main>
      </body>
    </html>
  );
}
