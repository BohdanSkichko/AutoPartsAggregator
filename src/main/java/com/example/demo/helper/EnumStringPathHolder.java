package com.example.demo.helper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum EnumStringPathHolder {

    GET_EL_BY_TAG_A("a"),
    GET_ATTRIBUTE_HREF("href"),
    GET_EL_BY_ATTRIBUTE_DATA_TOTAL("data-total"),

    UKR_PARTS_DOC_GET_EL_BY_CLASS("col-xs-12 col-md-4 part_box_wrapper"),
    UKR_PARTS_EL_GET_DESCRIPTION_BY_CLASS("part_article"),
    UKR_PARTS_EL_GET_PRICE_BY_CLASS("price_min"),

    AVTO_PLUS_DOC_GET_EL_BY_CLASS("goods__item product-card product-card--categoryPage"),
    AVTO_PLUS_EL_GET_PRICE_BY_CLASS("basket-button__uah"),
    AVTO_PLUS_DOC_SEARCH_GET_EL_BY_CLASS("wm-pagination__btn js-submit-pagination load-more-search"),

    URL_AVTO_PRO("https://avto.pro/"),
    URL_AVTO_PLUS("https://avto-plus.com.ua/"),
    URL_UKR_PARTS("https://ukrparts.com.ua"),

    JSON_NODE_SUGGESTIONS("Suggestions"),
    JSON_NODE_URI("Uri"),
    JSON_NODE_TITLE("Title"),
    JSON_NODE_FOUND_PART("FoundPart"),
    JSON_NODE_PART("Part"),
    JSON_NODE_CROSSGROUP("Crossgroup"),
    JSON_NODE_CATEGORY("Category"),
    JSON_NODE_NAME("Name"),
    REPLACE_TEXT_IN_PRICE("[а-яА-Я: ]+"),
    ;


    private String name;

}
