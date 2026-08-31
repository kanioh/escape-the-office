import { DeleteRoadmapEventButton } from "@/components/delete-roadmap-event-button";
import { RoadmapEventForm } from "@/components/roadmap-event-form";
import { api } from "@/lib/api";
import { formatMonthDay, formatYearMonth, toIsoDate } from "@/lib/format";
import type { RoadmapEvent } from "@/lib/types";

export default async function RoadmapPage() {
  const events = await api.get<RoadmapEvent[]>("/api/roadmap-events");
  const today = toIsoDate(new Date());

  // 終わった予定は隠す。ISO 形式（YYYY-MM-DD）は桁が固定なので文字列比較で時系列を判定できる。
  // 終了日が未定のものは「まだ続いている」とみなして残す
  const upcomingEvents = events.filter(
    (event) => event.endDate === null || event.endDate >= today,
  );
  const hiddenCount = events.length - upcomingEvents.length;

  // 開始月ごとにまとめる。API が開始日の昇順で返すため、
  // Map に入れた順（＝時系列）がそのまま見出しの並びになる
  const eventsByMonth = new Map<string, RoadmapEvent[]>();

  for (const event of upcomingEvents) {
    // "2026-09-01" の先頭7文字を月のキーにする
    const month = event.startDate.slice(0, 7);
    const sameMonth = eventsByMonth.get(month);

    if (sameMonth === undefined) {
      // その月は初めて。その予定1件だけを入れた新しい束を作る
      eventsByMonth.set(month, [event]);
    } else {
      // get が返すのは Map の中の配列そのものなので、push すれば Map の中身も変わる
      sameMonth.push(event);
    }
  }

  return (
    <>
      <h1 className="text-xl font-semibold">キャリアロードマップ</h1>
      <p className="mt-1 text-sm text-muted">
        {upcomingEvents.length} 件の予定
        {/* 黙って消すと「登録したのに出てこない」と誤解するため、隠した件数を出す */}
        {hiddenCount > 0 && `（終了した ${hiddenCount} 件は非表示）`}
      </p>

      <section className="mt-8">
        <h2 className="text-sm font-medium text-muted">予定を追加</h2>
        <div className="mt-3">
          <RoadmapEventForm today={today} />
        </div>
      </section>

      {upcomingEvents.length === 0 ? (
        <p className="mt-10 text-sm text-muted">これからの予定はありません</p>
      ) : (
        Array.from(eventsByMonth).map(([month, monthEvents]) => (
          <section key={month} className="mt-8">
            <h2 className="text-sm font-medium text-muted">{formatYearMonth(month)}</h2>

            <ul className="mt-3 divide-y divide-border border-y border-border">
              {monthEvents.map((event) => (
                <li key={event.id} className="flex items-center gap-4 py-3">
                  <span className="flex-1 truncate text-sm font-medium">
                    {event.title}
                  </span>

                  <span className="shrink-0 text-sm tabular-nums text-muted">
                    {formatMonthDay(event.startDate)}
                    {event.endDate === null
                      ? " 〜 未定"
                      : ` 〜 ${formatMonthDay(event.endDate)}`}
                  </span>

                  <DeleteRoadmapEventButton id={event.id} title={event.title} />
                </li>
              ))}
            </ul>
          </section>
        ))
      )}
    </>
  );
}
