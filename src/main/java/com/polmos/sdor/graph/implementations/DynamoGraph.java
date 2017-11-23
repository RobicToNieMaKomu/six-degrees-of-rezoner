package com.polmos.sdor.graph.implementations;

import com.polmos.sdor.github.rest.GithubVertex;
import com.polmos.sdor.graph.Graph;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by RobicToNieMaKomu on 2017-11-11.
 */
@Service
public class DynamoGraph implements Graph {
    @Override
    public void put(GithubVertex vertex) {
    }

    @Override
    public GithubVertex get(String name) {
        throw new UnsupportedOperationException("one day someone's gonna implement it for sure");
    }

    @Override
    public List<String> findFollowers(String name) {
        throw new UnsupportedOperationException("one day someone's gonna implement it for sure");
    }
}
