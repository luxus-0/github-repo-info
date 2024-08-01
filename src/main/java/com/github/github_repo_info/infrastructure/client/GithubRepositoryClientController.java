package com.github.github_repo_info.infrastructure.client;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/github")
public class GithubRepositoryClientController {

    private final GithubRepositoryClient githubRepositoryClient;

    @GetMapping("/repos/{username}")
    public ResponseEntity<?> getUserRepositories(@PathVariable String username) {
        return ResponseEntity.ok(githubRepositoryClient.fetchRepositories(username));
    }
}
