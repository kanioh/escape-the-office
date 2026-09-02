"use client";

import {
  BookOpen,
  LayoutDashboard,
  Map as MapIcon,
  PanelLeftClose,
  PanelLeftOpen,
  Wallet,
} from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState } from "react";

import { SIDEBAR_COOKIE } from "@/lib/sidebar";

// 画面を実装したらここに足していく（未実装の画面を並べると 404 になるため）
const NAV_ITEMS = [
  { href: "/", label: "ダッシュボード", icon: LayoutDashboard },
  { href: "/study", label: "学習管理", icon: BookOpen },
  { href: "/assets", label: "資産", icon: Wallet },
  { href: "/roadmap", label: "ロードマップ", icon: MapIcon },
];

// 初期値は layout.tsx が Cookie から読んで渡す。
// クライアント側で読むと最初の描画に間に合わず、一瞬開いた状態が見えてしまう
export function Sidebar({ defaultCollapsed }: { defaultCollapsed: boolean }) {
  // 現在地の判定にはブラウザ側の情報が要るので、この部品だけクライアントコンポーネントにする
  const pathname = usePathname();
  const [isCollapsed, setIsCollapsed] = useState(defaultCollapsed);

  const toggle = () => {
    const next = !isCollapsed;
    setIsCollapsed(next);
    // 1年保持。path=/ を付けないと現在のパス配下にしか送られない
    document.cookie = `${SIDEBAR_COOKIE}=${next}; path=/; max-age=31536000; samesite=lax`;
  };

  return (
    <aside
      className={`shrink-0 border-r border-border bg-surface py-6 transition-[width,padding] ${
        isCollapsed ? "w-16 px-2" : "w-56 px-4"
      }`}
    >
      <div
        className={`flex items-center ${isCollapsed ? "justify-center" : "justify-between pl-2"}`}
      >
        {!isCollapsed && <p className="text-sm font-semibold">サラリーマン脱出計画</p>}
        <button
          type="button"
          onClick={toggle}
          aria-label={isCollapsed ? "サイドバーを開く" : "サイドバーを閉じる"}
          className="rounded-md p-1.5 text-muted transition-colors hover:bg-foreground/5 hover:text-foreground"
        >
          {isCollapsed ? <PanelLeftOpen size={18} /> : <PanelLeftClose size={18} />}
        </button>
      </div>

      <nav className="mt-8 space-y-1">
        {NAV_ITEMS.map((item) => {
          const isCurrent = pathname === item.href;
          const Icon = item.icon;

          return (
            <Link
              key={item.href}
              href={item.href}
              // 色だけに頼らず、支援技術にも現在地を伝える
              aria-current={isCurrent ? "page" : undefined}
              // 折りたたみ時はラベルが見えないので、ホバーで名前を出す
              title={isCollapsed ? item.label : undefined}
              className={`flex items-center gap-3 rounded-md py-2 text-sm transition-colors ${
                isCollapsed ? "justify-center" : "px-3"
              } ${
                isCurrent
                  ? "bg-accent/10 font-medium text-accent"
                  : "text-muted hover:bg-foreground/5 hover:text-foreground"
              }`}
            >
              <Icon size={18} className="shrink-0" />
              {/* 折りたたんでも読み上げには残す */}
              <span className={isCollapsed ? "sr-only" : undefined}>{item.label}</span>
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}
