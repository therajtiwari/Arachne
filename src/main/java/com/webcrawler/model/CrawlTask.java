package com.webcrawler.model;

/**
 * Represents a crawling task with URL and depth information
 */
public class CrawlTask {
    private final String url;
    private final int depth;

    public CrawlTask(String url, int depth) {
        this.url = url;
        this.depth = depth;
    }

    public String getUrl() {
        return url;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public String toString() {
        return String.format("CrawlTask{url='%s', depth=%d}", url, depth);
    }
}