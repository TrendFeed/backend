-- Webhooks 테이블
-- 사용자가 등록한 웹훅 엔드포인트 정보 저장

CREATE TABLE webhooks (
    id BIGSERIAL PRIMARY KEY,
    
    -- 웹훅 소유자
    user_uid VARCHAR(255) NOT NULL REFERENCES users(uid) ON DELETE CASCADE,
    
    -- 웹훅 기본 정보
    url VARCHAR(2048) NOT NULL,
    description VARCHAR(500),
    secret_key VARCHAR(255) NOT NULL, -- HMAC 서명용 시크릿
    
    -- 구독할 이벤트 타입 (JSON 배열)
    -- 예: ["comic.new", "comic.trending", "newsletter.sent"]
    event_types JSONB NOT NULL DEFAULT '[]'::jsonb,
    
    -- 웹훅 상태
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- 통계
    total_deliveries INTEGER NOT NULL DEFAULT 0,
    successful_deliveries INTEGER NOT NULL DEFAULT 0,
    failed_deliveries INTEGER NOT NULL DEFAULT 0,
    last_delivery_at TIMESTAMP,
    last_success_at TIMESTAMP,
    last_failure_at TIMESTAMP,
    
    -- 재시도 설정
    max_retries INTEGER NOT NULL DEFAULT 3,
    retry_delay_seconds INTEGER NOT NULL DEFAULT 60,
    
    -- 메타데이터
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- 제약 조건
    CONSTRAINT valid_url CHECK (url ~ '^https?://'),
    CONSTRAINT valid_max_retries CHECK (max_retries >= 0 AND max_retries <= 10)
);

-- 인덱스
CREATE INDEX idx_webhooks_user_uid ON webhooks(user_uid);
CREATE INDEX idx_webhooks_is_active ON webhooks(is_active);
CREATE INDEX idx_webhooks_event_types ON webhooks USING GIN(event_types);

-- updated_at 자동 업데이트 트리거
CREATE TRIGGER update_webhooks_updated_at
    BEFORE UPDATE ON webhooks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
