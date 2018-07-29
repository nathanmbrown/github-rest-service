package server.api.verticle;

import server.api.model.ProjectInfo;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;

/**
 * The interface defining the Projects service API.
 */
public interface ProjectsApi  {
    //findProjectsByLanguage
    void findProjectsByLanguage(String lang, Handler<AsyncResult<List<ProjectInfo>>> handler);
    
}
