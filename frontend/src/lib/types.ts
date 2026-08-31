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

// GET /api/assets の1件分。その日時点の実残高
export type AssetSnapshot = {
  id: number;
  recordedOn: string;
  cashAmount: number;
  nisaAmount: number;
};

// GET /api/expenses の1件分。recordedOn は「その値に見直した日」
export type ExpenseSnapshot = {
  id: number;
  recordedOn: string;
  monthlyExpense: number;
};

// GET /api/simulation/survival
export type SurvivalSimulation = {
  totalAssets: number;
  monthlyExpense: number;
  survivableMonths: number;
  // どの時点のデータで計算したか。古い数字を見て判断する事故を防ぐために返している
  basedOn: {
    assetsRecordedOn: string;
    expenseRecordedOn: string;
  };
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
