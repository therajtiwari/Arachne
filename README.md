# Arachne: Concurrent Web Crawler

A multi-threaded web crawler implementation in Java demonstrating advanced concurrency concepts.

## Features

- **Thread Pool Management**: Uses `ExecutorService` with a configurable fixed-size thread pool for efficient task processing.
- **Concurrent Data Structures**: Employs `ConcurrentHashMap` for visited URLs and a `BlockingQueue` for tasks to ensure thread safety and high performance.
- **Configurable Rate Limiting**: A `Semaphore`-based rate limiter prevents overwhelming servers by controlling the number of requests per second.
- **Robust Termination Logic**: A supervisor pattern is used where the main thread monitors the state of the crawl, reliably detecting when all work is complete and initiating a clean shutdown.
- **Graceful Shutdown**: Properly shuts down the `ExecutorService` and other resources, ensuring all pending tasks in the queue are completed.
- **Crawl Boundary Control**: Configurable crawling depth and maximum number of pages to prevent infinite crawling and manage scope.
- **Atomic Operations**: Leverages atomic classes and methods like `AtomicInteger` and `ConcurrentHashMap.putIfAbsent` to eliminate race conditions.

## Key Concurrency Concepts Demonstrated

1.  **ExecutorService**: Manages a pool of worker threads, decoupling task submission from task execution.
2.  **BlockingQueue**: Implements the producer-consumer pattern for distributing crawl tasks between threads in a thread-safe manner.
3.  **ConcurrentHashMap**: Provides thread-safe storage of visited URLs. The use of `putIfAbsent` demonstrates an atomic check-then-act operation to prevent redundant work.
4.  **AtomicInteger**: Safely tracks the number of currently active worker threads and the total number of pages submitted for crawling.
5.  **Volatile Keyword**: Ensures visibility of the `shutdown` flag across all threads, triggering a coordinated shutdown.
6.  **Supervisor Thread Pattern**: The main thread acts as a supervisor, monitoring the state of the `BlockingQueue` and `activeWorkers` count to determine when the crawl is complete and it's time to shut down the system. This is a robust alternative to error-prone termination-detection logic within worker threads.
7.  **Semaphore**: Used within the `RateLimiter` class to control access to the "resource" of making an HTTP request, effectively throttling the crawl speed.