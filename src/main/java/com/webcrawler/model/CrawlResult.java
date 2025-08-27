package com.webcrawler.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents the result of crawling a single URL
 */
public class CrawlResult {
    private final String url;
    private final int statusCode;
    private final String title;
    private final List<String> extractedLinks;
    private final long crawlTimeMs;
    private final int depth;

    public CrawlResult(String url, int statusCode, String title,
                       List<String> extractedLinks, long crawlTimeMs, int depth) {
        this.url = url;
        this.statusCode = statusCode;
        this.title = title;
        this.extractedLinks = extractedLinks;
        this.crawlTimeMs = crawlTimeMs;
        this.depth = depth;
    }

    // Getters
    public String getUrl() { return url; }
    public int getStatusCode() { return statusCode; }
    public String getTitle() { return title; }
    public List<String> getExtractedLinks() { return extractedLinks; }
    public long getCrawlTimeMs() { return crawlTimeMs; }
    public int getDepth() { return depth; }

    @Override
    public String toString() {
        return String.format("CrawlResult{url='%s', status=%d, title='%s', links=%d, time=%dms, depth=%d}",
                url, statusCode, title, extractedLinks.size(), crawlTimeMs, depth);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrawlResult that = (CrawlResult) o;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}