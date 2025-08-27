package com.webcrawler.core;

import com.webcrawler.config.CrawlerConfig;
import com.webcrawler.model.CrawlResult;
import com.webcrawler.model.CrawlTask;
import com.webcrawler.service.HttpClient;
import com.webcrawler.service.RateLimiter;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main web crawler class that coordinates the crawling process
 */
public class WebCrawler {
    private final CrawlerConfig config;
    private final HttpClient httpClient;
    private final RateLimiter rateLimiter;
    private final ExecutorService executorService;
    private final BlockingQueue<CrawlTask> taskQueue;
    private final ConcurrentHashMap<String, Boolean> visitedUrls;
    private final ConcurrentLinkedQueue<CrawlResult> results;
    private final AtomicInteger activeWorkers;
    private final AtomicInteger pagesSubmittedCount;
    private volatile boolean shutdown = false;

    public WebCrawler(CrawlerConfig config) {
        this.config = config;
        this.httpClient = new HttpClient(
                config.getUserAgent(),
                config.getConnectionTimeoutMs(),
                config.getReadTimeoutMs()
        );
        this.rateLimiter = new RateLimiter(config.getPermitsPerSecond());
        this.executorService = Executors.newFixedThreadPool(config.getMaxThreads());
        this.taskQueue = new LinkedBlockingQueue<>();
        this.visitedUrls = new ConcurrentHashMap<>();
        this.results = new ConcurrentLinkedQueue<>();
        this.activeWorkers = new AtomicInteger(0);
        this.pagesSubmittedCount = new AtomicInteger(0);
    }

    public void crawl(String startUrl) {
        System.out.println("Starting crawl from: " + startUrl);

        addTask(new CrawlTask(startUrl, 0));

        for (int i = 0; i < config.getMaxThreads(); i++) {
            executorService.submit(new CrawlWorker());
        }

        // Supervisor loop to monitor progress and determine when to shut down
        monitorCrawl();

        shutdown();
        System.out.println("Crawl completed. Total pages processed: " + results.size());
    }

    private void monitorCrawl() {
        while (!shutdown) {
            try {
                Thread.sleep(1000);
                // The crawl is finished if the queue is empty and no workers are processing a page.
                if (taskQueue.isEmpty() && activeWorkers.get() == 0) {
                    // Double-check to prevent a race condition where a worker finishes
                    // after the first check but before the second, having added new tasks.
                    if (taskQueue.isEmpty() && activeWorkers.get() == 0) {
                        System.out.println("Crawl finished: No more tasks and no active workers.");
                        shutdown = true;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                shutdown = true;
            }
        }
    }


    private void addTask(CrawlTask task) {
        if (shutdown || task.getDepth() > config.getMaxDepth()) {
            return;
        }

        // Atomically check if URL is new. If it is, check against page limit.
        if (visitedUrls.putIfAbsent(task.getUrl(), true) == null) {
            if (pagesSubmittedCount.incrementAndGet() <= config.getMaxPages()) {
                taskQueue.offer(task);
            } else {
                // We've hit the page limit; stop adding tasks.
                // We can also trigger a shutdown here if desired for a hard stop.
                // shutdown = true;
            }
        }
    }

    private void shutdown() {
        rateLimiter.shutdown();
        executorService.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow(); // Cancel currently executing tasks
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public List<CrawlResult> getResults() {
        return List.copyOf(results);
    }

    private class CrawlWorker implements Runnable {
        @Override
        public void run() {
            try {
                // Loop while not shut down OR while there are still tasks to process
                while (!shutdown || !taskQueue.isEmpty()) {
                    CrawlTask task = taskQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (task == null) {
                        // If poll times out, just continue the loop.
                        // The loop condition will handle termination.
                        continue;
                    }

                    activeWorkers.incrementAndGet();
                    try {
                        processCrawlTask(task);
                    } finally {
                        activeWorkers.decrementAndGet();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Worker thread interrupted.");
            }
        }

        private void processCrawlTask(CrawlTask task) {
            try {
                rateLimiter.acquire();

                System.out.println("Crawling: " + task.getUrl() + " (depth: " + task.getDepth() + ")");

                long startTime = System.currentTimeMillis();
                HttpClient.HttpResponse response = httpClient.fetchPage(task.getUrl());
                long crawlTime = System.currentTimeMillis() - startTime;

                if (response.isSuccess()) {
                    String title = httpClient.extractTitle(response.getContent());
                    List<String> extractedLinks = httpClient.extractLinks(response.getContent(), task.getUrl());

                    CrawlResult result = new CrawlResult(
                            task.getUrl(), response.getStatusCode(), title,
                            extractedLinks, crawlTime, task.getDepth()
                    );
                    results.add(result);

                    // Add new tasks for extracted links
                    for (String link : extractedLinks) {
                        addTask(new CrawlTask(link, task.getDepth() + 1));
                    }
                } else {
                    System.out.println("Failed to crawl " + task.getUrl() + " - Status: " + response.getStatusCode());
                }

            } catch (Exception e) {
                System.err.println("Error crawling " + task.getUrl() + ": " + e.getMessage());
            }
        }
    }
}