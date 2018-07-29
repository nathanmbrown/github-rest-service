package server.api.verticle;

import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import server.api.MainApiException;

public class ProjectsApiVerticle extends AbstractVerticle {
    final static Logger LOGGER = LoggerFactory.getLogger(ProjectsApiVerticle.class); 
    
    final static String FINDPROJECTSBYLANGUAGE_SERVICE_ID = "findProjectsByLanguage";
    
    ProjectsApi service;

    public ProjectsApiVerticle() {
    }

  @Override
  public void init(Vertx vertx, Context context)
  {
    super.init(vertx, context);
    try {
      service = new ProjectsApiImpl(vertx);
    } catch (Exception e) {
      logUnexpectedError("ProjectsApiVerticle constructor", e);
      throw new RuntimeException(e);
    }
  }

  @Override
    public void start() throws Exception {
        
        //Consumer for findProjectsByLanguage
        vertx.eventBus().<JsonObject> consumer(FINDPROJECTSBYLANGUAGE_SERVICE_ID).handler(message -> {
            try {
                String lang = message.body().getString("lang");
                service.findProjectsByLanguage(lang, result -> {
                    if (result.succeeded())
                        message.reply(new JsonArray(Json.encode(result.result())).encodePrettily());
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "findProjectsByLanguage");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("findProjectsByLanguage", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
      LOGGER.info("Projects API installed on path /api/projects/findByLang");
    }
    
    private void manageError(Message<JsonObject> message, Throwable cause, String serviceName) {
        int code = MainApiException.INTERNAL_SERVER_ERROR.getStatusCode();
        String statusMessage = MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage();
        if (cause instanceof MainApiException) {
            code = ((MainApiException)cause).getStatusCode();
            statusMessage = ((MainApiException)cause).getStatusMessage();
        } else {
            logUnexpectedError(serviceName, cause); 
        }
            
        message.fail(code, statusMessage);
    }
    
    private void logUnexpectedError(String serviceName, Throwable cause) {
        LOGGER.error("Unexpected error in "+ serviceName, cause);
    }
}
