"use client";

import { useState } from "react";

import { api } from "@/lib/api";
import { STATUS_LABELS, STATUS_STYLES } from "@/lib/study-status";
import type { StudyProgress } from "@/lib/types";

export function StudyProgressEditor({
  initialProgresses,
}: {
  initialProgresses: StudyProgress[];
}) {
  // 画面に映す値。ドラッグ中はこちらだけが動く
  const [progresses, setProgresses] = useState(initialProgresses);
  // サーバーが受け付けた確定値。送信に失敗したときの戻し先
  const [saved, setSaved] = useState(initialProgresses);
  const [error, setError] = useState<string | null>(null);

  /** ドラッグ中の見た目だけを更新する（この時点では送信しない） */
  const changePercent = (studyItemId: number, progressPercent: number) => {
    const next = progresses.map((item) => {
      if (item.studyItemId !== studyItemId) {
        return item;
      }
      // 元の項目をコピーし、進捗率だけ差し替えた「別のオブジェクト」を作る。
      // item を直接書き換えると参照が変わらず、React が再描画してくれない
      return { ...item, progressPercent: progressPercent };
    });

    setProgresses(next);
  };

  /** 操作を終えた時点で確定して送る */
  const save = async (studyItemId: number, progressPercent: number) => {
    const confirmed = saved.find((item) => item.studyItemId === studyItemId);

    // 値が変わっていないなら送らない（スライダーをクリックしただけの場合など）
    if (confirmed !== undefined && confirmed.progressPercent === progressPercent) {
      return;
    }

    setError(null);

    try {
      // ステータスはサーバーが進捗率から決めるため、更新後の1件を受け取って差し替える
      const updated = await api.put<StudyProgress>(
        `/api/study-progress/${studyItemId}`,
        { progressPercent: progressPercent },
      );

      const nextProgresses = progresses.map((item) =>
        item.studyItemId === studyItemId ? updated : item,
      );
      const nextSaved = saved.map((item) =>
        item.studyItemId === studyItemId ? updated : item,
      );

      setProgresses(nextProgresses);
      setSaved(nextSaved);
    } catch {
      setError("保存できませんでした。通信を確認してください");
      // 画面とサーバーが食い違ったままにならないよう、確定値まで巻き戻す
      setProgresses(saved);
    }
  };

  return (
    <>
      {error && (
        <p role="alert" className="mb-3 text-sm text-accent">
          {error}
        </p>
      )}

      <ul className="divide-y divide-border border-y border-border">
        {progresses.map((progress) => (
          <li key={progress.studyItemId} className="flex items-center gap-4 py-4">
            <span className="w-32 shrink-0 truncate text-sm font-medium">
              {progress.studyItemName}
            </span>

            <input
              type="range"
              min={0}
              max={100}
              // 1%刻みは狙った値に止めづらいため 5%刻みにする
              step={5}
              value={progress.progressPercent}
              // input の値は文字列で届くため数値に変換する
              onChange={(event) =>
                changePercent(progress.studyItemId, Number(event.target.value))
              }
              // マウス・タッチは離したとき、キーボードはキーを離したときに確定する
              onPointerUp={(event) =>
                save(progress.studyItemId, Number(event.currentTarget.value))
              }
              onKeyUp={(event) =>
                save(progress.studyItemId, Number(event.currentTarget.value))
              }
              aria-label={`${progress.studyItemName} の進捗率`}
              className="h-1.5 flex-1 accent-accent"
            />

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
    </>
  );
}
