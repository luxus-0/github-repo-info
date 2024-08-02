package com.github.github_repo_info.infrastructure.client;

import com.github.github_repo_info.domain.Branch;
import com.github.github_repo_info.domain.Repository;
import com.github.github_repo_info.domain.exception.ResourceNotFoundException;
import com.github.github_repo_info.infrastructure.config.GithubApiConfigurationProperties;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
@Service
public class GithubRepositoryClient {
    private final RestTemplate restTemplate;
    private final GithubApiConfigurationProperties properties;

    public List<Repository> findRepositories(String username) {
        String url = properties.getUrl() + "/users/" + username + "/repos";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Authorization", "token " + properties.getToken());
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Repository[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                Repository[].class
        );
        return Optional.ofNullable(response.getBody())
                .map(Arrays::asList)
                .orElseThrow(() -> new ResourceNotFoundException("No repositories found for user: " + username));
    }

    public List<Branch> findBranches(String username, String repoName) {
        String url = properties.getUrl() + "/repos/" + username + "/" + repoName + "/branches";
        ResponseEntity<Branch[]> response = restTemplate.getForEntity(url, Branch[].class);
        return Arrays.asList(Objects.requireNonNull(response.getBody()));
    }
}
