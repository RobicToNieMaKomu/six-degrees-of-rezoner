package com.polmos.sdor.graph.tracking;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by RobicToNieMaKomu on 2017-11-08.
 */
public class ProcessedNode {
    private final long id;
    private final String login;
    private final String parent;
    private final JsonNode node;
    private final Runnable onSuccess;

    public ProcessedNode(JsonNode node, Runnable onSuccess) {
        this.id = node.get("id").asLong();
        this.login = node.get("login").asText();
        this.parent = node.get("parent").asText();
        this.node = node;
        this.onSuccess = onSuccess;
    }

    public long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getParent() {
        return parent;
    }

    public JsonNode getNode() {
        return node;
    }

    public void acknowledgeProcessing() {
        onSuccess.run();
    }
}