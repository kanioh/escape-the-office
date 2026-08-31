"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";

import { api } from "@/lib/api";

export function DeleteRoadmapEventButton({
  id,
  title,
}: {
  id: number;
  title: string;
}) {
  const router = useRouter();
  const [isDeleting, setIsDeleting] = useState(false);
  const [hasFailed, setHasFailed] = useState(false);

  const handleClick = async () => {
    // 取り消せない操作なので、ブラウザ標準の確認ダイアログを挟む
    if (!window.confirm(`「${title}」を削除しますか？`)) {
      return;
    }

    setHasFailed(false);
    setIsDeleting(true);

    try {
      // 204 で本文が返らないため、削除後は一覧を取り直して反映する
      await api.delete(`/api/roadmap-events/${id}`);
      router.refresh();
    } catch {
      setHasFailed(true);
    } finally {
      setIsDeleting(false);
    }
  };

  return (
    <button
      type="button"
      onClick={handleClick}
      disabled={isDeleting}
      // 一覧に同じ文言のボタンが並ぶため、どの予定のものか読み上げで区別できるようにする
      aria-label={`${title} を削除`}
      className="shrink-0 text-xs text-muted transition-colors hover:text-accent disabled:opacity-40"
    >
      {hasFailed ? "削除失敗" : "削除"}
    </button>
  );
}
