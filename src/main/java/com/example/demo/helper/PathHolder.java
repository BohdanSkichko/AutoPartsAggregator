package com.example.demo.helper;

public enum PathHolder {
    A("a"),
    HREF("href"),
    STRONG("strong"),
    DL("dl-horizontal"),
    PRAG("prag_id"),
    EXISTUA("exist.ua/search/?product_id"),
    DESCRIPTION("description"),
    TRADEMARK("trademark_description"),
    DATA_TOTAL("data-total"),
    DATA("data"),
    WRAPPER("part_box_wrapper"),
    ARTICLE("part_article"),
    PRICE_MIN("price_min"),
    PRODUCT("product-card--categoryPage"),
    BUTTON_UAH("basket-button__uah"),
    LOAD_MORE("load-more-search"),
    SEARCH("/?search"),
    URL_AVTO_PRO("UrlAvtoPro"),
    URL_AVTO_PLUS("UrlAvtoPlus"),
    URL_UKR_PARTS("UrlUrkParts"),
    URL_EXIST_UA("ExistUa"),
    URL_DEMEX_UA("DemexUa"),
    URL_AVTOZAPCHASTI("Avtozapchasti"),
    MULTIPLE_RESULTS("multipleResults"),
    SUGGESTIONS("Suggestions"),
    URI("Uri"),
    TITLE("Title"),
    FOUND_PART("FoundPart"),
    PART("Part"),
    CROSSGROUP("Crossgroup"),
    CATEGORY("Category"),
    NAME("Name"),
    REPLACE_TEXT("ReplaceText"),
    COST("Cost"),
    URL("Url"),
    WHITE_SPACE("WhiteSpace"),

    ;

    private final String path;

    PathHolder(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
