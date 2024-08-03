package com.github.github_repo_info.infrastructure.client;

import com.github.github_repo_info.domain.GithubService;
import com.github.github_repo_info.domain.RepositoryInfo;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/github")
public class GithubController {

    private final GithubService githubService;

    @GetMapping("/repos/{username}")
    public ResponseEntity<List<RepositoryInfo>> getRepositories(@PathVariable String username) {
        return ResponseEntity.ok(githubService.findRepositories(username));
    }
}
