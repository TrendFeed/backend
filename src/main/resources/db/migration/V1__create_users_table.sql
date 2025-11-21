-- 사용자 테이블
CREATE TABLE users (
    uid VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(255),
    photo_url TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_created_at ON users(created_at);

-- 코멘트 추가
COMMENT ON TABLE users IS '사용자 기본 정보';
COMMENT ON COLUMN users.uid IS 'Firebase UID';
COMMENT ON COLUMN users.email IS '사용자 이메일';
COMMENT ON COLUMN users.display_name IS '표시 이름';
COMMENT ON COLUMN users.photo_url IS '프로필 사진 URL';
