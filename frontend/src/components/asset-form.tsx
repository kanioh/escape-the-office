"use client";

import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";

import { api } from "@/lib/api";
import { toIsoDate } from "@/lib/format";
import type { AssetSnapshot } from "@/lib/types";

export function AssetForm({
  // 「今日」はサーバー側で決めた値を受け取る（描画時に new Date() を呼ぶと
  // サーバーとブラウザで結果が食い違い、React の警告が出る）
  today,
  latest,
}: {
  today: string;
  latest: AssetSnapshot | null;
}) {
  const router = useRouter();
  const [recordedOn, setRecordedOn] = useState(today);
  // input type="number" の値は文字列で届く。空欄を 0 と区別するため文字列のまま持つ
  const [cashAmount, setCashAmount] = useState(
    latest === null ? "" : String(latest.cashAmount),
  );
  const [nisaAmount, setNisaAmount] = useState(
    latest === null ? "" : String(latest.nisaAmount),
  );
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  /**
   * 画面を開いたまま日付をまたぐと today が前日のままになる。
   * 自分で日付を選んでいない場合に限り、送信時点のブラウザの日付を使う。
   * イベントハンドラの中なので new Date() を呼んでも描画結果には影響しない。
   */
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
      // 同じ日付に何度送っても上書きされる（UPSERT）ため、新規と修正を区別しない
      await api.put<AssetSnapshot>(`/api/assets/${resolveRecordedOn()}`, {
        cashAmount: Number(cashAmount),
        nisaAmount: Number(nisaAmount),
      });
      router.refresh();
    } catch {
      // 未来日は API 側の @PastOrPresent が 400 で弾く
      setError("記録できませんでした。未来の日付やマイナスの金額は登録できません");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="flex flex-wrap items-end gap-3">
        <label className="text-xs text-muted">
          日付
          <input
            type="date"
            value={recordedOn}
            onChange={(event) => setRecordedOn(event.target.value)}
            className="mt-1 block rounded-md border border-border bg-surface px-3 py-2 text-sm text-foreground outline-none focus:border-accent"
          />
        </label>

        <label className="text-xs text-muted">
          現金（円）
          <input
            type="number"
            value={cashAmount}
            onChange={(event) => setCashAmount(event.target.value)}
            min={0}
            placeholder="0"
            className="mt-1 block w-40 rounded-md border border-border bg-surface px-3 py-2 text-sm tabular-nums text-foreground outline-none focus:border-accent"
          />
        </label>

        <label className="text-xs text-muted">
          NISA（円）
          <input
            type="number"
            value={nisaAmount}
            onChange={(event) => setNisaAmount(event.target.value)}
            min={0}
            placeholder="0"
            className="mt-1 block w-40 rounded-md border border-border bg-surface px-3 py-2 text-sm tabular-nums text-foreground outline-none focus:border-accent"
          />
        </label>

        <button
          type="submit"
          disabled={isSubmitting || cashAmount === "" || nisaAmount === ""}
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
