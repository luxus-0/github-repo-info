package com.github.github_repo_info.infrastructure.errorhandler;

public record GithubErrorResponseDto(int statusCode, String message) {
}
