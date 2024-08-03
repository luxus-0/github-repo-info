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

    static String getBodyRepositoryInfo() {
        return """
                [
                    {
                        "name": "repo1",
                        "ownerLogin": "Login1",
                        "branches": [
                            {
                            "name": "main",
                            lastCommitSha : "e3a1b6d6f34efb29b1c98a09a6a587b7efb485d2"
                            },
                            {
                            "name" : "main2",
                            "lastCommit": "e3a1b6d6f34efb29b1c98a09a6a587b7efb485d7"
                            }
                        ]
                    },
                    {
                        "name": "repo2",
                        "ownerLogin": "Login1",
                        "branches": []
                    }
                ]
                """;
    }

    static String GetBodyRepositoryInfo2() {
        return """
                [
                    {
                        "name": "repo1",
                        "ownerLogin": "Login1",
                        "branches": [
                            {
                            "name": "main",
                            lastCommitSha : "e3a1b6d6f34efb29b1c98a09a6a587b7efb485d2"
                            },
                            {
                            "name" : "main2",
                            "lastCommit": "e3a1b6d6f34efb29b1c98a09a6a587b7efb485d2"
                            }
                ]
                """;
    }

    static String getBodyBranches() {
        return """
                [
                    {
                        "name": "main",
                        "lastCommitSha" : "e3a1b6d6f34efb29b1c98a09a6a587b7efb485d2"
                    },
                    {
                        "name": "dev",
                        "lastCommitSha" : "a3a1b6d6f34efc49b1c98a09a6a587b7efb485d2"
                    }
                ]
                """;
    }

    static String getBodyRepository() {
        return """
                [
                    {
                        "name": "repo1",
                        "login": "Login1",
                        "fork": false
                    },
                    {
                        "name": "repo2",
                        "login": "Login1",
                        "fork": true
                    }
                ]
                """;
    }
}
