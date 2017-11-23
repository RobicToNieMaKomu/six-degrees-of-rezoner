package com.polmos.sdor.github.rest;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by RobicToNieMaKomu on 2017-10-26.
 */
public class GithubVertex {
    private final long id;
    private final String login;
    private final String parent;
    private final Set<String> followers;

    public GithubVertex(long id, String login, String parent, Set<String> followers) {
        this.id = id;
        this.login = login;
        this.parent = parent;
        this.followers = new HashSet<>(followers);
    }

    public long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public Set<String> getFollowers() {
        return followers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GithubVertex vertex = (GithubVertex) o;

        if (id != vertex.id) return false;
        if (!login.equals(vertex.login)) return false;
        if (!parent.equals(vertex.parent)) return false;
        return followers.equals(vertex.followers);
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + login.hashCode();
        result = 31 * result + parent.hashCode();
        result = 31 * result + followers.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "GithubVertex{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", parent='" + parent + '\'' +
                ", followers=" + followers +
                '}';
    }

    public String getParent() {
        return parent;
    }
}