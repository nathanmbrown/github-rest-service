package server.api.verticle;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of the GitHubServiceClient interface that uses core java to access the repository search API.
 *
 * @author Nathan
 * Created : 21/02/2018
 */
public class GitHubServiceClientImpl implements GitHubServiceClient
{
  final static Logger LOGGER = LoggerFactory.getLogger(GitHubServiceClientImpl.class);

  @Override
  public List<JsonObject> searchRepositories(String language) throws IOException
  {
    // todo : circuit breaker, caching
    List<JsonObject> result = new ArrayList<>();
    String uri = getRepositorySearchURI(language);
    int pageCount = 0;
    // we need to limit to a maximum of 10 pages, as there is a rate limit set for the search API of 10 requests per minute
    while (uri != null && pageCount < 10) {
      Pair<JsonObject, String> pageResultsAndNextPage = queryRepositorySearchAPI(uri);
      result.add(pageResultsAndNextPage.getLeft());
      uri = pageResultsAndNextPage.getRight();
      pageCount++;
    }
    return result;
  }

  @NotNull
  protected String getRepositorySearchURI(String language)
  {
    return "https://api.github.com/search/repositories?per_page=100&q=language:" + language;
  }

  @NotNull
  private Pair<JsonObject, String> queryRepositorySearchAPI(String uri) throws IOException
  {
    LOGGER.info("Querying GitHub API {0}", uri);
    URL url = new URL(uri);
    HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
    try {
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Accept", "application/json");

      if (conn.getResponseCode() != 200) {
        String errorStreamContent = conn.getErrorStream() != null ? IOUtils.toString(conn.getErrorStream()) : "";
        LOGGER.error("Query of GitHub Page URI {0} failed with HTTP error code {1}, error stream content : {2}",
                     uri,
                     conn.getResponseCode(),
                     errorStreamContent);
        throw new IOException("Failed : HTTP error code : " + conn.getResponseCode());
      }
      String json = IOUtils.toString(conn.getInputStream());
      String nextPageURI = extractNextPageURIFromLinks(conn.getHeaderField("Link"));
      return Pair.of(new JsonObject(json), nextPageURI);
    } finally {
      conn.disconnect();
    }
  }

  private static final Pattern NEXT_PAGE_PATTERN = Pattern.compile(".*<(.*)>;\\s*rel=\"next\".*");

  protected static String extractNextPageURIFromLinks(String linksContent)
  {
    if (linksContent != null) {
      Matcher matcher = NEXT_PAGE_PATTERN.matcher(linksContent);
      if (matcher.matches() && matcher.groupCount() > 0) {
        return matcher.group(1);
      }
    }
    return null;
  }
}
