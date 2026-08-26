"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

// 画面を実装したらここに足していく（未実装の画面を並べると 404 になるため）
const NAV_ITEMS = [
  { href: "/", label: "ダッシュボード" },
  { href: "/study", label: "学習管理" },
];

export function Sidebar() {
  // 現在地の判定にはブラウザ側の情報が要るので、この部品だけクライアントコンポーネントにする
  const pathname = usePathname();

  return (
    <aside className="w-56 shrink-0 border-r border-border bg-surface px-4 py-6">
      <p className="px-3 text-sm font-semibold">脱出計画</p>

      <nav className="mt-8 space-y-1">
        {NAV_ITEMS.map((item) => {
          const isCurrent = pathname === item.href;

          return (
            <Link
              key={item.href}
              href={item.href}
              // 色だけに頼らず、支援技術にも現在地を伝える
              aria-current={isCurrent ? "page" : undefined}
              className={`block rounded-md px-3 py-2 text-sm transition-colors ${
                isCurrent
                  ? "bg-accent/10 font-medium text-accent"
                  : "text-muted hover:bg-foreground/5 hover:text-foreground"
              }`}
            >
              {item.label}
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}
