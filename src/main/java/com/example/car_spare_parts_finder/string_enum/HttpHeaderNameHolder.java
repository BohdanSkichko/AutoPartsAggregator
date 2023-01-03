package com.example.car_spare_parts_finder.string_enum;

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
