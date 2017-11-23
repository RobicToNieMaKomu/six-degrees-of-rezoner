package com.polmos.sdor.graph;

import com.polmos.sdor.github.rest.GithubVertex;
import com.polmos.sdor.graph.implementations.DynamoGraph;
import com.polmos.sdor.graph.implementations.JGraph;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by RobicToNieMaKomu on 2017-11-11.
 */
@Service
public class GraphSink {
    private final List<Graph> graphs;

    public GraphSink(JGraph janusGraph, DynamoGraph dynamoDb) {
        this.graphs = List.of(janusGraph, dynamoDb);
    }

    public void push(GithubVertex vertex) {
        graphs.forEach(graph -> graph.put(vertex));
    }
}
