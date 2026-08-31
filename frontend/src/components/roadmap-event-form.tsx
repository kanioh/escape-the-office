"use client";

import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";

import { api } from "@/lib/api";
import type { RoadmapEvent } from "@/lib/types";

export function RoadmapEventForm({ today }: { today: string }) {
  const router = useRouter();
  const [title, setTitle] = useState("");
  const [startDate, setStartDate] = useState(today);
  const [endDate, setEndDate] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      await api.post<RoadmapEvent>("/api/roadmap-events", {
        title: title,
        startDate: startDate,
        // 空欄は「終了未定」。空文字は日付として解釈できず 400 になるため null で送る
        endDate: endDate === "" ? null : endDate,
      });

      setTitle("");
      setEndDate("");
      router.refresh();
    } catch {
      setError("登録できませんでした。終了日は開始日以降にしてください");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="flex flex-wrap items-end gap-3">
        <label className="flex-1 text-xs text-muted">
          予定
          <input
            value={title}
            onChange={(event) => setTitle(event.target.value)}
            placeholder="有給消化、転職活動など"
            // DB の VARCHAR(200) に合わせる
            maxLength={200}
            className="mt-1 block w-full rounded-md border border-border bg-surface px-3 py-2 text-sm text-foreground outline-none focus:border-accent"
          />
        </label>

        <label className="text-xs text-muted">
          開始日
          <input
            type="date"
            value={startDate}
            onChange={(event) => setStartDate(event.target.value)}
            className="mt-1 block rounded-md border border-border bg-surface px-3 py-2 text-sm text-foreground outline-none focus:border-accent"
          />
        </label>

        <label className="text-xs text-muted">
          終了日（未定なら空欄）
          <input
            type="date"
            value={endDate}
            onChange={(event) => setEndDate(event.target.value)}
            // 開始日より前は選べないようにする（サーバー側でも弾いている）
            min={startDate}
            className="mt-1 block rounded-md border border-border bg-surface px-3 py-2 text-sm text-foreground outline-none focus:border-accent"
          />
        </label>

        <button
          type="submit"
          disabled={isSubmitting || title.trim() === "" || startDate === ""}
          className="rounded-md bg-accent px-4 py-2 text-sm text-white transition-opacity disabled:opacity-40"
        >
          {isSubmitting ? "追加中…" : "追加する"}
        </button>
      </div>

      {error && (
        <p role="alert" className="mt-2 text-sm text-accent">
          {error}
        </p>
      )}
    </form>
  );
}
