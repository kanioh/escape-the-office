import type { StudyStatus } from "@/lib/types";

// 表示専用の一覧と編集用の一覧の両方から使うため、定義はここに一本化する。
// Record にしておくと、StudyStatus に値を足したときの書き忘れが型エラーになる
export const STATUS_LABELS: Record<StudyStatus, string> = {
  NOT_STARTED: "未着手",
  IN_PROGRESS: "進行中",
  DONE: "完了",
};

// 「今やっているもの」だけアクセント色で拾い、完了と未着手は静かに沈める
export const STATUS_STYLES: Record<StudyStatus, string> = {
  NOT_STARTED: "border border-border text-muted",
  IN_PROGRESS: "bg-accent/10 text-accent",
  DONE: "bg-foreground/5 text-muted",
};
