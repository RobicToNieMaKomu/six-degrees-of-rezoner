package com.polmos.sdor.github.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;

/**
 * Created by RobicToNieMaKomu on 2017-11-01.
 */
@Service
public class GithubClient implements Client {
    private static final String GH_BASE = "https://api.github.com/";
    private static final String GET_USER_QUERY = GH_BASE + "users/";
    private static final String GET_RATE_LIMIT = GH_BASE + "rate_limit";
    private static final String FOLLOWERS = "/followers";
    private static final String FOLLOWING = "/following";

    private final RestTemplate template;
    private final HttpEntity<ArrayNode> entityWithHeaders;

    public GithubClient(@Value("${github.oauth}") String oauth) {
        this.template = new RestTemplate();
        this.entityWithHeaders = entityWithHeaders(oauth);
    }

    @Override
    public ArrayNode getFollowers(String user) {
        ArrayNode followers = new ArrayNode(JsonNodeFactory.instance);
        String url = GET_USER_QUERY + user + FOLLOWERS;
        ResponseEntity<ArrayNode> response = get(url);
        followers.addAll(response.getBody());
        boolean hasNext = response.getHeaders().containsKey("Link");
        if (hasNext) {
            int pages = pagesCount(response.getHeaders().get("Link").get(0));
            for (int i = 2; i <= pages; i++) {
                url = GET_USER_QUERY + user + FOLLOWERS + "?page=" + i;
                response = get(url);
                followers.addAll(response.getBody());
            }
        }
        return followers;
    }

    private int pagesCount(String link) {
        String s = link.split(",")[1];
        String p = s.split("page=")[1];
        return Integer.parseInt(p.substring(0, p.indexOf(">")));
    }

    private ResponseEntity<ArrayNode> get(String url) {
        return template.exchange(url, GET, entityWithHeaders, ArrayNode.class);
    }

    public ResponseEntity<ArrayNode> getFollowing(String user) {
        return template.exchange(GET_USER_QUERY + user + FOLLOWING, GET, entityWithHeaders, ArrayNode.class);
    }

    public long getRemainingCalls() {
        ResponseEntity<ObjectNode> entity = template.exchange(GET_RATE_LIMIT, GET, entityWithHeaders, ObjectNode.class);
        return entity.getBody()
                .get("resources")
                .get("core")
                .get("remaining")
                .asInt();
    }

    public LocalDateTime getResetTime() {
        ResponseEntity<ObjectNode> entity = template.exchange(GET_RATE_LIMIT, GET, entityWithHeaders, ObjectNode.class);
        return LocalDateTime.ofEpochSecond(
                entity.getBody()
                        .get("resources")
                        .get("core")
                        .get("reset").asLong(),
                0,
                ZoneOffset.UTC)
                .plusHours(1);
    }

    private HttpEntity<ArrayNode> entityWithHeaders(String oauth) {
        HttpHeaders headers = new HttpHeaders();
        if (oauth != null) {
            headers.set("Authorization", "token " + oauth);
        }
        return new HttpEntity<>(headers);
    }
}
