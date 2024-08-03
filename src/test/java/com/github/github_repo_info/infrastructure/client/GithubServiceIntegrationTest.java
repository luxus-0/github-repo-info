package com.github.github_repo_info.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.github_repo_info.domain.Branch;
import com.github.github_repo_info.domain.GithubService;
import com.github.github_repo_info.domain.Repository;
import com.github.github_repo_info.domain.RepositoryInfo;
import com.github.github_repo_info.domain.exception.ResourceNotFoundException;
import com.github.github_repo_info.infrastructure.errorhandler.GithubErrorResponseDto;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

import static com.github.github_repo_info.infrastructure.client.TestUtility.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(WireMockExtension.class)
class GithubServiceIntegrationTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private GithubRepositoryClient githubRepositoryClient;
    @Mock
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @InjectMocks
    private GithubService githubService;

    @Test
    void shouldReturnListOfRepositories() {
        // Given
        String username = "luxus-0";
        String repoName = "repo1";
        boolean fork = false;

        when(githubRepositoryClient.findRepositories(username)).thenReturn(List.of(new Repository(repoName, username, fork)));

        // When
        List<RepositoryInfo> result = githubService.findRepositories(username);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        result.forEach(repo -> {
            assertNotNull(repo.name());
            assertNotNull(repo.ownerLogin());
        });
    }

    @Test
    void shouldThrowingNotFound404WhenUsernameNotExist() throws Exception {

        String json = mockMvc.perform(get("/repos/luxus-0")
                        .contentType("application/json"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        GithubErrorResponseDto result = objectMapper.readValue(json, GithubErrorResponseDto.class);

        assertThat(result).isNotNull();
        assertThat(result.statusCode()).isEqualTo(404);
        assertThat(result.message()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
    }

    @Test
    void shouldThrowingNotFound404WhenUsernameIncorrect() {
        // Given

        String invalidUsername = "error";

        assertThrows(Exception.class, () -> {
             mockMvc.perform(get("/repos/" + invalidUsername).contentType(APPLICATION_JSON))
                     .andExpect(result -> {
                         assertThat(result.getResolvedException()).isInstanceOf(ResourceNotFoundException.class);
                         assertThat(Objects.requireNonNull(result.getResolvedException()).getMessage()).contains("Repository not found");
            });
        });
    }

    @Test
    public void shouldReturnRepositories() {
        // given
        String username = "testUsername";

        stubFor(WireMock.get("/api/github/repos/" + username)
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(GetBodyRepositoryInfo2().trim())))
                .getResponse()
                .getBody();

        //when
        when(githubRepositoryClient.findRepositories(username)).thenReturn(List.of(new Repository("repo1", "luxus-0", false)));
        when(githubRepositoryClient.findBranches("luxus-0", "repo1")).thenReturn(List.of(new Branch("Branch1", "e3a1b6d6f34efb29b1c98a09a6a587b7efb485d2")));

        List<RepositoryInfo> results = githubService.findRepositories(username);
        String repoName = getRepositoryName(results);
        String repoLogin = getRepositoryLogin(results);
        List<Branch> branches = getBranches(results);


        //then
        assertThat(results).isNotNull();
        assertThat(branches).isNotNull();
        assertThat(repoName).isEqualTo("repo1");
        assertThat(repoLogin).isEqualTo("luxus-0");

    }

    @Test
    public void shouldReturnRepositoriesa() {
        //given
        String username = "luxus-0";

        stubFor(WireMock.get("/api/github/repos/" + username)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withBody(getBodyRepositoryInfo().trim())))
                .getResponse()
                .getBody();

        //when
        when(githubRepositoryClient.findRepositories(username))
                .thenReturn(List.of(
                        new Repository("repo1", "Login1", false),
                        new Repository("repo2", "Login2", false)));

        List<RepositoryInfo> results = githubService.findRepositories(username);

        //then
        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(1);
    }

    @Test
    public void shouldReturnRepositoriesWithBranches() {
        //given
        String username = "luxus-0";

        stubFor(WireMock.get(urlEqualTo("/users/" + username + "/repos"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withBody(getBodyRepository().trim())));

        stubFor(WireMock.get(urlEqualTo("/repos/" + username + "/repo1/branches"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withBody(getBodyBranches().trim())));

        //when
        when(githubRepositoryClient.findRepositories(username)).thenReturn(List.of(new Repository("repo1", "Login1", false)));
        when(githubRepositoryClient.findBranches(username, "repo1")).thenReturn(List.of(new Branch("main", "lastCommit"), new Branch("dev", "lastCommit2")));

        List<RepositoryInfo> results = githubService.findRepositories(username);

        //then
        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(1);

        RepositoryInfo repoInfo = results.getFirst();
        assertThat(repoInfo.name()).isEqualTo("repo1");
        assertThat(repoInfo.ownerLogin()).isEqualTo("Login1");

        List<Branch> branches = repoInfo.branches();
        assertThat(branches).isNotNull();
        assertThat(branches.size()).isEqualTo(2);
        assertThat(branches.get(0).name()).isEqualTo("main");
        assertThat(branches.get(1).name()).isEqualTo("dev");
    }
}