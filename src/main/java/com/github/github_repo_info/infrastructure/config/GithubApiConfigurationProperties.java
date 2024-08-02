package com.github.github_repo_info.infrastructure.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "github.api")
@AllArgsConstructor
public class GithubApiConfigurationProperties {
    private final String url;
    private final String token;
}
