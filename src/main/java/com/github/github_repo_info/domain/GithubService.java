package com.github.github_repo_info.domain;

import com.github.github_repo_info.domain.exception.ResourceNotFoundException;
import com.github.github_repo_info.infrastructure.client.GithubRepositoryClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@AllArgsConstructor
public class GithubService {

    private final GithubRepositoryClient githubClient;

    public List<RepositoryInfo> findRepositories(String username) {
        if(username == null){
            throw new IllegalArgumentException("Repository username not found");
        }
        RepositoryInfo repositoryInfo = mapToRepository(username);
        return List.of(repositoryInfo);
        }

        public CompletableFuture<List<RepositoryInfo>> findRepositoriesAsync(String username) {
            return CompletableFuture.supplyAsync(() -> {
                if (username == null) {
                    throw new IllegalArgumentException("Repository username not found");
                }
                RepositoryInfo repository = mapToRepository(username);
                return List.of(repository);
            });
        }

    private RepositoryInfo mapToRepository(String username) {
        List<Repository> repositories = githubClient.findRepositories(username);
        return repositories.stream()
                .filter(repository -> !repository.fork())
                .map(repository -> createRepository(username, repository))
                .findAny()
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found"));
    }

    private RepositoryInfo createRepository(String username, Repository repository) {
        List<Branch> branches = githubClient.findBranches(username, repository.name());
        return new RepositoryInfo(repository.name(), repository.login(), branches);
    }
}
