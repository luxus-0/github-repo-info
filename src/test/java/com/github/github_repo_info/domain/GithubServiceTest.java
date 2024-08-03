package com.github.github_repo_info.domain;

import com.github.github_repo_info.domain.exception.ResourceNotFoundException;
import com.github.github_repo_info.infrastructure.client.GithubRepositoryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class GithubServiceTest {
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private GithubRepositoryClient githubClient;

    @InjectMocks
    private GithubService githubService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnRepositoryInfoList() {
        // Given
        String username = "testuser";
        Repository repo1 = new Repository("repo1", "testuser", false);
        Repository repo2 = new Repository("repo2", "testuser", true);
        List<Repository> repositories = Arrays.asList(repo1, repo2);

        Branch branch1 = new Branch("branch1", "sha1");
        Branch branch2 = new Branch("branch2", "sha2");
        List<Branch> branches = Arrays.asList(branch1, branch2);

        when(githubClient.findRepositories(username)).thenReturn(repositories);
        when(githubClient.findBranches(username, "repo1")).thenReturn(branches);

        // When
        List<RepositoryInfo> result = githubService.findRepositories(username);

        // Then
        assertEquals(1, result.size());
        RepositoryInfo repoInfo = result.getFirst();
        assertEquals("repo1", repoInfo.name());
        assertEquals("testuser", repoInfo.ownerLogin());
        assertEquals(2, repoInfo.branches().size());
        assertTrue(repoInfo.branches().contains(branch1));
        assertTrue(repoInfo.branches().contains(branch2));
    }

    @Test
    void shouldReturnListOfBranches() {
        // Given
        String username = "testuser";
        Branch branch1 = new Branch("branch1", "sha1");
        Branch branch2 = new Branch("branch2", "sha2");
        Repository repository = new Repository("repo1", username, false);
        ResponseEntity<Branch[]> branchResponse = new ResponseEntity<>(new Branch[]{branch1, branch2}, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(Branch[].class))).thenReturn(branchResponse);

        when(githubClient.findRepositories(username)).thenReturn(List.of(repository));
        when(githubClient.findBranches(username, repository.name())).thenReturn(Arrays.asList(branch1, branch2));

        // When
        List<Branch> result = githubService.findRepositories(username).stream()
                .flatMap(repoInfo -> repoInfo.branches().stream())
                .toList();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(branch1));
        assertTrue(result.contains(branch2));
    }

    @ParameterizedTest
    @CsvSource({
            "luxus-0, repo1, Login1, main, abc123, dev, def456",
            "luxus-1, repo2, Login2, feature, ghi789, hotfix, jkl012",
            "luxus-2, repo3, Login3, release, mno345, ,",
            "luxus-3, repo4, Login4, main, pqr678, develop, stu901, bugfix, vwx234",
            "luxus-4, repo5, Login5, main, yza567, test, bcd890",
            "luxus-5, repo6, Login6, hotfix, efg234, feature, hij456",
            "luxus-6, repo7, Login7, dev, klm789, main, nop012"
    })
    void shouldReturnRepositoriesWithBranches(String username, String repoName, String ownerLogin, String branch1Name, String branch1Sha, String branch2Name, String branch2Sha) {
        // Given
        List<Repository> repositories = List.of(
                new Repository(repoName, ownerLogin, false)
        );

        List<Branch> branches = List.of(
                new Branch(branch1Name, branch1Sha),
                new Branch(branch2Name, branch2Sha)
        );

        when(githubClient.findRepositories(username)).thenReturn(repositories);
        when(githubClient.findBranches(username, repoName)).thenReturn(branches);

        // When
        List<RepositoryInfo> result = githubService.findRepositories(username);

        // Then

        RepositoryInfo repoInfo = result.getFirst();
        assertEquals(repoName, repoInfo.name());
        assertEquals(ownerLogin, repoInfo.ownerLogin());

        List<Branch> resultBranches = repoInfo.branches();
        assertEquals(branch1Name, resultBranches.getFirst().name());
        assertEquals(branch1Sha, resultBranches.getFirst().lastCommitSha());
    }

    @ParameterizedTest
    @CsvSource({
            "user1, repo1, branch1, false",
    })
    void shouldReturnRepositoryWithBranches(String username, String repoName, String branchName, boolean fork) {
        Repository repo = new Repository(repoName, username, fork);
        Branch branch = new Branch(branchName, "a3c0d0f4e1e1a58a317c456cd831e3d2489d5b0b");

        when(githubClient.findRepositories(username)).thenReturn(List.of(repo));
        when(githubClient.findBranches(username, repoName)).thenReturn(List.of(branch));

        List<RepositoryInfo> result = githubService.findRepositories(username);

        assertEquals(1, result.size());
        assertEquals(repoName, result.getFirst().name());
        assertEquals(1, result.getFirst().branches().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"user5"})
    void shouldThrowingResourceNotFoundWhenEmptyRepositoriesAndBranches(String username) {
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> githubService.findRepositories(username));
    }

    @ParameterizedTest
    @ValueSource(strings = {"null"})
    void shouldTrowingExceptionWhenUsernameIsNull() {
        assertThatThrownBy(() -> githubService.findRepositories(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Repository username not found");
    }

    @ParameterizedTest
    @ValueSource(strings = {"user"})
    void shouldThrowingResourceNotFoundExceptionWhenEmptyResponse(String username) {
        assertThatThrownBy(() -> githubService.findRepositories(username))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Repository not found");
    }

    @Test
    @Timeout(value = 160, unit = TimeUnit.MILLISECONDS)// timeout less than 160 milliseconds
    void shouldReturnRepositoriesForMaxUsernameLengthWithinTimeLimit() throws Exception {
        String maxLengthUsername = "user".repeat(50);

        when(githubClient.findRepositories(maxLengthUsername))
                .thenReturn(List.of(new Repository("repo", "owner", false)));

        CompletableFuture<List<RepositoryInfo>> future = githubService.findRepositoriesAsync(maxLengthUsername);

        List<RepositoryInfo> results = future.get();

        assertThat(results).isNotEmpty();
        assertThat(results.getFirst().name()).isEqualTo("repo");
    }

    @Test
    @Timeout(value = 80, unit = TimeUnit.MILLISECONDS) //timeout less than 80 milliseconds
    void shouldThrowIllegalArgumentExceptionForUsernameNullWithinTimeLimit() {

        CompletableFuture<List<RepositoryInfo>> future = githubService.findRepositoriesAsync(null);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertEquals("Repository username not found", exception.getCause().getMessage());
    }

    @Test
    @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
    void shouldReturnRepositoriesForMinUsernameLengthWithinTimeLimit() throws Exception {
        String minLengthUsername = "u";

        when(githubClient.findRepositories(minLengthUsername))
                .thenReturn(List.of(new Repository("repo", "owner", false)));
        when(githubClient.findBranches(minLengthUsername, "repo"))
                .thenReturn(List.of(new Branch("branch", "sha")));

        CompletableFuture<List<RepositoryInfo>> future = githubService.findRepositoriesAsync(minLengthUsername);

        List<RepositoryInfo> results = future.get();

        assertThat(results).isNotEmpty();
        assertThat(results.getFirst().ownerLogin()).isEqualTo("owner");
        assertThat(results.getFirst().name()).isEqualTo("repo");
        assertThat(results.getFirst().branches()).isNotNull();
    }

    @Test
    @Timeout(value = 129, unit = TimeUnit.MILLISECONDS)
    void shouldThrowExceptionWhenUsernameDoesNotExistWithinTimeLimit() {
        String nonExistentUsername = "nonExistentUser";

        when(githubClient.findRepositories(nonExistentUsername))
                .thenThrow(new ResourceNotFoundException("Repository not found"));

        CompletableFuture<List<RepositoryInfo>> future = githubService.findRepositoriesAsync(nonExistentUsername);

        assertThrows(ExecutionException.class, future::get, "AA");
    }

    @Test
    void shouldThrowExceptionForNonExistentUsernameAsync() {
        String nonExistentUsername = "nonExistentUser";
        when(githubClient.findRepositories(nonExistentUsername))
                .thenThrow(new ResourceNotFoundException("Repository not found"));

        CompletableFuture<List<RepositoryInfo>> future = githubService.findRepositoriesAsync(nonExistentUsername);

        assertThatThrownBy(future::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldReturnRepositoriesWithinTimeLimitAsync() throws Exception {
        String username = "user";
        when(githubClient.findRepositories(username))
                .thenReturn(List.of(new Repository("repo", "owner", false)));

        CompletableFuture<List<RepositoryInfo>> future = githubService.findRepositoriesAsync(username);

        List<RepositoryInfo> result = future.get(20, TimeUnit.MILLISECONDS);

        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().name()).isEqualTo("repo");
    }

    @Test
    void shouldReturnRepositoriesForUsernameAsyncWithJoin() {
        String validUsername = "user";
        when(githubClient.findRepositories(validUsername))
                .thenReturn(List.of(new Repository("repo", "owner", false)));
        when(githubClient.findBranches(validUsername, "repo"))
                .thenReturn(List.of(new Branch("branch", "sha")));

        CompletableFuture<List<RepositoryInfo>> future = githubService.findRepositoriesAsync(validUsername);

        List<RepositoryInfo> result = future.join();

        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().name()).isEqualTo("repo");
    }

    @Test
    void shouldReturnRepositoriesForMinUsernameLengthAsyncWithComplete() {
        String minLengthUsername = "u";
        CompletableFuture<List<RepositoryInfo>> future = new CompletableFuture<>();

        when(githubClient.findRepositories(minLengthUsername))
                .thenReturn(List.of(new Repository("repo", "owner", false)));
        when(githubClient.findBranches(minLengthUsername, "repo"))
                .thenReturn(List.of(new Branch("branch", "sha")));

        future.complete(List.of(new RepositoryInfo("repo", "owner", List.of(new Branch("branch", "sha")))));

        List<RepositoryInfo> result = future.join();

        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().name()).isEqualTo("repo");
    }

    @Test
    void shouldThrowExceptionForNonExistentUsernameAsyncWithCompleteExceptionally() {
        String nonExistentUsername = "nonExistentUser";

        when(githubClient.findRepositories(nonExistentUsername))
                .thenThrow(new ResourceNotFoundException("Repository not found"));

        CompletableFuture<List<RepositoryInfo>> future = githubService.findRepositoriesAsync(nonExistentUsername);

        assertThatThrownBy(future::join)
                .isInstanceOf(CompletionException.class)
                .hasCauseInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Repository not found");
    }
}
