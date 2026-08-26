// バックエンドの DTO に対応する型。手書きなので DTO を変更したらここも直す。
// 対応先: backend/src/main/java/com/example/escapetheoffice/

// Java の enum は JSON では文字列で届くため、TS 側は文字列ユニオンで受ける
export type StudyStatus = "NOT_STARTED" | "IN_PROGRESS" | "DONE";

// GET /api/study-progress の1件分。進捗が未登録の項目も NOT_STARTED / 0 として返る
export type StudyProgress = {
  studyItemId: number;
  studyItemName: string;
  status: StudyStatus;
  progressPercent: number;
};

// POST /api/study-items の戻り値
export type StudyItem = {
  id: number;
  name: string;
};

// LocalDate は "2026-09-01" の文字列で届く。Date に変換すると UTC 解釈で
// 日付がずれるため、表示するだけの間は文字列のまま扱う
export type RoadmapEvent = {
  id: number;
  title: string;
  startDate: string;
  // 終了未定は null
  endDate: string | null;
};

// GET /api/dashboard
export type Dashboard = {
  totalAssets: number;
  survivableMonths: number;
  // 最近更新した順に最大3件。全件は /study で見る
  recentStudyProgress: StudyProgress[];
  // これから始まる予定が無ければ null
  nextEvent: RoadmapEvent | null;
};
