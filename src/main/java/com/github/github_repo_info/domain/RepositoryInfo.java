package com.github.github_repo_info.domain;

import java.util.List;

public record RepositoryInfo(String name, String ownerLogin, List<Branch> branches){

}
