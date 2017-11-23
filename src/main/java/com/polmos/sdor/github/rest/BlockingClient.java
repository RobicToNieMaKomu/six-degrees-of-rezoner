package com.polmos.sdor.github.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by RobicToNieMaKomu on 2017-11-09.
 */
@Service
public class BlockingClient implements Client {
    private final GithubClient githubClient;
    private final AtomicLong remainingHits;
    private final AtomicReference<LocalDateTime> resetTime;

    public BlockingClient(GithubClient githubClient) {
        this.githubClient = githubClient;
        this.remainingHits = new AtomicLong(githubClient.getRemainingCalls());
        this.resetTime = new AtomicReference<>(githubClient.getResetTime());
    }

    @Override
    public ArrayNode getFollowers(String user) throws InterruptedException {
        if (remainingHits.getAndDecrement() <= 0) {
            waitUntilResetTime();
            getFollowers(user);
        }
        return githubClient.getFollowers(user);
    }

    private void waitUntilResetTime() throws InterruptedException {
        LocalDateTime reset = resetTime.updateAndGet(prev -> githubClient.getResetTime());
        Thread.sleep(toMillis(reset) - toMillis(LocalDateTime.now()));
    }

    private long toMillis(LocalDateTime ldt) {
        return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}