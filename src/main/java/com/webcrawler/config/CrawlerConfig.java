package com.webcrawler.config;

/**
 * Configuration class for the web crawler
 */
public class CrawlerConfig {
    private final int maxThreads;
    private final int maxDepth;
    private final int maxPages;
    private final int connectionTimeoutMs;
    private final int readTimeoutMs;
    private final int permitsPerSecond;
    private final String userAgent;

    private CrawlerConfig(Builder builder) {
        this.maxThreads = builder.maxThreads;
        this.maxDepth = builder.maxDepth;
        this.maxPages = builder.maxPages;
        this.connectionTimeoutMs = builder.connectionTimeoutMs;
        this.readTimeoutMs = builder.readTimeoutMs;
        this.permitsPerSecond = builder.permitsPerSecond;
        this.userAgent = builder.userAgent;
    }

    // Getters
    public int getMaxThreads() { return maxThreads; }
    public int getMaxDepth() { return maxDepth; }
    public int getMaxPages() { return maxPages; }
    public int getConnectionTimeoutMs() { return connectionTimeoutMs; }
    public int getReadTimeoutMs() { return readTimeoutMs; }
    public int getPermitsPerSecond() { return permitsPerSecond; }
    public String getUserAgent() { return userAgent; }

    public static class Builder {
        private int maxThreads = 10;
        private int maxDepth = 3;
        private int maxPages = 100;
        private int connectionTimeoutMs = 5000;
        private int readTimeoutMs = 10000;
        private int permitsPerSecond = 2; // Default to 2 requests per second
        private String userAgent = "ConcurrentWebCrawler/1.0";

        public Builder maxThreads(int maxThreads) {
            this.maxThreads = maxThreads > 0 ? maxThreads : 1;
            return this;
        }

        public Builder maxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }

        public Builder maxPages(int maxPages) {
            this.maxPages = maxPages > 0 ? maxPages : 1;
            return this;
        }

        public Builder connectionTimeout(int timeoutMs) {
            this.connectionTimeoutMs = timeoutMs;
            return this;
        }

        public Builder readTimeout(int timeoutMs) {
            this.readTimeoutMs = timeoutMs;
            return this;
        }

        public Builder permitsPerSecond(int permits) {
            if (permits <= 0) {
                throw new IllegalArgumentException("Permits per second must be positive.");
            }
            this.permitsPerSecond = permits;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public CrawlerConfig build() {
            return new CrawlerConfig(this);
        }
    }
}