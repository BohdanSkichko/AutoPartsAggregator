package com.example.demo.helper;

public enum PathHolder {
    URL_AVTO_PRO("UrlAvtoPro"),
    URL_AVTO_PLUS("UrlAvtoPlus"),
    URL_UKR_PARTS("UrlUrkParts"),
    URL_EXIST_UA("ExistUa"),
    URL_DEMEX_UA("DemexUa"),
    URL_AVTOZAPCHASTI("Avtozapchasti"),
    ;

    private final String path;

    PathHolder(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
