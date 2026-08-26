import Link from "next/link";

import { StudyProgressList } from "@/components/study-progress-list";
import { api } from "@/lib/api";
import type { Dashboard } from "@/lib/types";

// サーバーとブラウザで既定ロケールが違うと表示が食い違うため、明示的に固定する
const formatNumber = (value: number) => value.toLocaleString("ja-JP");

// Date に変換すると UTC 解釈で日付がずれるので、文字列のまま整形する
const formatDate = (value: string) => value.replaceAll("-", "/");

export default async function DashboardPage() {
  const dashboard = await api.get<Dashboard>("/api/dashboard");

  return (
    <>
      <h1 className="text-xl font-semibold">ダッシュボード</h1>

      <div className="mt-8 grid grid-cols-2 gap-4">
        <div className="rounded-xl border border-border bg-surface p-5">
          <p className="text-sm text-muted">総資産</p>
          <p className="mt-2 text-3xl font-semibold tabular-nums">
            {formatNumber(dashboard.totalAssets)}
            <span className="ml-1 text-base font-normal text-muted">円</span>
          </p>
        </div>

        <div className="rounded-xl border border-border bg-surface p-5">
          <p className="text-sm text-muted">生存可能期間</p>
          <p className="mt-2 text-3xl font-semibold tabular-nums">
            {formatNumber(dashboard.survivableMonths)}
            <span className="ml-1 text-base font-normal text-muted">か月</span>
          </p>
        </div>
      </div>

      <section className="mt-10">
        <h2 className="text-sm font-medium text-muted">次の予定</h2>

        {dashboard.nextEvent ? (
          <div className="mt-3 flex items-center justify-between rounded-xl border border-border bg-surface p-5">
            <span className="font-medium">{dashboard.nextEvent.title}</span>
            <span className="text-sm tabular-nums text-muted">
              {formatDate(dashboard.nextEvent.startDate)}
              {" 〜 "}
              {dashboard.nextEvent.endDate
                ? formatDate(dashboard.nextEvent.endDate)
                : "未定"}
            </span>
          </div>
        ) : (
          <p className="mt-3 text-sm text-muted">これから始まる予定はありません</p>
        )}
      </section>

      <section className="mt-10">
        <div className="flex items-baseline justify-between">
          <h2 className="text-sm font-medium text-muted">最近の学習</h2>
          <Link
            href="/study"
            className="text-sm text-muted transition-colors hover:text-foreground"
          >
            すべて見る →
          </Link>
        </div>

        <div className="mt-3">
          <StudyProgressList progresses={dashboard.recentStudyProgress} />
        </div>
      </section>
    </>
  );
}
