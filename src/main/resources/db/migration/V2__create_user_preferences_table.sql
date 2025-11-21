-- 사용자 설정 테이블
CREATE TABLE user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_uid VARCHAR(255) NOT NULL UNIQUE,
    interests JSONB,
    notifications JSONB,
    comic_style VARCHAR(50),
    FOREIGN KEY (user_uid) REFERENCES users(uid) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX idx_user_preferences_user_uid ON user_preferences(user_uid);
CREATE INDEX idx_user_preferences_comic_style ON user_preferences(comic_style);

-- 코멘트 추가
COMMENT ON TABLE user_preferences IS '사용자 개인화 설정';
COMMENT ON COLUMN user_preferences.interests IS '관심 주제 배열 (JSONB)';
COMMENT ON COLUMN user_preferences.notifications IS '알림 설정 (JSONB)';
COMMENT ON COLUMN user_preferences.comic_style IS '선호 코믹 스타일';
