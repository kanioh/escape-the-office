CREATE TABLE study_items (
    id   BIGSERIAL    PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

COMMENT ON TABLE  study_items      IS '学習項目マスタ';
COMMENT ON COLUMN study_items.id   IS '学習項目ID';
COMMENT ON COLUMN study_items.name IS '学習項目名';


CREATE TABLE study_progress (
    id               BIGSERIAL   PRIMARY KEY,
    user_id          BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    -- マスタの誤削除で進捗記録を失わないよう、CASCADE ではなく RESTRICT にする
    study_item_id    BIGINT      NOT NULL REFERENCES study_items(id) ON DELETE RESTRICT,
    -- Java 側は enum + @Enumerated(EnumType.STRING) で受ける。
    -- PostgreSQL の ENUM 型は値の追加に ALTER TYPE が必要で運用が硬いため VARCHAR + CHECK にする
    status           VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED'
                         CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'DONE')),
    progress_percent INT         NOT NULL DEFAULT 0
                         CHECK (progress_percent BETWEEN 0 AND 100),
    -- UPDATE 時の自動更新は PostgreSQL では行われない。JPA 側で更新する
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_study_progress_user_item UNIQUE (user_id, study_item_id)
);

-- 複合インデックスは左端から連続する列にしか使えないため、
-- study_item_id 単独の検索（および study_items 削除時の参照チェック）用に別途作成する
CREATE INDEX idx_study_progress_item ON study_progress (study_item_id);

COMMENT ON TABLE  study_progress                  IS '学習進捗';
COMMENT ON COLUMN study_progress.id               IS '学習進捗ID';
COMMENT ON COLUMN study_progress.user_id          IS 'ユーザーID';
COMMENT ON COLUMN study_progress.study_item_id    IS '学習項目ID';
COMMENT ON COLUMN study_progress.status           IS '進捗ステータス（NOT_STARTED/IN_PROGRESS/DONE）';
COMMENT ON COLUMN study_progress.progress_percent IS '進捗率（％）';
COMMENT ON COLUMN study_progress.updated_at       IS '更新日時';
