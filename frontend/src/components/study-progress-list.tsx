import type { StudyProgress, StudyStatus } from "@/lib/types";

// Record にしておくと、StudyStatus に値を足したときの書き忘れが型エラーになる
const STATUS_LABELS: Record<StudyStatus, string> = {
  NOT_STARTED: "未着手",
  IN_PROGRESS: "進行中",
  DONE: "完了",
};

// 「今やっているもの」だけアクセント色で拾い、完了と未着手は静かに沈める
const STATUS_STYLES: Record<StudyStatus, string> = {
  NOT_STARTED: "border border-border text-muted",
  IN_PROGRESS: "bg-accent/10 text-accent",
  DONE: "bg-foreground/5 text-muted",
};

// 表示するだけでクリックも状態も持たないため、サーバーコンポーネントのままにする
export function StudyProgressList({ progresses }: { progresses: StudyProgress[] }) {
  return (
    <ul className="divide-y divide-border border-y border-border">
      {progresses.map((progress) => (
        <li key={progress.studyItemId} className="flex items-center gap-4 py-4">
          <span className="w-32 shrink-0 truncate text-sm font-medium">
            {progress.studyItemName}
          </span>

          <div className="h-1.5 flex-1 overflow-hidden rounded-full bg-track">
            {/* 幅はデータ次第で変わるため、Tailwind のクラスでは表現できない */}
            <div
              className="h-full rounded-full bg-accent"
              style={{ width: `${progress.progressPercent}%` }}
            />
          </div>

          <span className="w-10 shrink-0 text-right text-sm tabular-nums text-muted">
            {progress.progressPercent}%
          </span>

          <span
            className={`w-16 shrink-0 rounded-full px-2 py-0.5 text-center text-xs ${STATUS_STYLES[progress.status]}`}
          >
            {STATUS_LABELS[progress.status]}
          </span>
        </li>
      ))}
    </ul>
  );
}
