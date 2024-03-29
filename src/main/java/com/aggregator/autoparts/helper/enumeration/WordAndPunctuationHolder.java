package com.aggregator.autoparts.helper.enumeration;

public enum WordAndPunctuationHolder {
    NULL("null"),
    TXT(".txt"),
    COST("Cost: "),
    URL("Url: "),
    COMA(","),
    DOT("."),
    WHITE_SPACE(" "),
    ;
    private final String path;

    WordAndPunctuationHolder(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
