package com.example.demo.helper;

public enum BusinessNameHolder {
    A("a"),
    APPLICATION("Application"),
    HREF("href"),
    STRONG("strong"),
    DL_HORIZONTAL("dl-horizontal"),
    PRAG_ID("prag_id"),
    EXIST_UA_SEARCH("https://exist.ua/search/?product_id="),
    DESCRIPTION("description"),
    TRADEMARK_DESCRIPTION("trademark_description"),
    DATA_TOTAL("data-total"),
    DATA("data"),
    PART_BOX_WRAPPER("col-xs-12 col-md-4 part_box_wrapper"),
    PART_ARTICLE("part_article"),
    PRICE_MIN("price_min"),
    NEW_PRICE("new-price"),
    GOOD_ITEM("goods__item product-card product-card--categoryPage"),
    BASKET_BUTTON("basket-button__uah"),
    LOAD_MORE_SEARCH("wm-pagination__btn js-submit-pagination load-more-search"),
    SEARCH("/?search="),
    MULTIPLE_RESULTS("multipleResults"),
    SUGGESTIONS("Suggestions"),
    URI("Uri"),
    MICRODATA("feed-microdata__price"),
    TITLE("Title"),
    FOUND_PART("FoundPart"),
    PART("Part"),
    CROSSGROUP("Crossgroup"),
    CATEGORY("Category"),
    NAME("Name"),
    REPLACE_TEXT("[а-яА-Яії: ?a-zA-Z;&]+"),

    REPLACE_HYPHEN_PRICE("(\\-\\d+)"),
    TXT(".txt"),
    SPAN("span"),
    COST("Cost: "),
    URL("Url: "),
    COMA(","),
    DOT("."),
    WHITE_SPACE(" "),
    EXIST_GET_BY_CLASS("SearchResultsGridstyle__SearchResultsPrice-sc-1ibh0zg-15"),

    ;


    private final String path;

    BusinessNameHolder(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
