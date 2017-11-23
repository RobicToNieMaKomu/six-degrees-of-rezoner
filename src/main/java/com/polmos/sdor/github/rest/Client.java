package com.polmos.sdor.github.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Created by RobicToNieMaKomu on 2017-11-09.
 */
public interface Client {
    ArrayNode getFollowers(String user) throws InterruptedException ;
}
