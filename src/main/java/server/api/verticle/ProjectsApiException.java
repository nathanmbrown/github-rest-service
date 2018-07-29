package server.api.verticle;

import server.api.MainApiException;

/**
 * Exception thrown by the ProjectsAPI if it encounters a problem
 */
public final class ProjectsApiException extends MainApiException {
    public ProjectsApiException(int statusCode, String statusMessage) {
        super(statusCode, statusMessage);

    }

  public static final ProjectsApiException INVALID_LANGUAGE_EXCEPTION = new ProjectsApiException(400, "Invalid language value");


}