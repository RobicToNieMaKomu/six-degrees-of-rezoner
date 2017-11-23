package com.polmos.sdor.graph;

import com.polmos.sdor.github.rest.GithubVertex;

import java.util.List;

/**
 * Created by RobicToNieMaKomu on 2017-10-26.
 */
public interface Graph {
    void put(GithubVertex vertex);

    GithubVertex get(String name);

    List<String> findFollowers(String name);
}
