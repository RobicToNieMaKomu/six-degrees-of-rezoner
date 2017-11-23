package com.polmos.sdor.graph.tracking;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by RobicToNieMaKomu on 2017-11-01.
 */
@Service
public class GraphTracker {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final ConcurrentMap<String, String> nameToUrl;
    private final AmazonSQS sqs;
    private final int waitTimeSeconds;

    public GraphTracker(AmazonSQS sqs,
                        @Value("${tracker.waitTimeSeconds}") int waitTimeSeconds) {
        this.nameToUrl = new ConcurrentHashMap<>();
        this.sqs = sqs;
        this.waitTimeSeconds = waitTimeSeconds;
    }

    public void push(List<JsonNode> nodes, State state) {
        nodes.forEach(node -> {
            SendMessageRequest smr = sendMessageRequest(node, state);
            sqs.sendMessage(smr);
        });
    }

    public void push(JsonNode node, State state) {
        push(List.of(node), state);
    }

    public Optional<ProcessedNode> next(State state, int lockTimeout) {
        return sqs.receiveMessage(receiveMessageRequest(state, lockTimeout))
                .getMessages()
                .stream()
                .findFirst()
                .filter(Objects::nonNull)
                .map(m -> toProcessedNode(m, state));
    }

    public Optional<ProcessedNode> next(State state) {
        return next(state, 60);
    }

    private ProcessedNode toProcessedNode(Message m, State s) {
        try {
            return new ProcessedNode(mapper.readTree(m.getBody()), callback(s, m));
        } catch (IOException shouldNeverHappen) {
            return null;
        }
    }

    private Runnable callback(State state, Message m) {
        return () -> sqs.deleteMessage(deleteMessageRequest(state, m));
    }

    private DeleteMessageRequest deleteMessageRequest(State state, Message m) {
        return new DeleteMessageRequest(queueUrl(state), m.getReceiptHandle());
    }

    private ReceiveMessageRequest receiveMessageRequest(State state, int visibilityTimeout) {
        return new ReceiveMessageRequest(queueUrl(state))
                .withWaitTimeSeconds(waitTimeSeconds)
                .withMaxNumberOfMessages(1)
                .withVisibilityTimeout(visibilityTimeout);
    }

    private SendMessageRequest sendMessageRequest(JsonNode node, State state) {
        return new SendMessageRequest()
                .withQueueUrl(queueUrl(state))
                .withMessageBody(node.toString());
    }

    private String queueUrl(State state) {
        return nameToUrl.computeIfAbsent(state.getQueueName(), n -> sqs.getQueueUrl(n).getQueueUrl());
    }
}