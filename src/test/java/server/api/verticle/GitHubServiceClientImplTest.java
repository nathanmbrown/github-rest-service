package server.api.verticle;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GitHubServiceClientImplTest
{
  @Test
  public void testExtractNextPageURIFromLinks() throws Exception
  {
    String uri = GitHubServiceClientImpl.extractNextPageURIFromLinks(
      "<https://api.github.com/resource?page=1>; rel=\"prev\", <https://api.github.com/resource?page=2>; rel=\"next\", <https://api.github.com/resource?page=5>; rel=\"last\"");
    assertThat(uri, is("https://api.github.com/resource?page=2"));
  }
}