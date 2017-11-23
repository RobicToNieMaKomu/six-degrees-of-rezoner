package com.polmos.sdor.github.crawlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.polmos.sdor.github.rest.Client;
import com.polmos.sdor.github.rest.GithubVertex;
import com.polmos.sdor.graph.GraphSink;
import com.polmos.sdor.graph.tracking.GraphTracker;
import com.polmos.sdor.graph.tracking.ProcessedNode;
import com.polmos.sdor.graph.tracking.State;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Created by RobicToNieMaKomu on 2017-11-09.
 */
public class Crawler implements Runnable {
    private static final Logger log = Logger.getLogger("crawler");

    private final Client githubGithubClient;
    private final GraphTracker tracker;
    private final GraphSink sink;

    public Crawler(Client githubGithubClient,
                   GraphTracker tracker,
                   GraphSink sink) {
        this.githubGithubClient = githubGithubClient;
        this.tracker = tracker;
        this.sink = sink;
    }

    @Override
    public void run() {
        log.info("starting crawler...");
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Optional<ProcessedNode> next = tracker.next(State.CURR);
                processIfPresent(next);
            }
        } catch (RuntimeException e) {
            log.info("unexpected shit happened: " + e.toString());
        } catch (InterruptedException e) {
            log.info("crawler has been interrupted");
        }
        log.info("shutting down crawler...");
    }

    private void processIfPresent(Optional<ProcessedNode> next) throws InterruptedException {
        if (next.isPresent()) {
            ProcessedNode processedNode = next.get();
            List<JsonNode> followers = transform(
                    processedNode.getLogin(),
                    githubGithubClient.getFollowers(processedNode.getLogin())
            );
            GithubVertex vertex = toVertex(processedNode, followers);
            pushToGraphDb(processedNode, followers, vertex);
        }
    }

    private void pushToGraphDb(ProcessedNode processedNode, List<JsonNode> followers, GithubVertex vertex) {
        sink.push(vertex);
        tracker.push(followers, State.CURR);
        tracker.push(processedNode.getNode(), State.PREV);
        processedNode.acknowledgeProcessing();
    }

    private List<JsonNode> transform(String parent, ArrayNode followers) {
        return IntStream
                .range(0, followers.size())
                .mapToObj(followers::get)
                .map(j -> extractInterestingFields(j, parent))
                .collect(toList());
    }

    private ObjectNode extractInterestingFields(JsonNode follower, String parent) {
        ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
        node.put("id", follower.get("id").asLong());
        node.put("login", follower.get("login").asText());
        node.put("parent", parent);
        return node;
    }

    private GithubVertex toVertex(ProcessedNode node, List<JsonNode> followers) {
        Set<String> f = followers.stream()
                .map(follower -> follower.get("login").asText())
                .collect(toSet());
        return new GithubVertex(node.getId(), node.getLogin(), node.getParent(), f);
    }
}