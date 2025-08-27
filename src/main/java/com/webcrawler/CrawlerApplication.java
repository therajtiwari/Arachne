package com.webcrawler;

import com.webcrawler.config.CrawlerConfig;
import com.webcrawler.core.WebCrawler;
import com.webcrawler.model.CrawlResult;

import java.util.List;
import java.util.Scanner;

/**
 * Main application class
 */
public class CrawlerApplication {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Concurrent Web Crawler ===");
        System.out.println("Enter starting URL (or press Enter for default 'https://toscrape.com/'): ");
        String startUrl = scanner.nextLine().trim();

        if (startUrl.isEmpty()) {
            startUrl = "https://toscrape.com/";
        }

        // Build configuration
        CrawlerConfig config = new CrawlerConfig.Builder()
                .maxThreads(50)
                .maxDepth(10)
                .maxPages(400)
                .connectionTimeout(5000)
                .readTimeout(10000)
                .permitsPerSecond(50) // Allow 5 requests per second
                .userAgent("ConcurrentWebCrawler/1.0 (Arachne)")
                .build();

        System.out.println("\nConfiguration:");
        System.out.println("Max Threads: " + config.getMaxThreads());
        System.out.println("Max Depth: " + config.getMaxDepth());
        System.out.println("Max Pages: " + config.getMaxPages());
        System.out.println("Requests per second: " + config.getPermitsPerSecond());
        System.out.println("Starting URL: " + startUrl);
        System.out.println("\nStarting crawl...\n");

        WebCrawler crawler = new WebCrawler(config);
        long startTime = System.currentTimeMillis();

        crawler.crawl(startUrl);

        long totalTime = System.currentTimeMillis() - startTime;
        List<CrawlResult> results = crawler.getResults();

        // Display results
        System.out.println("\n=== Crawl Results ===");
        System.out.println("Total time: " + totalTime + "ms");
        System.out.println("Pages crawled: " + results.size());
        if (!results.isEmpty()) {
            System.out.println("Average time per page: " + (totalTime / results.size()) + "ms");
        }

//        System.out.println("\nDetailed Results:");
//        results.stream().forEach(System.out::println);


        scanner.close();
    }
}