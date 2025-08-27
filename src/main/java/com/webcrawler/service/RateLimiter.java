package com.webcrawler.service;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Simple rate limiter using a Semaphore and a refill thread.
 * This ensures that permits are added at a fixed rate.
 */
public class RateLimiter {
    private final Semaphore semaphore;
    private final int maxPermits;
    private final long intervalNanos;
    private final Thread refillThread;
    private volatile boolean shutdown = false;

    public RateLimiter(int permitsPerSecond) {
        if (permitsPerSecond <= 0) {
            throw new IllegalArgumentException("Permits per second must be positive.");
        }
        this.maxPermits = permitsPerSecond;
        this.semaphore = new Semaphore(maxPermits);
        this.intervalNanos = TimeUnit.SECONDS.toNanos(1) / permitsPerSecond;

        this.refillThread = new Thread(this::refillTokens);
        this.refillThread.setDaemon(true);
        this.refillThread.start();
    }

    public void acquire() throws InterruptedException {
        semaphore.acquire();
    }

    public void shutdown() {
        shutdown = true;
        refillThread.interrupt();
    }

    private void refillTokens() {
        long nextRefill = System.nanoTime();
        while (!shutdown) {
            try {
                long now = System.nanoTime();
                if (now < nextRefill) {
                    TimeUnit.NANOSECONDS.sleep(nextRefill - now);
                }

                if (semaphore.availablePermits() < maxPermits) {
                    semaphore.release();
                }

                nextRefill += intervalNanos;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}