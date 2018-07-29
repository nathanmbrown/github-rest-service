package server.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Value;

@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectInfo
{
  public final Long id;
  public final String name;
  public final String url;
  public final String owner;

  @JsonCreator
  public ProjectInfo(Long id, String name, String url, String owner)
  {
    this.id = id;
    this.name = name;
    this.url = url;
    this.owner = owner;
  }
}