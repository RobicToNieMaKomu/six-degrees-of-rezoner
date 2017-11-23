package com.polmos.sdor.sqs;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.polmos.sdor.graph.tracking.GraphTracker;
import com.polmos.sdor.graph.tracking.ProcessedNode;
import com.polmos.sdor.graph.tracking.State;
import org.elasticmq.rest.sqs.SQSRestServer;
import org.elasticmq.rest.sqs.SQSRestServerBuilder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * Created by RobicToNieMaKomu on 2017-11-08.
 */
public class GraphTrackerTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    private static SQSRestServer server;
    private static AmazonSQS sqs;
    private static GraphTracker graphTracker;

    @BeforeClass
    public static void setup() {
        server = SQSRestServerBuilder.start();
        AmazonSQSClientBuilder builder = AmazonSQSClientBuilder.standard();
        builder.setEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:9324", ""));
        sqs = builder.build();
        graphTracker = new GraphTracker(sqs, 0);
    }

    @Before
    public void recreateQueue() {
        sqs.listQueues().getQueueUrls().forEach(sqs::deleteQueue);
        sqs.createQueue(State.CURR.getQueueName());
    }

    @AfterClass
    public static void clean() {
        server.stopAndWait();
    }

    @Test
    public void shouldReturnEmptyOptionalWhenThereAreNoMessagesInQueue() {
        Optional<ProcessedNode> result = graphTracker.next(State.CURR);

        assertEquals(Optional.empty(), result);
    }

    @Test
    public void nodeAddedShouldBeSentToQueue() {
        JsonNode node = node("abc",1, "parent");

        graphTracker.push(List.of(node), State.CURR);
        Optional<ProcessedNode> result = graphTracker.next(State.CURR);

        assertNode("abc", 1, "parent", result.get());
    }

    @Test
    public void nodeSentToQueueShouldBeAvailableTillAcked() {
        JsonNode node = node("abc",1, "p");

        graphTracker.push(List.of(node), State.CURR);
        Optional<ProcessedNode> r1 = graphTracker.next(State.CURR, 0);
        Optional<ProcessedNode> r2 = graphTracker.next(State.CURR, 0);
        Optional<ProcessedNode> r3 = graphTracker.next(State.CURR, 0);

        assertNode("abc", 1, "p", r1.get());
        assertNode("abc", 1, "p", r2.get());
        assertNode("abc", 1, "p", r3.get());

        r3.get().acknowledgeProcessing();
        Optional<ProcessedNode> r4 = graphTracker.next(State.CURR);

        assertEquals(Optional.empty(), r4);
    }

    private JsonNode node(String login, long id, String parent) {
        ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
        node.put("login", login);
        node.put("id", id);
        node.put("parent", parent);
        return node;
    }

    private void assertNode(String login, long id, String parent, ProcessedNode result) {
        assertEquals(login, result.getLogin());
        assertEquals(id, result.getId());
        assertEquals(parent, result.getParent());
    }
}
