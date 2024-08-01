package com.github.github_repo_info.infrastructure.client;

import com.github.github_repo_info.domain.GithubApiConfigurationProperties;
import com.github.github_repo_info.domain.RepositoryInfo;
import com.github.github_repo_info.domain.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class GithubRepositoryClientImpl implements GithubRepositoryClient {
    private final RestTemplate restTemplate;
    private final GithubApiConfigurationProperties properties;

    @Override
    public List<RepositoryInfo> fetchRepositories(String username) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept","application/json");
        headers.set("Authorization", "token " + properties.getToken());
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<RepositoryInfo[]> response = restTemplate.exchange(
                properties.getUrl(),
                HttpMethod.GET,
                requestEntity,
                RepositoryInfo[].class
        );
        return Optional.ofNullable(response.getBody())
                .map(Arrays::asList)
                .orElseThrow(() -> new ResourceNotFoundException("No repositories found for user: " + username));
    }
}
