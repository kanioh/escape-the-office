CREATE TABLE asset_snapshots (
    id          BIGSERIAL   PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recorded_on DATE        NOT NULL,
    cash_amount BIGINT      NOT NULL CHECK (cash_amount >= 0),
    nisa_amount BIGINT      NOT NULL DEFAULT 0 CHECK (nisa_amount >= 0),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- 同一ユーザーが同じ日に二重記録するのを防ぐ。
    -- このユニーク制約が (user_id, recorded_on) のインデックスも兼ねるため、別途 INDEX は作らない
    CONSTRAINT uq_asset_snapshots_user_date UNIQUE (user_id, recorded_on)
);

COMMENT ON TABLE  asset_snapshots             IS '資産スナップショット';
COMMENT ON COLUMN asset_snapshots.id          IS '資産スナップショットID';
COMMENT ON COLUMN asset_snapshots.user_id     IS 'ユーザーID';
COMMENT ON COLUMN asset_snapshots.recorded_on IS '記録日';
COMMENT ON COLUMN asset_snapshots.cash_amount IS '現金残高（円）';
COMMENT ON COLUMN asset_snapshots.nisa_amount IS 'NISA評価額（円）';
COMMENT ON COLUMN asset_snapshots.created_at  IS '登録日時';


CREATE TABLE expense_snapshots (
    id              BIGSERIAL   PRIMARY KEY,
    user_id         BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recorded_on     DATE        NOT NULL,
    monthly_expense BIGINT      NOT NULL CHECK (monthly_expense > 0),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_expense_snapshots_user_date UNIQUE (user_id, recorded_on)
);

COMMENT ON TABLE  expense_snapshots                 IS '生活費スナップショット';
COMMENT ON COLUMN expense_snapshots.id              IS '生活費スナップショットID';
COMMENT ON COLUMN expense_snapshots.user_id         IS 'ユーザーID';
COMMENT ON COLUMN expense_snapshots.recorded_on     IS '記録日';
COMMENT ON COLUMN expense_snapshots.monthly_expense IS '月間生活費（円）';
COMMENT ON COLUMN expense_snapshots.created_at      IS '登録日時';
