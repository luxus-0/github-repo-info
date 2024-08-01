package com.github.github_repo_info.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@ConfigurationProperties(prefix = "github.api")
@Component
@AllArgsConstructor
public class GithubApiConfigurationProperties {
    private final String url;
    private final String token;
}
