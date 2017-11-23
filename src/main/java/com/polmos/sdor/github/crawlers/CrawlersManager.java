package com.polmos.sdor.github.crawlers;

import com.polmos.sdor.github.rest.BlockingClient;
import com.polmos.sdor.github.rest.Client;
import com.polmos.sdor.graph.GraphSink;
import com.polmos.sdor.graph.tracking.GraphTracker;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by RobicToNieMaKomu on 2017-11-01.
 */
@Component
public class CrawlersManager {
    private final ExecutorService service;
    private final Client githubGithubClient;
    private final GraphTracker tracker;
    private final GraphSink graphSink;

    public CrawlersManager(BlockingClient githubGithubClient,
                           GraphTracker tracker,
                           GraphSink graphSink) {
        this.githubGithubClient = githubGithubClient;
        this.tracker = tracker;
        this.graphSink = graphSink;
        this.service = Executors.newCachedThreadPool();
    }

    @PreDestroy
    public void clean() {
        service.shutdownNow();
        try {
            service.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void launchCrawler() {
        service.execute(newCrawler());
    }

    private Runnable newCrawler() {
        return new Crawler(githubGithubClient, tracker, graphSink);
    }
}
