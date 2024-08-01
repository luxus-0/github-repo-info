package com.github.github_repo_info.infrastructure.client;

import com.github.github_repo_info.domain.RepositoryInfo;

import java.util.List;

public interface GithubRepositoryClient {
    List<RepositoryInfo> fetchRepositories(String username);
}
