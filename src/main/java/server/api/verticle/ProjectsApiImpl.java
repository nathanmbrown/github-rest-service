package server.api.verticle;

import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import server.api.model.ProjectInfo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of the ProjectsApi service interface.
 *
 * @author Nathan
 * Created : 21/02/2018
 */
public class ProjectsApiImpl implements ProjectsApi
{
  final static Logger LOGGER = LoggerFactory.getLogger(ProjectsApiImpl.class);
  private final Vertx vertx;

  private GitHubServiceClient _gitHubServiceClient = new GitHubServiceClientImpl();

  public ProjectsApiImpl(Vertx vertx)
  {
    this.vertx = vertx;
  }

  @Override
  public void findProjectsByLanguage(String lang, Handler<AsyncResult<List<ProjectInfo>>> handler)
  {
    LOGGER.info("Handling request for projects for language {0}...", lang);
    /*
     We need to call executeBlocking here as this is a long blocking operation, and we want to release the calling
     event loop thread to be able to be reused to service other requests. The handler will be called back in the calling
     event loop thread however.
    */
    vertx.executeBlocking((Future<List<ProjectInfo>> future) -> findProjectsByLanguage(lang, future), handler);
  }

  public void findProjectsByLanguage(String lang, Future<List<ProjectInfo>> future)
  {
    if (StringUtils.isEmpty(lang)) {
      future.fail(ProjectsApiException.INVALID_LANGUAGE_EXCEPTION);
      return;
    }
    List<JsonObject> pageDatas;
    try {
      pageDatas = _gitHubServiceClient.searchRepositories(lang);
    } catch (Exception e) {
      LOGGER.error("Error invoking GitHub API : {0}", e, e.getMessage());
      future.fail(new ProjectsApiException(500, "Error invoking GitHub API"));
      return;
    }
    try {
      List<ProjectInfo> result = createAllProjectInfos(pageDatas);
      future.complete(result);
    } catch (Exception e) {
      LOGGER.error("Error parsing GitHub API Response : {0}", e, e.getMessage());
      future.fail(new ProjectsApiException(500, "Error parsing GitHub API Response"));
    }
  }

  protected static List<ProjectInfo> createAllProjectInfos(List<JsonObject> pageDatas)
  {
    return pageDatas.stream()
                    .flatMap(ProjectsApiImpl::createProjectInfos)
                    .collect(Collectors.toList());
  }

  protected static Stream<ProjectInfo> createProjectInfos(JsonObject pageData)
  {
    JsonArray items = requireNonNull(pageData.getJsonArray("items"), "items");
    return items.stream()
                .map(JsonObject.class::cast)
                .map(ProjectsApiImpl::createProjectInfo);
  }

  protected static ProjectInfo createProjectInfo(JsonObject projectEntry)
  {
    Long id = requireNonNull(projectEntry.getLong("id"), "id");
    String name = requireNonNull(projectEntry.getString("name"), "name");
    String html_url = requireNonNull(projectEntry.getString("html_url"), "html_url");
    JsonObject owner = requireNonNull(projectEntry.getJsonObject("owner"), "owner");
    String login = requireNonNull(owner.getString("login"), "login");
    return new ProjectInfo(id, name, html_url, login);
  }

  private static <T> T requireNonNull(T value, String key)
  {
    if (value == null) {
      throw new JSONProcessingException("No Value for key " + key);
    }
    return value;
  }

  /**
   * For testing : should use Dependency Injection so we can mock easier and not need this.
   */
  public void setGitHubServiceClient(GitHubServiceClient gitHubServiceClient)
  {
    _gitHubServiceClient = gitHubServiceClient;
  }

  private static class JSONProcessingException extends RuntimeException
  {
    public JSONProcessingException(String message)
    {
      super(message);
    }
  }
}
