package com.polmos.sdor.graph;

import com.polmos.sdor.github.rest.GithubVertex;
import com.polmos.sdor.graph.implementations.JGraph;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by RobicToNieMaKomu on 2017-10-26.
 */
public class JGraphTest {
    private static JGraph graph;

    @BeforeClass
    public static void setup() throws URISyntaxException {
        File f = new File(JGraphTest.class.getClassLoader().getResource("janusgraph-berkeleyje-lucene.properties").toURI());
        graph = new JGraph(false, f.getPath());
    }

    @Test
    public void addByName() throws URISyntaxException {
        GithubVertex vertex = new GithubVertex(1, "1", "", Set.of("11", "12"));

        graph.put(vertex);
        GithubVertex result = graph.get("1");

        assertEquals(vertex, result);
    }

    @Test
    public void findFollowers() {
        GithubVertex vertex = new GithubVertex(1, "2", "", Set.of("21", "22"));

        graph.put(vertex);
        List<String> followers = graph.findFollowers("2");

        assertTrue(followers.contains("21") &&  followers.contains("22"));
    }
}
