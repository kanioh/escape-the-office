"use client";

import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";

import { api } from "@/lib/api";
import { toIsoDate } from "@/lib/format";
import type { ExpenseSnapshot } from "@/lib/types";

export function ExpenseForm({
  today,
  latest,
}: {
  today: string;
  latest: ExpenseSnapshot | null;
}) {
  const router = useRouter();
  const [recordedOn, setRecordedOn] = useState(today);
  const [monthlyExpense, setMonthlyExpense] = useState(
    latest === null ? "" : String(latest.monthlyExpense),
  );
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // 画面を開いたまま日付をまたいでも、送信時点の日付で記録されるようにする
  const resolveRecordedOn = () => {
    if (recordedOn !== today) {
      return recordedOn;
    }
    return toIsoDate(new Date());
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      await api.put<ExpenseSnapshot>(`/api/expenses/${resolveRecordedOn()}`, {
        monthlyExpense: Number(monthlyExpense),
      });
      router.refresh();
    } catch {
      setError("記録できませんでした。未来の日付やマイナスの金額は登録できません");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="flex flex-wrap items-end gap-3">
        <label className="text-xs text-muted">
          {/* 資産と違い「その日の実績」ではなく「その金額に見直した日」を指す */}
          見直した日
          <input
            type="date"
            value={recordedOn}
            onChange={(event) => setRecordedOn(event.target.value)}
            className="mt-1 block rounded-md border border-border bg-surface px-3 py-2 text-sm text-foreground outline-none focus:border-accent"
          />
        </label>

        <label className="text-xs text-muted">
          月の生活費（円）
          <input
            type="number"
            value={monthlyExpense}
            onChange={(event) => setMonthlyExpense(event.target.value)}
            min={0}
            placeholder="0"
            className="mt-1 block w-40 rounded-md border border-border bg-surface px-3 py-2 text-sm tabular-nums text-foreground outline-none focus:border-accent"
          />
        </label>

        <button
          type="submit"
          disabled={isSubmitting || monthlyExpense === ""}
          className="rounded-md bg-accent px-4 py-2 text-sm text-white transition-opacity disabled:opacity-40"
        >
          {isSubmitting ? "記録中…" : "記録する"}
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
