import { AssetForm } from "@/components/asset-form";
import { ExpenseForm } from "@/components/expense-form";
import { ApiError, api } from "@/lib/api";
import { formatDate, formatNumber, toIsoDate } from "@/lib/format";
import type {
  AssetSnapshot,
  ExpenseSnapshot,
  SurvivalSimulation,
} from "@/lib/types";

export default async function AssetsPage() {
  // 互いに依存しない3本なので、順番に待たず同時に投げる
  const [simulation, assets, expenses] = await Promise.all([
    // 資産か生活費が未登録だと 404 になる。まだ計算できないだけなので
    // ページ全体を落とさず null として扱う。通信障害などは握りつぶさない
    api.get<SurvivalSimulation>("/api/simulation/survival").catch((error) => {
      if (error instanceof ApiError && error.status === 404) {
        return null;
      }
      throw error;
    }),
    api.get<AssetSnapshot[]>("/api/assets"),
    api.get<ExpenseSnapshot[]>("/api/expenses"),
  ]);

  // 一覧は日付の新しい順に返るため、先頭が直近の記録
  const latestAsset = assets.length === 0 ? null : assets[0];
  const latestExpense = expenses.length === 0 ? null : expenses[0];

  // 「今日」はサーバー側で決め、フォームへ渡す
  const today = toIsoDate(new Date());

  return (
    <>
      <h1 className="text-xl font-semibold">資産</h1>

      <div className="mt-8 grid grid-cols-3 gap-4">
        <div className="rounded-xl border border-border bg-surface p-5">
          <p className="text-sm text-muted">総資産</p>
          <p className="mt-2 text-2xl font-semibold tabular-nums">
            {simulation === null ? "—" : formatNumber(simulation.totalAssets)}
            <span className="ml-1 text-sm font-normal text-muted">円</span>
          </p>
          <p className="mt-2 text-xs text-muted">
            {simulation === null
              ? "未登録"
              : `${formatDate(simulation.basedOn.assetsRecordedOn)} 時点`}
          </p>
        </div>

        <div className="rounded-xl border border-border bg-surface p-5">
          <p className="text-sm text-muted">月の生活費</p>
          <p className="mt-2 text-2xl font-semibold tabular-nums">
            {simulation === null ? "—" : formatNumber(simulation.monthlyExpense)}
            <span className="ml-1 text-sm font-normal text-muted">円</span>
          </p>
          <p className="mt-2 text-xs text-muted">
            {simulation === null
              ? "未登録"
              : `${formatDate(simulation.basedOn.expenseRecordedOn)} 時点`}
          </p>
        </div>

        <div className="rounded-xl border border-border bg-surface p-5">
          <p className="text-sm text-muted">生存可能期間</p>
          <p className="mt-2 text-2xl font-semibold tabular-nums">
            {simulation === null ? "—" : formatNumber(simulation.survivableMonths)}
            <span className="ml-1 text-sm font-normal text-muted">か月</span>
          </p>
          <p className="mt-2 text-xs text-muted">総資産 ÷ 月の生活費</p>
        </div>
      </div>

      <section className="mt-10">
        <h2 className="text-sm font-medium text-muted">資産を記録</h2>

        <div className="mt-3">
          <AssetForm today={today} latest={latestAsset} />
        </div>

        <div className="mt-6 overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border text-xs text-muted">
                <th className="py-2 text-left font-normal">日付</th>
                <th className="py-2 text-right font-normal">現金</th>
                <th className="py-2 text-right font-normal">NISA</th>
                <th className="py-2 text-right font-normal">合計</th>
              </tr>
            </thead>
            <tbody>
              {assets.map((asset) => (
                <tr key={asset.id} className="border-b border-border">
                  <td className="py-2 tabular-nums">{formatDate(asset.recordedOn)}</td>
                  <td className="py-2 text-right tabular-nums">
                    {formatNumber(asset.cashAmount)}
                  </td>
                  <td className="py-2 text-right tabular-nums">
                    {formatNumber(asset.nisaAmount)}
                  </td>
                  {/* 合計は API に無いため画面側で足す */}
                  <td className="py-2 text-right font-medium tabular-nums">
                    {formatNumber(asset.cashAmount + asset.nisaAmount)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <section className="mt-10">
        <h2 className="text-sm font-medium text-muted">生活費を記録</h2>

        <div className="mt-3">
          <ExpenseForm today={today} latest={latestExpense} />
        </div>

        <div className="mt-6 overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border text-xs text-muted">
                <th className="py-2 text-left font-normal">見直した日</th>
                <th className="py-2 text-right font-normal">月の生活費</th>
              </tr>
            </thead>
            <tbody>
              {expenses.map((expense) => (
                <tr key={expense.id} className="border-b border-border">
                  <td className="py-2 tabular-nums">{formatDate(expense.recordedOn)}</td>
                  <td className="py-2 text-right tabular-nums">
                    {formatNumber(expense.monthlyExpense)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}
