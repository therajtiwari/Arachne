package com.webcrawler.core;

import com.webcrawler.model.CrawlResult;
import java.util.List;

/**
 * A helper class to return both the result of a crawl and the new links discovered.
 */
public class CrawlOutput {
    private final CrawlResult result;
    private final List<String> newLinks;

    public CrawlOutput(CrawlResult result, List<String> newLinks) {
        this.result = result;
        this.newLinks = newLinks;
    }

    public CrawlResult getResult() {
        return result;
    }

    public List<String> getNewLinks() {
        return newLinks;
    }
}