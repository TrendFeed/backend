-- 저장된 코믹 테이블 (Many-to-Many)
CREATE TABLE saved_comics (
    id BIGSERIAL PRIMARY KEY,
    user_uid VARCHAR(255) NOT NULL,
    comic_id BIGINT NOT NULL,
    saved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_uid) REFERENCES users(uid) ON DELETE CASCADE,
    FOREIGN KEY (comic_id) REFERENCES comics(id) ON DELETE CASCADE,
    UNIQUE (user_uid, comic_id)
);

-- 인덱스 생성
CREATE INDEX idx_saved_comics_user_uid ON saved_comics(user_uid);
CREATE INDEX idx_saved_comics_comic_id ON saved_comics(comic_id);
CREATE INDEX idx_saved_comics_saved_at ON saved_comics(saved_at DESC);
CREATE INDEX idx_saved_comics_user_saved_at ON saved_comics(user_uid, saved_at DESC);

-- 코멘트 추가
COMMENT ON TABLE saved_comics IS '사용자가 저장한 코믹 목록';
COMMENT ON COLUMN saved_comics.user_uid IS '사용자 UID';
COMMENT ON COLUMN saved_comics.comic_id IS '코믹 ID';
COMMENT ON COLUMN saved_comics.saved_at IS '저장 시각';
