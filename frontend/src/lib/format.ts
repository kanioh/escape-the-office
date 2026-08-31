// サーバーとブラウザで既定ロケールが違うと表示が食い違うため、明示的に固定する
export const formatNumber = (value: number) => value.toLocaleString("ja-JP");

// "2026-08-27" → "2026/08/27"。
// Date に変換すると UTC 解釈で日付がずれるため、文字列のまま整形する
export const formatDate = (value: string) => value.replaceAll("-", "/");

// "2026-09-01" → "2026年9月"。月の見出し用
export const formatYearMonth = (value: string) => {
  const [year, month] = value.split("-");
  // "09" のままだと「2026年09月」になるため数値に戻す
  return `${year}年${Number(month)}月`;
};

// "2026-09-01" → "09/01"。見出しに年があるため月日だけ出す
export const formatMonthDay = (value: string) => value.slice(5).replace("-", "/");

/**
 * Date を API が受け取る "2026-08-27" 形式にする。
 * toISOString() は UTC 基準に変換するため、日本時間の夜に呼ぶと前日になる。
 * ローカル時間の年月日をそのまま組み立てる。
 */
export const toIsoDate = (date: Date) => {
  const year = date.getFullYear();
  // getMonth() は 0 始まりなので +1 する
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
};
