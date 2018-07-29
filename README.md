## GitHub Projects REST Service

######Author : Nathan Brown

### Overview

This REST service has been implemented using Vert.x. As a reactive event driven framework it is well suited to implementing such gateway API's, making it easy to write non-blocking service endpoints.

The API definition was created in Swagger, and then the Swagger Codegen tool used to create the REST service Verticle - The API definition file created can be found in the `/src/main/resources` directory of the project. The service implementation and GitHub Service client was then created using core Java to call the GitHub service, and Vert.x's JSON support to parse the response.

Lombok was used to de-boilerplate the ProjectInfo service return object. 

**Implementation Note :** The GitHub API results are paged with a maximum of 100 items per page. The service will enumerate up to 10 pages maximum in order to comply with the API rate limiting restrictions of 10 requests per minute, and so returning a maximum of 1000 projects. This means for many languages it is not possible to satisfy the original requirement of returning *all* the projects for a given language.  

###Testing

Unit tests have been implemented for both the service implementation and the GitHub client.

###Usage

In a development environment with Maven installed, the service can be run up from command line using `mvn compile exec:java` in the project directory.

If the service needs to be run standalone, a 'fat' jar containing everything needed to run the service can be generated using `mvn package` in the project directory. The fat jar can then be found in the project target directory (`github-rest-service-1.0.0-SNAPSHOT-fat.jar`) and can be run anywhere using the command line:
```
java -jar github-rest-service-1.0.0-SNAPSHOT-fat.jar
```

###API
#####Request
The server is bound to port 8080 and the service supports a single end point of the form: 

`GET /projects/findByLang?lang={language_name}`

`language_name` is required and must be the same as one defined in GitHub e.g. rust, go, coffeescript etc.

An example request URL would be:
`http://localhost:8080/api/projects/findByLang?lang=java`

This can be invoked from the command line using
``` 
curl -X GET "http://localhost:8080/api/projects/findByLang?lang=java" -H "accept: application/json"
```
Or directly from your development tool; a test .http file can be found in `/src/main/resources/Manual REST Service Test.http`

#####Response

The response takes the form of a JSON array of ProjectInfo structures, which have the following schema
```
ProjectInfo {
  id	integer($int64)
  name	string
  url	string
  owner	string
}
```
An example response would be
```
 [ 
   {
     "id" : 21663285,
     "name" : "chapel",
     "url" : "https://github.com/chapel-lang/chapel",
     "owner" : "chapel-lang"
   }, 
   {
     "id" : 53186620,
     "name" : "chord",       
     "url" : "https://github.com/briangu/chord",
     "owner" : "briangu"
   }
 ]
```

######Errors
An invalid or missing `lang` parameter will return a 400 error.

####To Dos
There are a number of things that need to be done to improve this service implementation before it is ready for production usage, including but not limited to:
* Support for customization via command line arguments e.g. port number
* Potential implementation of caching to improve response time
* Use of a Circuit Breaker to gracefully deal with GitHub service issues
* Proper integration testing of a deployed service end to end
* Use of Dependency Injection to ease testing and modularity