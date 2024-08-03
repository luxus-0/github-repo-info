package com.github.github_repo_info.infrastructure.client;

import com.github.github_repo_info.domain.Branch;
import com.github.github_repo_info.domain.RepositoryInfo;

import java.util.List;

public class TestUtility {
     static List<Branch> getBranches(List<RepositoryInfo> results) {
        return results.stream().map(RepositoryInfo::branches).findAny().orElse(List.of());
    }

    static String getRepositoryLogin(List<RepositoryInfo> results) {
        return results.stream().map(RepositoryInfo::ownerLogin).findAny().orElse("Empty repo login");
    }

    static String getRepositoryName(List<RepositoryInfo> results) {
        return results.stream()
                .map(RepositoryInfo::name)
                .findAny()
                .orElse("Empty repo name");
    }
}
