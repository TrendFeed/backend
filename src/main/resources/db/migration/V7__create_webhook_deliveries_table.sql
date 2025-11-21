-- Webhook Deliveries 테이블
-- 웹훅 전송 이력 및 재시도 로그 저장

CREATE TABLE webhook_deliveries (
    id BIGSERIAL PRIMARY KEY,
    
    -- 웹훅 참조
    webhook_id BIGINT NOT NULL REFERENCES webhooks(id) ON DELETE CASCADE,
    
    -- 이벤트 정보
    event_type VARCHAR(100) NOT NULL, -- 예: "comic.new", "comic.trending"
    event_id VARCHAR(255), -- 이벤트 관련 ID (comic_id 등)
    
    -- 요청 정보
    request_url VARCHAR(2048) NOT NULL,
    request_method VARCHAR(10) NOT NULL DEFAULT 'POST',
    request_headers JSONB,
    request_body JSONB NOT NULL,
    
    -- 응답 정보
    response_status INTEGER,
    response_headers JSONB,
    response_body TEXT,
    response_time_ms INTEGER, -- 응답 시간 (밀리초)
    
    -- 전송 상태
    status VARCHAR(20) NOT NULL, -- 'pending', 'sent', 'failed', 'retrying'
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 3,
    
    -- 에러 정보
    error_message TEXT,
    error_stack_trace TEXT,
    
    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    completed_at TIMESTAMP,
    next_retry_at TIMESTAMP,
    
    -- 제약 조건
    CONSTRAINT valid_status CHECK (status IN ('pending', 'sent', 'failed', 'retrying', 'success')),
    CONSTRAINT valid_retry_count CHECK (retry_count >= 0),
    CONSTRAINT valid_response_status CHECK (response_status IS NULL OR (response_status >= 100 AND response_status < 600))
);

-- 인덱스
CREATE INDEX idx_webhook_deliveries_webhook_id ON webhook_deliveries(webhook_id);
CREATE INDEX idx_webhook_deliveries_status ON webhook_deliveries(status);
CREATE INDEX idx_webhook_deliveries_event_type ON webhook_deliveries(event_type);
CREATE INDEX idx_webhook_deliveries_created_at ON webhook_deliveries(created_at DESC);
CREATE INDEX idx_webhook_deliveries_next_retry ON webhook_deliveries(next_retry_at) 
    WHERE status = 'retrying';

-- 전송 대기 중인 웹훅을 빠르게 조회하기 위한 복합 인덱스
CREATE INDEX idx_webhook_deliveries_pending ON webhook_deliveries(status, next_retry_at)
    WHERE status IN ('pending', 'retrying');
