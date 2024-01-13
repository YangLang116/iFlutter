package com.xtu.plugin.flutter.store;

import java.util.Objects;

public class HttpEntity {

    public String path;
    public String method;
    public String description;
    public String response;

    @SuppressWarnings("unused")
    public HttpEntity() {
    }

    public HttpEntity(String path, String method, String description, String response) {
        this.path = path;
        this.method = method;
        this.description = description;
        this.response = response;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpEntity that = (HttpEntity) o;
        return Objects.equals(path, that.path)
                && Objects.equals(method, that.method)
                && Objects.equals(description, that.description)
                && Objects.equals(response, that.response);
    }

    @Override
    public String toString() {
        return "HttpEntity{" +
                "path='" + path + '\'' +
                ", method='" + method + '\'' +
                ", description='" + description + '\'' +
                ", response='" + response + '\'' +
                '}';
    }
}
