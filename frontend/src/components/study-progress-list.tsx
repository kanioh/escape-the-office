import { STATUS_LABELS, STATUS_STYLES } from "@/lib/study-status";
import type { StudyProgress } from "@/lib/types";

// 表示するだけでクリックも状態も持たないため、サーバーコンポーネントのままにする。
// 編集できる一覧は study-progress-editor.tsx（クライアント）側
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
