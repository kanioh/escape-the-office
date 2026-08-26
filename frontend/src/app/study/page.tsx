import { StudyItemForm } from "@/components/study-item-form";
import { StudyProgressList } from "@/components/study-progress-list";
import { api } from "@/lib/api";
import type { StudyProgress } from "@/lib/types";

// サーバーコンポーネントなので、Next.js のサーバー側で API を叩いてから HTML を返す
export default async function StudyPage() {
  const progresses = await api.get<StudyProgress[]>("/api/study-progress");

  return (
    <>
      <h1 className="text-xl font-semibold">学習管理</h1>
      <p className="mt-1 text-sm text-muted">{progresses.length} 項目</p>

      <div className="mt-8">
        <StudyProgressList progresses={progresses} />
        <StudyItemForm />
      </div>
    </>
  );
}
