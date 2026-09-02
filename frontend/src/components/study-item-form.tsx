"use client";

import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";

import { Button } from "@/components/button";
import { ApiError, api } from "@/lib/api";
import type { StudyItem } from "@/lib/types";

// エラーの中身を画面向けの文言に変換する。想定外のものは一括で同じ文言にする
function toErrorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    if (error.status === 409) {
      return "その項目はすでに登録されています";
    }
    if (error.status === 400) {
      return "項目名を入力してください";
    }
  }
  return "登録に失敗しました。時間をおいて試してください";
}

export function StudyItemForm() {
  const router = useRouter();
  const [isOpen, setIsOpen] = useState(false);
  const [name, setName] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const close = () => {
    setIsOpen(false);
    setName("");
    setError(null);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    // フォーム送信の既定動作（ページ全体の再読み込み）を止める
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      await api.post<StudyItem>("/api/study-items", { name });
      setName("");
      // 一覧はサーバーコンポーネントが持っているため、サーバー側だけ再実行して差し替える。
      // ページ全体の再読み込みと違い、入力欄やスクロール位置は保たれる
      router.refresh();
    } catch (caught) {
      setError(toErrorMessage(caught));
    } finally {
      // 成功・失敗どちらでも送信中を解除する
      setIsSubmitting(false);
    }
  };

  if (!isOpen) {
    return (
      <button
        type="button"
        onClick={() => setIsOpen(true)}
        className="mt-4 text-sm text-muted transition-colors hover:text-foreground"
      >
        ＋ 項目を追加
      </button>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="mt-4">
      <div className="flex items-center gap-2">
        <input
          // 値は React の状態から与え、変更のたびに状態へ書き戻す（制御コンポーネント）
          value={name}
          onChange={(event) => setName(event.target.value)}
          placeholder="項目名を入力"
          autoFocus
          // DB の VARCHAR(100) に合わせ、超過分は入力段階で受け付けない
          maxLength={100}
          className="flex-1 rounded-md border border-border bg-surface px-3 py-2 text-sm outline-none focus:border-accent"
        />

        <Button
          type="submit"
          // 空のまま送ると 400 が確定するため、押せないようにする
          disabled={isSubmitting || name.trim() === ""}
        >
          {isSubmitting ? "追加中…" : "追加"}
        </Button>

        <button
          type="button"
          onClick={close}
          className="px-2 py-2 text-sm text-muted transition-colors hover:text-foreground"
        >
          キャンセル
        </button>
      </div>

      {/* role="alert" を付けると、追加されたことが支援技術にも読み上げられる */}
      {error && (
        <p role="alert" className="mt-2 text-sm text-accent">
          {error}
        </p>
      )}
    </form>
  );
}
