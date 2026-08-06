CREATE TABLE users (
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

COMMENT ON TABLE  users            IS 'ユーザー';
COMMENT ON COLUMN users.id         IS 'ユーザーID';
COMMENT ON COLUMN users.name       IS '氏名';
COMMENT ON COLUMN users.email      IS 'メールアドレス';
COMMENT ON COLUMN users.created_at IS '登録日時';
