package com.trendfeed.backend.controller;

import com.trendfeed.backend.entity.TrendingCandidateEntity;
import com.trendfeed.backend.service.GitHubService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private final GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    // 1) 초기 db에 수동으로 저장 테스트용
    //    예: GET /api/github/ingest?fullName=facebook/react
    @GetMapping("/ingest")
    public ResponseEntity<String> ingest(@RequestParam String fullName) {
        gitHubService.upsertRepository(fullName);
        return ResponseEntity.ok("ingested: " + fullName);
    }

    // 2) 수동으로 후보 만들기 테스트용
    //    예: POST /api/github/promote?fullName=facebook/react
    @PostMapping("/promote")
    public ResponseEntity<?> promote(@RequestParam String fullName) {
        TrendingCandidateEntity cand = gitHubService.promoteManually(fullName);
        return ResponseEntity.ok(cand);
    }

    // 3) ai호출용
    //    예: GET /api/github/nextCandidate
    @GetMapping("/nextCandidate")
    public ResponseEntity<?> nextCandidate() {
        TrendingCandidateEntity cand = gitHubService.getNextCandidateForAI();
        if (cand == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(cand);
    }
}
