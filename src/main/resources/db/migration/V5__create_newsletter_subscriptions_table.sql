-- 뉴스레터 구독 상태 타입
CREATE TYPE subscription_status AS ENUM ('PENDING', 'ACTIVE', 'UNSUBSCRIBED');

-- 뉴스레터 구독 테이블
CREATE TABLE newsletter_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    status subscription_status NOT NULL DEFAULT 'PENDING',
    confirmation_token VARCHAR(255) UNIQUE,
    unsubscribe_token VARCHAR(255) UNIQUE,
    subscribed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP,
    unsubscribed_at TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX idx_newsletter_email ON newsletter_subscriptions(email);
CREATE INDEX idx_newsletter_status ON newsletter_subscriptions(status);
CREATE INDEX idx_newsletter_confirmation_token ON newsletter_subscriptions(confirmation_token);
CREATE INDEX idx_newsletter_unsubscribe_token ON newsletter_subscriptions(unsubscribe_token);
CREATE INDEX idx_newsletter_active_subscribers ON newsletter_subscriptions(status) WHERE status = 'ACTIVE';

-- 코멘트 추가
COMMENT ON TABLE newsletter_subscriptions IS '뉴스레터 구독 정보';
COMMENT ON COLUMN newsletter_subscriptions.status IS '구독 상태: PENDING, ACTIVE, UNSUBSCRIBED';
COMMENT ON COLUMN newsletter_subscriptions.confirmation_token IS '이메일 확인용 토큰';
COMMENT ON COLUMN newsletter_subscriptions.unsubscribe_token IS '구독 취소용 토큰';
