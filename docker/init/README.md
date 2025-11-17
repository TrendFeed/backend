# PostgreSQL 초기화 스크립트

## 개요

이 디렉토리의 SQL 파일들은 PostgreSQL Docker 컨테이너가 **처음 시작될 때** 자동으로 실행됩니다.

## 멱등성 보장

모든 SQL 스크립트는 멱등성(Idempotency)을 보장합니다:

- ✅ `CREATE TABLE IF NOT EXISTS`
- ✅ `CREATE INDEX IF NOT EXISTS`
- ✅ `INSERT ... ON CONFLICT DO NOTHING`
- ✅ ENUM 타입 중복 생성 방지

따라서 **여러 번 실행해도 안전**합니다.

## 실행 순서

PostgreSQL은 `/docker-entrypoint-initdb.d/` 디렉토리의 파일을 **알파벳 순서**로 실행합니다:

1. `01-init-schema.sql` - 테이블 및 인덱스 생성
2. `02-sample-data.sql` - 샘플 데이터 삽입 (선택사항)

## 사용 방법

### 1. 처음 시작 (데이터베이스 초기화)

```bash
# PostgreSQL 컨테이너 시작
docker-compose up -d postgres

# 로그 확인 (초기화 과정 확인)
docker logs -f trendfeed-postgres
```

**출력 예시:**

```
...
NOTICE: TrendFeed database schema initialized successfully!
NOTICE: Sample comic data inserted successfully!
...
PostgreSQL init process complete; ready for start up.
```

### 2. 재시작 (스크립트 재실행 안됨)

```bash
# 단순 재시작 - 초기화 스크립트 실행 안됨
docker-compose restart postgres
```

**주의:** PostgreSQL은 초기화 스크립트를 **데이터베이스가 비어있을 때만** 실행합니다.

### 3. 완전 초기화 (스크립트 재실행)

데이터베이스를 완전히 삭제하고 다시 시작하려면:

```bash
# 1. 컨테이너와 볼륨 삭제
docker-compose down -v

# 2. 다시 시작 (초기화 스크립트 재실행)
docker-compose up -d postgres

# 3. 로그 확인
docker logs -f trendfeed-postgres
```

**⚠️ 경고:** 이 방법은 **모든 데이터를 삭제**합니다! 프로덕션에서는 절대 사용하지 마세요.

## Flyway와의 관계

### 현재 설정

이 프로젝트는 **두 가지 방식**을 모두 사용합니다:

1. **Docker Init Scripts** (`docker/init/*.sql`)

   - Docker 컨테이너 시작 시 자동 실행
   - 개발 환경에서 빠른 초기화
   - 샘플 데이터 포함

2. **Flyway Migrations** (`src/main/resources/db/migration/V*.sql`)
   - Spring Boot 애플리케이션 시작 시 실행
   - 버전 관리 및 마이그레이션 히스토리
   - 프로덕션 환경 권장

### 충돌 방지

두 방식이 충돌하지 않도록 하려면:

#### 옵션 1: Flyway 비활성화 (개발 시)

`application.yml`:

```yaml
spring:
  flyway:
    enabled: false # Docker init scripts만 사용
```

#### 옵션 2: Docker Init Scripts 비활성화 (프로덕션)

`docker-compose.yml`에서 해당 줄 제거 또는 주석:

```yaml
volumes:
  - pgdata:/var/lib/postgresql/data
  # - ./docker/init:/docker-entrypoint-initdb.d  # 주석 처리
```

#### 옵션 3: Flyway baseline (권장)

Docker init으로 초기화 후 Flyway가 이를 인식하도록:

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true # 이미 있는 스키마를 baseline으로 인식
```

## 샘플 데이터

### 샘플 데이터 제거

프로덕션에서는 샘플 데이터가 필요 없습니다:

```bash
# 샘플 데이터 파일 제거
rm docker/init/02-sample-data.sql

# 또는 파일 이름 변경하여 비활성화
mv docker/init/02-sample-data.sql docker/init/02-sample-data.sql.disabled
```

### 커스텀 샘플 데이터

`02-sample-data.sql`을 수정하여 원하는 데이터 추가:

```sql
INSERT INTO comics (repo_name, repo_url, stars, language, panels, key_insights, created_at, updated_at)
VALUES
  ('your-org/your-repo', 'https://github.com/your-org/your-repo', 1000, 'Python',
   '[]'::jsonb, '["Your insight"]'::jsonb, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
```

## 파일 구조

```
docker/init/
├── README.md                    # 이 파일
├── 01-init-schema.sql           # 테이블 및 인덱스 생성 (필수)
└── 02-sample-data.sql           # 샘플 데이터 (선택사항)
```

## 테이블 확인

PostgreSQL에 접속하여 테이블 확인:

```bash
# PostgreSQL 접속
docker exec -it trendfeed-postgres psql -U postgres -d backend_db

# 테이블 목록
\dt

# 테이블 구조 확인
\d users
\d comics

# 샘플 데이터 확인
SELECT * FROM comics;

# 종료
\q
```

## 문제 해결

### 1. 스크립트가 실행되지 않음

**증상:** 테이블이 생성되지 않음

**원인:** 데이터베이스가 이미 초기화되어 있음

**해결:**

```bash
docker-compose down -v
docker-compose up -d postgres
```

### 2. "relation already exists" 에러

**증상:** 테이블이 이미 존재한다는 에러

**원인:** 멱등성이 보장되어야 하는데 `IF NOT EXISTS`가 없음

**해결:** SQL 파일에서 `CREATE TABLE IF NOT EXISTS` 사용 확인

### 3. Flyway와 충돌

**증상:** Flyway가 "Schema not empty" 에러

**해결:** 위의 "Flyway와의 관계" 섹션 참조

### 4. 샘플 데이터 중복

**증상:** 샘플 데이터 삽입 시 unique constraint 에러

**원인:** `ON CONFLICT DO NOTHING`이 없음

**해결:** 이미 `02-sample-data.sql`에 포함되어 있음

## 로그 확인

초기화 과정 전체 로그:

```bash
docker logs trendfeed-postgres 2>&1 | grep -A 10 "PostgreSQL init process"
```

NOTICE 메시지만 확인:

```bash
docker logs trendfeed-postgres 2>&1 | grep "NOTICE"
```

## 추가 스크립트

새로운 초기화 스크립트 추가:

1. `docker/init/` 디렉토리에 SQL 파일 생성
2. 파일명은 숫자로 시작 (예: `03-create-functions.sql`)
3. 멱등성 보장
4. 컨테이너 재생성

```bash
# 새 스크립트 생성 예시
echo "CREATE OR REPLACE FUNCTION ..." > docker/init/03-create-functions.sql

# 적용
docker-compose down -v
docker-compose up -d postgres
```

## 권장 사항

### 개발 환경

- ✅ Docker init scripts 사용
- ✅ 샘플 데이터 포함
- ✅ Flyway baseline-on-migrate

### 스테이징 환경

- ✅ Docker init scripts 또는 Flyway
- ⚠️ 샘플 데이터 제거
- ✅ Flyway 마이그레이션

### 프로덕션 환경

- ❌ Docker init scripts 사용 안함
- ✅ Flyway 마이그레이션만 사용
- ✅ 버전 관리 및 히스토리 추적
- ❌ 볼륨 삭제 절대 금지
