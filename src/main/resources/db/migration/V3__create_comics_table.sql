-- 코믹 테이블
CREATE TABLE comics (
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

-- 인덱스 생성
CREATE INDEX idx_comics_repo_name ON comics(repo_name);
CREATE INDEX idx_comics_language ON comics(language);
CREATE INDEX idx_comics_created_at ON comics(created_at DESC);
CREATE INDEX idx_comics_stars ON comics(stars DESC);
CREATE INDEX idx_comics_likes ON comics(likes DESC);
CREATE INDEX idx_comics_is_new ON comics(is_new) WHERE is_new = true;

-- 코멘트 추가
COMMENT ON TABLE comics IS '코믹 컨텐츠 메타데이터';
COMMENT ON COLUMN comics.repo_name IS 'GitHub 저장소 이름';
COMMENT ON COLUMN comics.repo_url IS 'GitHub 저장소 URL';
COMMENT ON COLUMN comics.panels IS '코믹 패널 데이터 (JSONB)';
COMMENT ON COLUMN comics.key_insights IS '주요 인사이트 (JSONB)';
