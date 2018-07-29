package server.api.verticle;

import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.List;

/**
 * API to a client that can query the GitHub repository search API.
 *
 * @author Nathan
 * Created : 21/02/2018
 */
@FunctionalInterface
public interface GitHubServiceClient
{
  List<JsonObject> searchRepositories(String language) throws IOException;
}
