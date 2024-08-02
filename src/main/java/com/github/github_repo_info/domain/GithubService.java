package com.github.github_repo_info.domain;

import com.github.github_repo_info.infrastructure.client.GithubRepositoryClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class GithubService {

    private final GithubRepositoryClient githubClient;

    public List<RepositoryInfo> findReposWithBranches(String username){
        List<Repository> repositories = githubClient.findRepositories(username);
        return repositories.stream()
                .filter(repository -> !repository.fork())
                .map(repository -> {List<Branch> branches = githubClient.findBranches(username, repository.name());
                    return new RepositoryInfo(repository.name(), repository.login(), branches);
                })
                .toList();
    }
}
