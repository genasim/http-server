# HTTP Server

This project implements a simple HTTP 1.0 server in Java.

## Table of Contents
- [Architecture](#architecture)
- [Features](#features)
- [Routing](#routing)
- [Error Handling](#error-handling)
- [Testing](#testing)

## Architecture

The server utilizes a synchronous API with a hybrid threading model to efficiently handle client requests. 
- **Virtual Threads for I/O:** Each client connection is handled in its own virtual thread (`VirtualThreadPerTaskExecutor`), which is ideal for I/O-bound tasks like reading requests and writing responses. This allows the server to handle a large number of concurrent connections with minimal overhead.
- **Work-Stealing Pool for Request Building:** A work-stealing thread pool (`Executors.newWorkStealingPool()`) is used for the initial processing of requests. This ensures that CPU-intensive tasks, such as parsing request lines and headers, are distributed efficiently across available processor cores.

## Features

- **HTTP 1.0 Support:** The server understands and processes basic HTTP 1.0 requests.
- **Express.js-like Routing API:** The server provides a simple and intuitive API for defining routes, inspired by the popular Node.js framework, Express.
- **JSON Body Parsing:** The server can parse and validate JSON request bodies.
- **Basic Request Validation:** The server performs initial checks on incoming requests to ensure they are well-formed.

## Routing

Routing is handled by a custom trie-based router (`TrieRouter`). This data structure provides efficient matching of request paths to their corresponding handlers.

- **Path Parameters:** The router supports dynamic path segments (e.g., `/users/:id`), which are extracted and made available to the request handler.
- **HTTP Verb Matching:** Routes are associated with specific HTTP verbs (GET, POST, PUT, DELETE), allowing for different handlers for the same path but different methods.

## Error Handling

The server includes basic error handling and responds with appropriate HTTP status codes:

- **400 Bad Request:** Sent for malformed requests, such as invalid request lines or incorrect JSON syntax in the body.
- **404 Not Found:** Sent when no handler is found for the requested path.
- **405 Method Not Allowed:** Sent when a request is made to a valid path but with an unsupported HTTP method.
- **500 Internal Server Error:** Sent when an unexpected exception occurs during request processing.

## Testing

The project includes a suite of unit tests written using **JUnit 6** and **Mockito**. These tests cover the core components of the server, including the `HttpServer`, `ClientRequestHandler`, and `TrieRouter`, ensuring their correctness and reliability.
