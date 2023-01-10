package com.aggregator.autoparts.helper.enumeration;

public enum HttpHeaderHolder {
    APPLICATION("Application"),
    IP_UA("185.102.186.203"),
    X_Forwarded_For("X-Forwarded-For"),
    ;
    private final String path;

    HttpHeaderHolder(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
