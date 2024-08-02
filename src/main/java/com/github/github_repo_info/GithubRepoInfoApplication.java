package com.github.github_repo_info;

import com.github.github_repo_info.infrastructure.config.GithubApiConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(GithubApiConfigurationProperties.class)
public class GithubRepoInfoApplication {

	public static void main(String[] args) {
		SpringApplication.run(GithubRepoInfoApplication.class, args);
	}

}
