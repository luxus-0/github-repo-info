package com.github.github_repo_info.infrastructure.client;

import com.github.github_repo_info.domain.Repository;

import java.util.List;

public interface GithubRepositoryClient {
    List<Repository> fetchRepositories(String username);
}
