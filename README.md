# miniWeb

A lightweight, annotation-driven HTTP framework written in pure Java.

miniWeb uses only the JDK (no external dependencies) to provide a small
Spring Boot style controller model. It is built on top of the built-in
`com.sun.net.httpserver.HttpServer` and uses reflection to discover, register,
and dispatch routes.

**Status: work in progress.** The framework is functional but still early.
GET routes with path parameters work end to end. JSON serialization, request
body support, and the full set of HTTP verbs are not implemented yet.

## Features

- Annotation-based routing using `@API`, `@GET`, `@POST`, `@PUT`, `@DELETE`, and `@PathParam`
- Classpath scanning that auto-discovers annotated controllers at startup
- Path variable extraction with automatic type conversion (`String`, `int`, `long`, `UUID`)
- Fail-fast validation that rejects duplicate API paths and conflicting HTTP annotations
- No dependencies beyond the standard JDK

## How it works

1. `Main` creates a `Loader` and calls `start()`.
2. The loader scans the package of the main class for `.class` files.
3. Classes annotated with `@API` are instantiated and their HTTP annotated
   methods are registered in a route map keyed by request method.
4. `SimpleServer` starts an `HttpServer` on port `8080`.
5. For each request the server picks the route list for the HTTP verb, matches
   the path against registered routes, binds `@PathParam` values into the
   handler arguments, invokes the method, and writes the returned `String` to
   the response.

## Annotations

| Annotation  | Target     | Purpose                                            |
|-------------|------------|----------------------------------------------------|
| `@API`      | Type       | Marks a class as a controller with a base path     |
| `@GET`      | Method     | Maps a handler to a GET route                      |
| `@POST`     | Method     | Maps a handler to a POST route                     |
| `@PUT`      | Method     | Maps a handler to a PUT route                      |
| `@DELETE`   | Method     | Maps a handler to a DELETE route                   |
| `@PathParam`| Parameter  | Binds a path variable to a handler parameter       |

The HTTP method annotations accept an optional path. When omitted, the route
is the `@API` value alone. Path variables are declared inline with curly
braces, for example `/user/new-path/{id}`.

## Example

```java
@API("/user")
public class UserService {

    @GET
    public String getName() {
        return "Swogyan Bhattarai";
    }

    @GET("/new-path/{id}")
    public String getById(@PathParam("id") int id) {
        return "This is " + id;
    }
}
```

A request to `GET /user/new-path/5` matches the second handler and returns
`This is 5`.

## Getting started

Requirements:

- JDK 25
- Maven

Build and run from the IDE or with Maven:

```
mvn compile
```

The server starts on `http://localhost:8080`. The example controllers are in
`UserService` and `TestService`.

## Project layout

```
src/main/java/org/example/justdeepfried/
├── annotations/        Custom annotations (API, GET, POST, PUT, DELETE, PathParam)
├── dto/                Shared records
├── exception/          Custom exception types
├── service/            Example controllers
├── Loader.java         Classpath scanning and route registration
├── SimpleServer.java   HTTP server, path matching, and dispatch
└── Main.java           Entry point
```

## Current limitations

- Handlers must return a `String`; there is no JSON support
- Request bodies, query parameters, and headers are not handled
- Unmatched routes return a generic 500 response instead of a 404
- Content types are not set on responses
- No tests yet

## Roadmap

- Response serialization to JSON
- Request body binding and content type handling
- Proper 404 responses
- Unit and integration tests
- Optional route ordering and a dedicated path matcher
"# miniWeb" 
