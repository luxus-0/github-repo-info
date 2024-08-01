package com.github.github_repo_info.infrastructure.errorhanler;

public record GithubErrorResponseDto(int statusCode, String message) {
}
