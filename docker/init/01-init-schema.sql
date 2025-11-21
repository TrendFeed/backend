-- TrendFeed 데이터베이스 초기화 스크립트 (멱등성 보장)
-- PostgreSQL Docker 컨테이너 시작 시 자동 실행됨

-- 1. 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    uid VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(255),
    photo_url TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 (IF NOT EXISTS는 PostgreSQL 9.5+에서 지원)
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

-- 코멘트
COMMENT ON TABLE users IS '사용자 기본 정보';
COMMENT ON COLUMN users.uid IS 'Firebase UID';

-- 2. 사용자 설정 테이블
CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_uid VARCHAR(255) NOT NULL UNIQUE,
    interests JSONB,
    notifications JSONB,
    comic_style VARCHAR(50),
    CONSTRAINT fk_user_preferences_user FOREIGN KEY (user_uid) REFERENCES users(uid) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_preferences_user_uid ON user_preferences(user_uid);
CREATE INDEX IF NOT EXISTS idx_user_preferences_comic_style ON user_preferences(comic_style);

COMMENT ON TABLE user_preferences IS '사용자 개인화 설정';

-- 3. 코믹 테이블
CREATE TABLE IF NOT EXISTS comics (
    id BIGSERIAL PRIMARY KEY,
    repo_name VARCHAR(500) NOT NULL,
    repo_url TEXT NOT NULL,
    stars INTEGER DEFAULT 0,
    language VARCHAR(100),
    panels JSONB NOT NULL,
    key_insights JSONB,
    is_new BOOLEAN DEFAULT true,
    likes INTEGER DEFAULT 0,
    shares INTEGER DEFAULT 0,
    comments INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_comics_repo_name ON comics(repo_name);
CREATE INDEX IF NOT EXISTS idx_comics_language ON comics(language);
CREATE INDEX IF NOT EXISTS idx_comics_created_at ON comics(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_comics_stars ON comics(stars DESC);
CREATE INDEX IF NOT EXISTS idx_comics_likes ON comics(likes DESC);
CREATE INDEX IF NOT EXISTS idx_comics_is_new ON comics(is_new) WHERE is_new = true;

COMMENT ON TABLE comics IS '코믹 컨텐츠 메타데이터';

-- 4. 저장된 코믹 테이블
CREATE TABLE IF NOT EXISTS saved_comics (
    id BIGSERIAL PRIMARY KEY,
    user_uid VARCHAR(255) NOT NULL,
    comic_id BIGINT NOT NULL,
    saved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_saved_comics_user FOREIGN KEY (user_uid) REFERENCES users(uid) ON DELETE CASCADE,
    CONSTRAINT fk_saved_comics_comic FOREIGN KEY (comic_id) REFERENCES comics(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_comic UNIQUE (user_uid, comic_id)
);

CREATE INDEX IF NOT EXISTS idx_saved_comics_user_uid ON saved_comics(user_uid);
CREATE INDEX IF NOT EXISTS idx_saved_comics_comic_id ON saved_comics(comic_id);
CREATE INDEX IF NOT EXISTS idx_saved_comics_saved_at ON saved_comics(saved_at DESC);
CREATE INDEX IF NOT EXISTS idx_saved_comics_user_saved_at ON saved_comics(user_uid, saved_at DESC);

COMMENT ON TABLE saved_comics IS '사용자가 저장한 코믹 목록';

-- 5. 뉴스레터 구독 테이블
-- ENUM 타입 생성 (IF NOT EXISTS 직접 지원 안됨, 따라서 DO 블록 사용)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'subscription_status') THEN
        CREATE TYPE subscription_status AS ENUM ('PENDING', 'ACTIVE', 'UNSUBSCRIBED');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS newsletter_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    status subscription_status NOT NULL DEFAULT 'PENDING',
    confirmation_token VARCHAR(255) UNIQUE,
    unsubscribe_token VARCHAR(255) UNIQUE,
    subscribed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP,
    unsubscribed_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_newsletter_email ON newsletter_subscriptions(email);
CREATE INDEX IF NOT EXISTS idx_newsletter_status ON newsletter_subscriptions(status);
CREATE INDEX IF NOT EXISTS idx_newsletter_confirmation_token ON newsletter_subscriptions(confirmation_token);
CREATE INDEX IF NOT EXISTS idx_newsletter_unsubscribe_token ON newsletter_subscriptions(unsubscribe_token);
CREATE INDEX IF NOT EXISTS idx_newsletter_active_subscribers ON newsletter_subscriptions(status) WHERE status = 'ACTIVE';

COMMENT ON TABLE newsletter_subscriptions IS '뉴스레터 구독 정보';

-- 초기화 완료 로그
DO $$ 
BEGIN
    RAISE NOTICE 'TrendFeed database schema initialized successfully!';
END $$;
