package com.polmos.sdor.graph.tracking;

/**
 * Created by RobicToNieMaKomu on 2017-11-09.
 */
public enum State {
    PREV("previous_nodes"), CURR("current_nodes"), FAILED("failed_nodes");

    public String getQueueName() {
        return queueName;
    }

    private final String queueName;

    State(String queueName) {
        this.queueName = queueName;
    }
}
