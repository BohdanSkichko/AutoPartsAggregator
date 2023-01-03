package com.carspareparts.finder.helper.stringenumholder;

public enum HttpHeaderNameHolder {
    APPLICATION("Application"),
    ;
    private final String path;

    HttpHeaderNameHolder(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
