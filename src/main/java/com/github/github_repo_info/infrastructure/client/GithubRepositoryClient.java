package com.github.github_repo_info.infrastructure.client;

import com.github.github_repo_info.domain.Branch;
import com.github.github_repo_info.domain.Repository;
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

@AllArgsConstructor
@Service
public class GithubRepositoryClient {
    private final RestTemplate restTemplate;
    private final GithubApiConfigurationProperties properties;

    public List<Repository> findRepositories(String username) {
        validUsername(username);
        HttpEntity<String> requestEntity = readHttpEntity();
        ResponseEntity<Repository[]> response = getResponseRepository(requestEntity, username);
        return Arrays.asList(Objects.requireNonNull(response.getBody()));
    }

    private static void validUsername(String username) {
        if(username == null){
            throw new IllegalArgumentException("Username repositories is empty");
        }
    }

    private ResponseEntity<Repository[]> getResponseRepository(HttpEntity<String> requestEntity, String username) {
        String url = properties.getUrl() + "/users/" + username + "/repos";
        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                Repository[].class);
    }

    private HttpEntity<String> readHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Authorization", "token " + properties.getToken());
        return new HttpEntity<>(headers);
    }

    public List<Branch> findBranches(String username, String repoName) {
        validRepositoryName(repoName);
        String url = properties.getUrl() + "/repos/" + username + "/" + repoName + "/branches";
        ResponseEntity<Branch[]> response = restTemplate.getForEntity(url, Branch[].class);
        return Arrays.asList(Objects.requireNonNull(response.getBody()));
    }

    private static void validRepositoryName(String repoName) {
        if (repoName == null) {
            throw new IllegalArgumentException("Repository name is null");
        }
    }
}
