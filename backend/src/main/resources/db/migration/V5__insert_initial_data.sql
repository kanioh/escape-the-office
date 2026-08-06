-- MVP は認証を持たず、このユーザー（id = 1）を固定で使う。
-- id を明示指定するとシーケンスのカウンタが進まず、後続の自動採番が重複エラーになるため指定しない
INSERT INTO users (name, email) VALUES ('kani', 'kani@example.com');

INSERT INTO study_items (name) VALUES
    ('Spring Boot'),
    ('AWS'),
    ('Docker'),
    ('AtCoder'),
    ('TryHackMe');
