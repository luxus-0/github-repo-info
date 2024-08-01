package com.github.github_repo_info.infrastructure.errorhandler;

import com.github.github_repo_info.domain.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GithubRepositoryErrorHandlerClient {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GithubErrorResponseDto> handleResourceNotFoundException(ResourceNotFoundException exception) {
        GithubErrorResponseDto errorResponse = new GithubErrorResponseDto(HttpStatus.NOT_FOUND.value(), exception.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}
