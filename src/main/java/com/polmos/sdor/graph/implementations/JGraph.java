package com.polmos.sdor.graph.implementations;

import com.polmos.sdor.github.rest.GithubVertex;
import com.polmos.sdor.graph.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.*;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static com.polmos.sdor.graph.implementations.GraphConstants.*;

/**
 * Created by RobicToNieMaKomu on 2017-10-26.
 */
@Service
public class JGraph implements Graph {
    private final JanusGraph graph;

    public JGraph(@Value("${janus.create.schema}") boolean createSchema,
                  @Value("${janus.conf.file}") String pathToProperties) {
        this.graph = JanusGraphFactory.open(pathToProperties);
        initSchema(createSchema);
    }

    @Override
    public void put(GithubVertex vertex) {
        JanusGraphTransaction tx = graph.newTransaction();
        Vertex v = putToJanus(vertex);
        linkFollowers(vertex, v);
        tx.commit();
    }

    private void linkFollowers(GithubVertex vertex, Vertex v) {
        vertex.getFollowers().forEach(follower -> {
            Vertex fv = getJanusVertex(follower)
                    .orElseGet(() -> addVertex(vertex.getId(), follower, vertex.getLogin()));
            fv.addEdge(FOLLOW_EDGE, v);
        });
    }

    private Vertex putToJanus(GithubVertex vertex) {
        return getJanusVertex(vertex.getLogin())
                    .orElseGet(() -> {
                        Vertex v1 = addVertex(vertex.getId(), vertex.getLogin(), vertex.getParent());
                        if (!vertex.getParent().isEmpty()) {
                            v1.addEdge(FOLLOW_EDGE, getJanusVertex(vertex.getParent()).get());
                        }
                        return v1;
                    });
    }

    private JanusGraphVertex addVertex(long id, String name, String parent) {
        return graph.addVertex(
                ID_PROPERTY, id,
                NAME_PROPERTY, name,
                PARENT_PROPERTY, parent
        );
    }

    private Optional<Vertex> getJanusVertex(String name) {
        return graph.traversal().V().has(NAME_PROPERTY, name).tryNext();
    }

    @Override
    public GithubVertex get(String name) {
        Optional<Vertex> vertexOpt = getJanusVertex(name);
        return vertexOpt.isPresent() ? toGithubVertex(vertexOpt.get()) : null;
    }

    @Override
    public List<String> findFollowers(String name) {
        Optional<Vertex> vertexOpt = getJanusVertex(name);
        return vertexOpt.isPresent() ? findFollowers(vertexOpt.get()) : null;
    }

    private GithubVertex toGithubVertex(Vertex vertex) {
        return new GithubVertex(
                vertex.value(ID_PROPERTY),
                vertex.value(NAME_PROPERTY),
                vertex.value(PARENT_PROPERTY),
                new HashSet<>(findFollowers(vertex))
        );
    }

    private List<String> findFollowers(Vertex v) {
        return graph.traversal().V(v).in(FOLLOW_EDGE).<String>values(NAME_PROPERTY).toList();
    }

    private void initSchema(boolean createSchema) {
        if (createSchema) {
            JanusGraphManagement mgt = graph.openManagement();
            createPropertyKeys(mgt);
            createEdges(mgt);
            createIndexes(mgt);
            mgt.commit();
        }
    }

    private void createPropertyKeys(JanusGraphManagement mgt) {
        if (!mgt.containsPropertyKey(NAME_PROPERTY)) {
            mgt.makePropertyKey(NAME_PROPERTY).dataType(String.class).cardinality(Cardinality.SINGLE).make();
        }
        if (!mgt.containsPropertyKey(ID_PROPERTY)) {
            mgt.makePropertyKey(ID_PROPERTY).dataType(String.class).cardinality(Cardinality.SINGLE).make();
        }
        if (!mgt.containsPropertyKey(PARENT_PROPERTY)) {
            mgt.makePropertyKey(PARENT_PROPERTY).dataType(String.class).cardinality(Cardinality.SINGLE).make();
        }
    }

    private void createIndexes(JanusGraphManagement mgt) {
        if (!mgt.containsGraphIndex(NAME_AND_PARENT_INDEX)) {
            PropertyKey name = mgt.getPropertyKey(NAME_PROPERTY);
            PropertyKey parent = mgt.getPropertyKey(PARENT_PROPERTY);
            mgt.buildIndex(NAME_AND_PARENT_INDEX, Vertex.class).addKey(name).addKey(parent).buildMixedIndex("search");
        }
    }

    private void createEdges(JanusGraphManagement mgt) {
        if (!mgt.containsEdgeLabel(FOLLOW_EDGE)) {
            mgt.makeEdgeLabel(FOLLOW_EDGE).multiplicity(Multiplicity.SIMPLE).make();
        }
    }
}