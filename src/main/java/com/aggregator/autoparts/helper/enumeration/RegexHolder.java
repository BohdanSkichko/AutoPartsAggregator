package com.aggregator.autoparts.helper.enumeration;

public enum RegexHolder {
    REPLACE_TEXT("[а-яА-Яії: ?a-zA-Z;&]+"),
    REPLACE_HYPHEN_PRICE("(\\-\\d+)"),
    ;
    private final String path;

    RegexHolder(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
