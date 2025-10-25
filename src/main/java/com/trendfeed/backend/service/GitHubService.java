package com.trendfeed.backend.service;

import com.trendfeed.backend.entity.GitHubEntity;
import com.trendfeed.backend.entity.TrendingCandidateEntity;
import com.trendfeed.backend.repository.GitHubRepository;
import com.trendfeed.backend.repository.TrendingCandidateRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class GitHubService {

    private final WebClient github;
    private final GitHubRepository repoRepo;
    private final TrendingCandidateRepository candidateRepo;

    // ---- 수집 파라미터 ----
    // 초기 수집 조건

    @Value("${crawler.star-threshold:300}") //최소 star 조건
    private int minStars;

    @Value("${crawler.lookback-years:2}") //최소 업로드 날짜
    private int lookbackYears;

    // 한 번에 가져올 페이지 크기 / 페이지 수 한도
    @Value("${crawler.per-page:50}")
    private int perPage;

    @Value("${crawler.max-pages:3}")
    private int maxPages;

    // 트렌드 판단 조건(추후 수정)
    @Value("${crawler.trending-threshold:20}")
    private int trendingThreshold;

    public GitHubService(
            WebClient githubWebClient,
            GitHubRepository repoRepo,
            TrendingCandidateRepository candidateRepo
    ) {
        this.github = githubWebClient;
        this.repoRepo = repoRepo;
        this.candidateRepo = candidateRepo;
    }

    // -------- 유틸: "owner/repo" 분리 --------
    private static String[] splitFullName(String fullName) {
        if (fullName == null || !fullName.contains("/")) {
            throw new IllegalArgumentException("fullName must be like 'owner/repo'");
        }
        String[] parts = fullName.split("/", 2);
        return new String[]{ parts[0].trim(), parts[1].trim() };
    }

    private OffsetDateTime parseTime(String iso) {
        return iso == null ? null : OffsetDateTime.parse(iso);
    }

    // ============================================================
    // 1) 초기/주기 크롤링:
    //    githubEntity에 수집
    // ============================================================

    @Scheduled(cron = "${crawler.crawl-cron:0 0 3 * * *}") // 수집 스케줄 설정
    public void crawlRecentPopularRepos() {
        String createdAfter = LocalDate.now()
                .minusYears(lookbackYears)
                .format(DateTimeFormatter.ISO_LOCAL_DATE);

        // GitHub search query
        String q = "created:>=" + createdAfter + "+stars:>=" + minStars;

        for (int page = 1; page <= maxPages; page++) {
            final int currentPage = page;
            final int pageSize = perPage;

            Map<String, Object> searchResult = github.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/repositories")
                            .queryParam("q", q)
                            .queryParam("sort", "stars")
                            .queryParam("order", "desc")
                            .queryParam("per_page", pageSize)
                            .queryParam("page", currentPage)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (searchResult == null) break;

            List<Map<String, Object>> items = (List<Map<String, Object>>) searchResult.get("items");
            if (items == null || items.isEmpty()) break;

            for (Map<String, Object> item : items) {
                String fullName = (String) item.get("full_name"); // "owner/repo"
                try {
                    upsertRepository(fullName);
                    Thread.sleep(120L); // rate limit 완화용 얕은 딜레이
                } catch (Exception ignore) {
                    // TODO: 로깅
                }
            }
        }
    }

    // ============================================================
    // 2) 매일 스타 수 갱신 & 증가량 체크
    //    - repoRepo에 저장된 모든 repo를 다시 GitHub에서 조회
    //    - 새 star 수 - 예전 star 수 = delta
    //    - delta >= trendingThreshold 면 후보 테이블행
    // ============================================================

    @Scheduled(cron = "${crawler.delta-cron:0 0 4 * * *}") //갱신 스케줄 설정
    public void refreshStarCountsAndMarkCandidates() {
        List<GitHubEntity> all = repoRepo.findAll();
        for (GitHubEntity repo : all) {
            try {
                String[] parts = splitFullName(repo.getFullName());
                Map<String, Object> meta = github.get()
                        .uri("/repos/{owner}/{repo}", parts[0], parts[1])
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

                if (meta == null) continue;

                Integer oldStars = repo.getStargazersCount();
                Integer newStars = toInt(meta.get("stargazers_count"));

                int delta = 0;
                if (oldStars != null && newStars != null) {
                    delta = newStars - oldStars;
                }

                // repo 엔티티 업데이트
                mapIntoExistingEntity(repo, meta);
                repo.setLastDelta24h(delta);
                repo.setLastStarCheck(OffsetDateTime.now());
                repoRepo.save(repo);

                // 만약 delta가 기준 넘으면 후보로 push
                if (delta >= trendingThreshold) {
                    pushToCandidate(repo, delta);
                }

                Thread.sleep(120L);
            } catch (Exception ignore) {
                // TODO 로깅
            }
        }
    }

    // ============================================================
    // 3) 단일 리포지토리를 즉시 DB에 upsert (테스트용)
    //    - 메타 + README 저장
    // ============================================================
    public void upsertRepository(String fullName) {
        String[] parts = splitFullName(fullName);

        // (1) 메타데이터
        Map<String, Object> meta = github.get()
                .uri("/repos/{owner}/{repo}", parts[0], parts[1])
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (meta == null) return;

        Long repoId = ((Number) meta.get("id")).longValue();

        GitHubEntity existing = repoRepo.findById(repoId).orElse(null);
        GitHubEntity e = (existing != null) ? existing : new GitHubEntity();

        mapIntoExistingEntity(e, meta);

        // (2) README (etag 조건부)
        fetchAndAttachReadme(parts[0], parts[1], e);

        e.setLastCrawledAt(OffsetDateTime.now());

        // 신규라면 lastDelta24h 아직 없음 → 0으로
        if (e.getLastDelta24h() == null) {
            e.setLastDelta24h(0);
        }
        if (e.getLastStarCheck() == null) {
            e.setLastStarCheck(OffsetDateTime.now());
        }

        repoRepo.save(e);
    }

    // ============================================================
    // 4) 강제로 db > 후보 db 엔트리 (ai연결 테스트용) 
    // ============================================================

    public TrendingCandidateEntity promoteManually(String fullName) {
        // fullName을 아직 DB에 안 넣었으면 upsert해서 확보
        GitHubEntity repo = repoRepo.findByFullName(fullName);
        if (repo == null) {
            upsertRepository(fullName);
            repo = repoRepo.findByFullName(fullName);
            if (repo == null) {
                throw new RuntimeException("failed to upsert/persist repo " + fullName);
            }
        }

        // delta는 그냥 0으로, 지금 시점 snapshot만 후보로
        return pushToCandidate(repo, 0);
    }

    // ============================================================
    // 5) AI 호출 반환용
    // ============================================================

    public TrendingCandidateEntity getNextCandidateForAI() {

    TrendingCandidateEntity cand = candidateRepo
        .findFirstByProcessedFalseOrderByCapturedAtAsc()
        .orElse(null); // <-- Optional에서 실제 엔티티 뽑거나 없으면 null

    if (cand == null) {
        return null;
    }
    
    cand.setProcessed(true);
    candidateRepo.save(cand);

    return cand;
}

    // ============================================================
    // 내부 유틸: README 가져오기 (+etag 활용)
    // ============================================================
    private void fetchAndAttachReadme(String owner, String repoName, GitHubEntity e) {
        ClientResponse response = github.get()
                .uri("/repos/{owner}/{repo}/readme", owner, repoName)
                .headers(h -> {
                    if (e.getReadmeEtag() != null) {
                        h.add("If-None-Match", e.getReadmeEtag());
                    }
                })
                .exchange()
                .block();

        if (response == null) return;

        if (response.statusCode() == HttpStatus.NOT_MODIFIED) {
            // 304 → 변경 없음
            return;
        }

        if (response.statusCode().is2xxSuccessful()) {
            Map<String, Object> readme = response.bodyToMono(Map.class).block();
            if (readme == null) return;

            String encoded = (String) readme.get("content");
            String encoding = (String) readme.get("encoding"); // 기대: "base64"
            String sha = (String) readme.get("sha");

            String text = null;
            if (encoded != null && "base64".equalsIgnoreCase(encoding)) {
                byte[] bytes = Base64.getDecoder().decode(encoded.getBytes(StandardCharsets.UTF_8));
                text = new String(bytes, StandardCharsets.UTF_8);
            }

            e.setReadmeText(text);
            e.setReadmeSha(sha);
            // 응답 헤더의 ETag 저장
            String etag = response.headers().asHttpHeaders().getETag();
            e.setReadmeEtag(etag);
        }
    }

    // ============================================================
    // 내부 유틸: GitHub API 메타 -> GitHubEntity 에 덮어쓰기
    // (upsertRepository()와 refreshStarCountsAndMarkCandidates()에서 재사용)
    // ============================================================
    @SuppressWarnings("unchecked")
    private void mapIntoExistingEntity(GitHubEntity e, Map<String, Object> meta) {
        e.setId(((Number) meta.get("id")).longValue());
        e.setNodeId((String) meta.get("node_id"));
        e.setName((String) meta.get("name"));
        e.setFullName((String) meta.get("full_name"));

        Map<String, Object> owner = (Map<String, Object>) meta.get("owner");
        e.setOwnerLogin(owner != null ? (String) owner.get("login") : null);

        e.setHtmlUrl((String) meta.get("html_url"));
        e.setDescription((String) meta.get("description"));
        e.setLanguage((String) meta.get("language"));

        Integer starsNow = toInt(meta.get("stargazers_count"));
        e.setStargazersCount(starsNow);

        e.setCreatedAt(parseTime((String) meta.get("created_at")));
        e.setPushedAt(parseTime((String) meta.get("pushed_at")));
        e.setUpdatedAt(parseTime((String) meta.get("updated_at")));
    }

    private Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        return Integer.parseInt(String.valueOf(o));
    }

    // ============================================================
    // 내부 유틸: 후보 테이블에 밀어넣기
    // ============================================================
    private TrendingCandidateEntity pushToCandidate(GitHubEntity repo, int delta) {
        TrendingCandidateEntity cand = new TrendingCandidateEntity();
        cand.setRepoId(repo.getId());
        cand.setFullName(repo.getFullName());
        cand.setHtmlUrl(repo.getHtmlUrl());
        cand.setDescription(repo.getDescription());
        cand.setLanguage(repo.getLanguage());
        cand.setStarCountAtCapture(repo.getStargazersCount());
        cand.setDeltaLast24h(delta);
        cand.setCapturedAt(OffsetDateTime.now());
        cand.setReadmeSnapshot(repo.getReadmeText());
        cand.setProcessed(false);

        return candidateRepo.save(cand);
    }
}
