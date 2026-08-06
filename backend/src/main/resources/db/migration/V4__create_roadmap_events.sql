CREATE TABLE roadmap_events (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title      VARCHAR(200) NOT NULL,
    start_date DATE         NOT NULL,
    -- 終了未定を NULL で表現する
    end_date   DATE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    -- CHECK は結果が NULL のとき通るため end_date IS NULL の条件は冗長だが、
    -- 終了日 NULL を許容する設計であることを明示するために残す
    CONSTRAINT ck_roadmap_events_date_order
        CHECK (end_date IS NULL OR end_date >= start_date)
);

-- 「あるユーザーの予定を開始日順に並べる」が主要クエリ。
-- 絞り込み列を先、並べ替え列を後にすることで絞り込みと並べ替えを同時に処理できる
CREATE INDEX idx_roadmap_events_user_start ON roadmap_events (user_id, start_date);

COMMENT ON TABLE  roadmap_events            IS 'キャリアロードマップ予定';
COMMENT ON COLUMN roadmap_events.id         IS '予定ID';
COMMENT ON COLUMN roadmap_events.user_id    IS 'ユーザーID';
COMMENT ON COLUMN roadmap_events.title      IS '予定タイトル';
COMMENT ON COLUMN roadmap_events.start_date IS '開始日';
COMMENT ON COLUMN roadmap_events.end_date   IS '終了日（未定の場合は NULL）';
COMMENT ON COLUMN roadmap_events.created_at IS '登録日時';
