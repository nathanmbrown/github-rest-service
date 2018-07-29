package server.api.verticle;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import server.api.model.ProjectInfo;

import java.util.*;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ProjectsApiImplTest
{
  private ProjectsApiImpl projectsApi = new ProjectsApiImpl(Vertx.vertx());

  @Test
  public void testInvalidLanguageFails() throws Exception
  {
    Future<List<ProjectInfo>> future = Future.future();
    projectsApi.findProjectsByLanguage(null, future);
    assertTrue(future.failed());
    assertTrue(future.cause() instanceof ProjectsApiException &&
               ((ProjectsApiException)future.cause()).getStatusCode() == 400);
  }

  @Test
  public void testCreateProjectInfo() throws Exception
  {
    JsonObject jsonObject = new JsonObject(TEST_PROJECT_JSON);
    ProjectInfo projectInfo = ProjectsApiImpl.createProjectInfo(jsonObject);
    assertThat(projectInfo.getId(), is(3081286L));
    assertThat(projectInfo.getName(), is("Tetris"));
    assertThat(projectInfo.getOwner(), is("dtrupenn"));
    assertThat(projectInfo.getUrl(), is("https://github.com/dtrupenn/Tetris"));
  }

  @Test
  public void testCreateProjectInfos() throws Exception
  {
    JsonObject page = new JsonObject(TEST_PAGE_JSON);
    Stream<ProjectInfo> projectInfos = ProjectsApiImpl.createProjectInfos(page);
    Optional<ProjectInfo> first = projectInfos.findFirst();
    ProjectInfo projectInfo = first.get();
    assertThat(projectInfo.getId(), is(3081286L));
    assertThat(projectInfo.getName(), is("Tetris"));
    assertThat(projectInfo.getOwner(), is("dtrupenn"));
    assertThat(projectInfo.getUrl(), is("https://github.com/dtrupenn/Tetris"));
  }

  @Test
  public void testCreateAllProjectInfos() throws Exception
  {
    JsonObject[] pages = new JsonObject[]{new JsonObject(TEST_PAGE_JSON),
                                          new JsonObject(TEST_PAGE_JSON_2)};
    List<ProjectInfo> projectInfos = ProjectsApiImpl.createAllProjectInfos(Arrays.asList(pages));
    ProjectInfo projectInfo = projectInfos.get(0);
    assertThat(projectInfo.getId(), is(3081286L));
    assertThat(projectInfo.getName(), is("Tetris"));
    assertThat(projectInfo.getOwner(), is("dtrupenn"));
    assertThat(projectInfo.getUrl(), is("https://github.com/dtrupenn/Tetris"));
    projectInfo = projectInfos.get(1);
    assertThat(projectInfo.getId(), is(68911683L));
    assertThat(projectInfo.getName(), is("tetros"));
    assertThat(projectInfo.getOwner(), is("daniel-e"));
    assertThat(projectInfo.getUrl(), is("https://github.com/daniel-e/tetros"));
  }

  @Test
  public void testGitHubServiceFailHandled() throws Exception
  {
    // incorrect url
    projectsApi.setGitHubServiceClient(new GitHubServiceClientImpl() {
      @NotNull
      @Override
      protected String getRepositorySearchURI(String language)
      {
        return "https://api.github.com/sarch/repositories?per_page=100&q=language:" + language;
      }
    });
    Future<List<ProjectInfo>> future = Future.future();
    projectsApi.findProjectsByLanguage("java", future);
    assertThat(future.failed(), is(true));
    assertThat(future.cause(), is(instanceOf(ProjectsApiException.class)));
    assertThat(((ProjectsApiException)future.cause()).getStatusCode(), is(500));
  }

  @Test
  public void testParsePageFailHandled() throws Exception
  {
    projectsApi.setGitHubServiceClient(language -> Collections.singletonList(new JsonObject(PAGE_FAIL_PAGE_JSON)));
    Future<List<ProjectInfo>> future = Future.future();
    projectsApi.findProjectsByLanguage("java", future);
    assertThat(future.failed(), is(true));
    assertThat(future.cause(), is(instanceOf(ProjectsApiException.class)));
    assertThat(((ProjectsApiException)future.cause()).getStatusCode(), is(500));
  }

  @Test
  public void testParseProjectFailHandled() throws Exception
  {
    projectsApi.setGitHubServiceClient(language -> Collections.singletonList(new JsonObject(PROJECT_FAIL_PAGE_JSON)));
    Future<List<ProjectInfo>> future = Future.future();
    projectsApi.findProjectsByLanguage("java", future);
    assertThat(future.failed(), is(true));
    assertThat(future.cause(), is(instanceOf(ProjectsApiException.class)));
    assertThat(((ProjectsApiException)future.cause()).getStatusCode(), is(500));
  }

  //  @Test
//  public void manualTestFindProjectsByLanguage() throws Exception
//  {
//    final Throwable[] error = {null};
//    Future<List<ProjectInfo>> result = Future.future();
//    _projectsApi.findProjectsByLanguage("chapel", result);
//    if (result.failed()) {
//      if (result.cause() != null) {
//        error[0] = result.cause();
//      } else {
//        error[0] = new Exception("Failed.");
//      }
//    } else {
//      result.result().forEach(System.out::println);
//    }
//  }

  protected static final String TEST_PROJECT_JSON = "{      \"id\": 3081286,\n" +
                                                    "      \"name\": \"Tetris\",\n" +
                                                    "      \"full_name\": \"dtrupenn/Tetris\",\n" +
                                                    "      \"owner\": {\n" +
                                                    "        \"login\": \"dtrupenn\",\n" +
                                                    "        \"id\": 872147,\n" +
                                                    "        \"avatar_url\": \"https://secure.gravatar.com/avatar/e7956084e75f239de85d3a31bc172ace?d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-user-420.png\",\n" +
                                                    "        \"gravatar_id\": \"\",\n" +
                                                    "        \"url\": \"https://api.github.com/users/dtrupenn\",\n" +
                                                    "        \"received_events_url\": \"https://api.github.com/users/dtrupenn/received_events\",\n" +
                                                    "        \"type\": \"User\"\n" +
                                                    "      },\n" +
                                                    "      \"private\": false,\n" +
                                                    "      \"html_url\": \"https://github.com/dtrupenn/Tetris\",\n" +
                                                    "      \"description\": \"A C implementation of Tetris using Pennsim through LC4\",\n" +
                                                    "      \"fork\": false,\n" +
                                                    "      \"url\": \"https://api.github.com/repos/dtrupenn/Tetris\",\n" +
                                                    "      \"created_at\": \"2012-01-01T00:31:50Z\",\n" +
                                                    "      \"updated_at\": \"2013-01-05T17:58:47Z\",\n" +
                                                    "      \"pushed_at\": \"2012-01-01T00:37:02Z\",\n" +
                                                    "      \"homepage\": \"\",\n" +
                                                    "      \"size\": 524,\n" +
                                                    "      \"stargazers_count\": 1,\n" +
                                                    "      \"watchers_count\": 1,\n" +
                                                    "      \"language\": \"Assembly\",\n" +
                                                    "      \"forks_count\": 0,\n" +
                                                    "      \"open_issues_count\": 0,\n" +
                                                    "      \"master_branch\": \"master\",\n" +
                                                    "      \"default_branch\": \"master\",\n" +
                                                    "      \"score\": 10.309712}";

  protected static final String TEST_PAGE_JSON = "{\n" +
                                                 "  \"total_count\": 40,\n" +
                                                 "  \"incomplete_results\": false,\n" +
                                                 "  \"items\": [\n" +
                                                 TEST_PROJECT_JSON +
                                                 "  ]\n" +
                                                 "}";

  protected static final String PAGE_FAIL_PAGE_JSON = "{\n" +
                                                      "  \"total_count\": 40,\n" +
                                                      "  \"incomplete_results\": false,\n" +
                                                      "  \"elements\": [\n" +
                                                      TEST_PROJECT_JSON +
                                                      "  ]\n" +
                                                      "}";

  protected static final String PROJECT_FAIL_PAGE_JSON = "{\n" +
                                                      "  \"total_count\": 40,\n" +
                                                      "  \"incomplete_results\": false,\n" +
                                                      "  \"items\": [\n" +
                                                         "{      \"id\": 3081286,\n" +
                                                          "      \"full_name\": \"dtrupenn/Tetris\",\n" +
                                                          "      \"owner\": {\n" +
                                                          "        \"login\": \"dtrupenn\",\n" +
                                                          "        \"id\": 872147,\n" +
                                                          "        \"avatar_url\": \"https://secure.gravatar.com/avatar/e7956084e75f239de85d3a31bc172ace?d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-user-420.png\",\n" +
                                                          "        \"gravatar_id\": \"\",\n" +
                                                          "        \"url\": \"https://api.github.com/users/dtrupenn\",\n" +
                                                          "        \"received_events_url\": \"https://api.github.com/users/dtrupenn/received_events\",\n" +
                                                          "        \"type\": \"User\"\n" +
                                                          "      }}\n" +
                                                      "  ]\n" +
                                                      "}";

  protected static final String TEST_PAGE_JSON_2 = "{\n" +
                                                   "  \"total_count\": 1014,\n" +
                                                   "  \"incomplete_results\": false,\n" +
                                                   "  \"items\": [\n" +
                                                   "    {\n" +
                                                   "      \"id\": 68911683,\n" +
                                                   "      \"name\": \"tetros\",\n" +
                                                   "      \"full_name\": \"daniel-e/tetros\",\n" +
                                                   "      \"owner\": {\n" +
                                                   "        \"login\": \"daniel-e\",\n" +
                                                   "        \"id\": 5294331,\n" +
                                                   "        \"avatar_url\": \"https://avatars2.githubusercontent.com/u/5294331?v=4\",\n" +
                                                   "        \"gravatar_id\": \"\",\n" +
                                                   "        \"url\": \"https://api.github.com/users/daniel-e\",\n" +
                                                   "        \"html_url\": \"https://github.com/daniel-e\",\n" +
                                                   "        \"followers_url\": \"https://api.github.com/users/daniel-e/followers\",\n" +
                                                   "        \"following_url\": \"https://api.github.com/users/daniel-e/following{/other_user}\",\n" +
                                                   "        \"gists_url\": \"https://api.github.com/users/daniel-e/gists{/gist_id}\",\n" +
                                                   "        \"starred_url\": \"https://api.github.com/users/daniel-e/starred{/owner}{/repo}\",\n" +
                                                   "        \"subscriptions_url\": \"https://api.github.com/users/daniel-e/subscriptions\",\n" +
                                                   "        \"organizations_url\": \"https://api.github.com/users/daniel-e/orgs\",\n" +
                                                   "        \"repos_url\": \"https://api.github.com/users/daniel-e/repos\",\n" +
                                                   "        \"events_url\": \"https://api.github.com/users/daniel-e/events{/privacy}\",\n" +
                                                   "        \"received_events_url\": \"https://api.github.com/users/daniel-e/received_events\",\n" +
                                                   "        \"type\": \"User\",\n" +
                                                   "        \"site_admin\": false\n" +
                                                   "      },\n" +
                                                   "      \"private\": false,\n" +
                                                   "      \"html_url\": \"https://github.com/daniel-e/tetros\",\n" +
                                                   "      \"description\": \"Tetris that fits into the boot sector.\",\n" +
                                                   "      \"fork\": false,\n" +
                                                   "      \"url\": \"https://api.github.com/repos/daniel-e/tetros\",\n" +
                                                   "      \"forks_url\": \"https://api.github.com/repos/daniel-e/tetros/forks\",\n" +
                                                   "      \"keys_url\": \"https://api.github.com/repos/daniel-e/tetros/keys{/key_id}\",\n" +
                                                   "      \"collaborators_url\": \"https://api.github.com/repos/daniel-e/tetros/collaborators{/collaborator}\",\n" +
                                                   "      \"teams_url\": \"https://api.github.com/repos/daniel-e/tetros/teams\",\n" +
                                                   "      \"hooks_url\": \"https://api.github.com/repos/daniel-e/tetros/hooks\",\n" +
                                                   "      \"issue_events_url\": \"https://api.github.com/repos/daniel-e/tetros/issues/events{/number}\",\n" +
                                                   "      \"events_url\": \"https://api.github.com/repos/daniel-e/tetros/events\",\n" +
                                                   "      \"assignees_url\": \"https://api.github.com/repos/daniel-e/tetros/assignees{/user}\",\n" +
                                                   "      \"branches_url\": \"https://api.github.com/repos/daniel-e/tetros/branches{/branch}\",\n" +
                                                   "      \"tags_url\": \"https://api.github.com/repos/daniel-e/tetros/tags\",\n" +
                                                   "      \"blobs_url\": \"https://api.github.com/repos/daniel-e/tetros/git/blobs{/sha}\",\n" +
                                                   "      \"git_tags_url\": \"https://api.github.com/repos/daniel-e/tetros/git/tags{/sha}\",\n" +
                                                   "      \"git_refs_url\": \"https://api.github.com/repos/daniel-e/tetros/git/refs{/sha}\",\n" +
                                                   "      \"trees_url\": \"https://api.github.com/repos/daniel-e/tetros/git/trees{/sha}\",\n" +
                                                   "      \"statuses_url\": \"https://api.github.com/repos/daniel-e/tetros/statuses/{sha}\",\n" +
                                                   "      \"languages_url\": \"https://api.github.com/repos/daniel-e/tetros/languages\",\n" +
                                                   "      \"stargazers_url\": \"https://api.github.com/repos/daniel-e/tetros/stargazers\",\n" +
                                                   "      \"contributors_url\": \"https://api.github.com/repos/daniel-e/tetros/contributors\",\n" +
                                                   "      \"subscribers_url\": \"https://api.github.com/repos/daniel-e/tetros/subscribers\",\n" +
                                                   "      \"subscription_url\": \"https://api.github.com/repos/daniel-e/tetros/subscription\",\n" +
                                                   "      \"commits_url\": \"https://api.github.com/repos/daniel-e/tetros/commits{/sha}\",\n" +
                                                   "      \"git_commits_url\": \"https://api.github.com/repos/daniel-e/tetros/git/commits{/sha}\",\n" +
                                                   "      \"comments_url\": \"https://api.github.com/repos/daniel-e/tetros/comments{/number}\",\n" +
                                                   "      \"issue_comment_url\": \"https://api.github.com/repos/daniel-e/tetros/issues/comments{/number}\",\n" +
                                                   "      \"contents_url\": \"https://api.github.com/repos/daniel-e/tetros/contents/{+path}\",\n" +
                                                   "      \"compare_url\": \"https://api.github.com/repos/daniel-e/tetros/compare/{base}...{head}\",\n" +
                                                   "      \"merges_url\": \"https://api.github.com/repos/daniel-e/tetros/merges\",\n" +
                                                   "      \"archive_url\": \"https://api.github.com/repos/daniel-e/tetros/{archive_format}{/ref}\",\n" +
                                                   "      \"downloads_url\": \"https://api.github.com/repos/daniel-e/tetros/downloads\",\n" +
                                                   "      \"issues_url\": \"https://api.github.com/repos/daniel-e/tetros/issues{/number}\",\n" +
                                                   "      \"pulls_url\": \"https://api.github.com/repos/daniel-e/tetros/pulls{/number}\",\n" +
                                                   "      \"milestones_url\": \"https://api.github.com/repos/daniel-e/tetros/milestones{/number}\",\n" +
                                                   "      \"notifications_url\": \"https://api.github.com/repos/daniel-e/tetros/notifications{?since,all,participating}\",\n" +
                                                   "      \"labels_url\": \"https://api.github.com/repos/daniel-e/tetros/labels{/name}\",\n" +
                                                   "      \"releases_url\": \"https://api.github.com/repos/daniel-e/tetros/releases{/id}\",\n" +
                                                   "      \"deployments_url\": \"https://api.github.com/repos/daniel-e/tetros/deployments\",\n" +
                                                   "      \"created_at\": \"2016-09-22T10:42:55Z\",\n" +
                                                   "      \"updated_at\": \"2018-02-21T00:48:49Z\",\n" +
                                                   "      \"pushed_at\": \"2016-12-18T13:32:27Z\",\n" +
                                                   "      \"git_url\": \"git://github.com/daniel-e/tetros.git\",\n" +
                                                   "      \"ssh_url\": \"git@github.com:daniel-e/tetros.git\",\n" +
                                                   "      \"clone_url\": \"https://github.com/daniel-e/tetros.git\",\n" +
                                                   "      \"svn_url\": \"https://github.com/daniel-e/tetros\",\n" +
                                                   "      \"homepage\": \"\",\n" +
                                                   "      \"size\": 171,\n" +
                                                   "      \"stargazers_count\": 633,\n" +
                                                   "      \"watchers_count\": 633,\n" +
                                                   "      \"language\": \"Assembly\",\n" +
                                                   "      \"has_issues\": true,\n" +
                                                   "      \"has_projects\": true,\n" +
                                                   "      \"has_downloads\": true,\n" +
                                                   "      \"has_wiki\": true,\n" +
                                                   "      \"has_pages\": false,\n" +
                                                   "      \"forks_count\": 32,\n" +
                                                   "      \"mirror_url\": null,\n" +
                                                   "      \"archived\": false,\n" +
                                                   "      \"open_issues_count\": 0,\n" +
                                                   "      \"license\": {\n" +
                                                   "        \"key\": \"mit\",\n" +
                                                   "        \"name\": \"MIT License\",\n" +
                                                   "        \"spdx_id\": \"MIT\",\n" +
                                                   "        \"url\": \"https://api.github.com/licenses/mit\"\n" +
                                                   "      },\n" +
                                                   "      \"forks\": 32,\n" +
                                                   "      \"open_issues\": 0,\n" +
                                                   "      \"watchers\": 633,\n" +
                                                   "      \"default_branch\": \"master\",\n" +
                                                   "      \"score\": 37.632088\n" +
                                                   "    }\n]}";
}