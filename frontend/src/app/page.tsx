import Link from "next/link";

import { StudyProgressList } from "@/components/study-progress-list";
import { api } from "@/lib/api";
import { formatDate, formatNumber } from "@/lib/format";
import type { Dashboard } from "@/lib/types";

export default async function DashboardPage() {
  const dashboard = await api.get<Dashboard>("/api/dashboard");

  return (
    <>
      <h1 className="text-xl font-semibold">ダッシュボード</h1>

      <div className="mt-8 grid grid-cols-2 gap-4">
        {/* a 要素は既定が inline のため、grid の中で高さを揃えるには block が要る */}
        <Link href="/assets" className="block h-full">
          <div className="h-full rounded-xl border border-border bg-surface p-5">
            <p className="text-sm text-muted">総資産</p>
            {/* 0 円と未登録を区別するため、falsy 判定ではなく null と比べる */}
            {dashboard.totalAssets === null ? (
              <>
                <p className="mt-2 text-3xl font-semibold text-muted">未登録</p>
                <p className="mt-2 text-sm text-accent">資産を登録する →</p>
              </>
            ) : (
              <p className="mt-2 text-3xl font-semibold tabular-nums">
                {formatNumber(dashboard.totalAssets)}
                <span className="ml-1 text-base font-normal text-muted">円</span>
              </p>
            )}
          </div>
        </Link>

        <div className="rounded-xl border border-border bg-surface p-5">
          <p className="text-sm text-muted">生存可能期間</p>
          {dashboard.survivableMonths === null ? (
            <>
              <p className="mt-2 text-3xl font-semibold text-muted">—</p>
              <p className="mt-2 text-sm text-muted">資産と生活費の記録が必要です</p>
            </>
          ) : (
            <p className="mt-2 text-3xl font-semibold tabular-nums">
              {formatNumber(dashboard.survivableMonths)}
              <span className="ml-1 text-base font-normal text-muted">か月</span>
            </p>
          )}
        </div>
      </div>

      <section className="mt-10">
        <h2 className="text-sm font-medium text-muted">次の予定</h2>

        {/* 先頭の / が無いと相対パス扱いになり、別の画面から使ったときにずれる */}
        <Link href="/roadmap">
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
        </Link>
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
