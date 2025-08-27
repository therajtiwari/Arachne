package com.webcrawler.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Simple HTTP client for fetching web pages and extracting links
 */
public class HttpClient {
    private final String userAgent;
    private final int connectionTimeout;
    private final int readTimeout;

    private static final Pattern LINK_PATTERN = Pattern.compile(
            "<a\\s+(?:[^>]*?\\s+)?href\\s*=\\s*[\"']([^\"']*)[\"']",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern TITLE_PATTERN = Pattern.compile(
            "<title>\\s*(.*?)\\s*</title>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    public HttpClient(String userAgent, int connectionTimeout, int readTimeout) {
        this.userAgent = userAgent;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }

    public HttpResponse fetchPage(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", userAgent);
            connection.setConnectTimeout(connectionTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setInstanceFollowRedirects(true);

            int statusCode = connection.getResponseCode();
            String content = "";

            if (statusCode >= 200 && statusCode < 300) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    content = reader.lines().collect(Collectors.joining("\n"));
                }
            }
            return new HttpResponse(statusCode, content);
        } finally {
            connection.disconnect();
        }
    }

    public String extractTitle(String html) {
        Matcher matcher = TITLE_PATTERN.matcher(html);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "No Title Found";
    }

    public List<String> extractLinks(String html, String baseUrl) {
        List<String> links = new ArrayList<>();
        Matcher matcher = LINK_PATTERN.matcher(html);

        while (matcher.find()) {
            String link = matcher.group(1);
            resolveUrl(link, baseUrl).ifPresent(links::add);
        }
        return links;
    }

    private java.util.Optional<String> resolveUrl(String link, String baseUrl) {
        try {
            link = link.trim();
            if (link.isEmpty() || link.startsWith("#") || link.startsWith("mailto:") || link.startsWith("javascript:")) {
                return java.util.Optional.empty();
            }

            URL base = new URL(baseUrl);
            URL resolved = new URL(base, link);
            String urlString = resolved.toString();

            // Remove fragment identifier
            int fragmentIndex = urlString.indexOf('#');
            if (fragmentIndex != -1) {
                urlString = urlString.substring(0, fragmentIndex);
            }

            if (urlString.startsWith("http://") || urlString.startsWith("https://")) {
                return java.util.Optional.of(urlString);
            }
            return java.util.Optional.empty();
        } catch (MalformedURLException e) {
            return java.util.Optional.empty();
        }
    }

    public static class HttpResponse {
        private final int statusCode;
        private final String content;

        public HttpResponse(int statusCode, String content) {
            this.statusCode = statusCode;
            this.content = content;
        }

        public int getStatusCode() { return statusCode; }
        public String getContent() { return content; }
        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }
    }
}