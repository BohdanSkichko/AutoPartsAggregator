package com.example.demo.service.impl;


import com.example.demo.dto.Response;
import com.example.demo.entity.SparePart;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.example.demo.service.SparePartService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@NoArgsConstructor

public class SparePartServiceImp implements SparePartService {

    Object target;
    Logger logger = LoggerFactory.getLogger(SparePartServiceImp.class);
    @Value("#{'${website.urls}'.split(',')}")
    List<String> urls;

    private static final String REPLACE_TEXT_IN_PRICE = "[а-яА-Я: ]+";

    private static final String AVTO_PLUS_SITE = "https://avto-plus.com.ua/";
    private static final String UKRPARTS_SITE = "https://ukrparts.com.ua";


    @Async
    public CompletableFuture<Response> searchSparePartBySerialNumber(String serialNumber) {
        Response responseAvtoPlus = new Response();
        CompletableFuture<Response> result1 = CompletableFuture.supplyAsync(
                () -> {
                    logger.info("find first spare parts " + Thread.currentThread().getName());
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        throw new IllegalStateException(e);
                    }
                    for (String url : urls) {
                        if (url.contains("avto-plus")) {
                            extractDataFromAvtoPlus(responseAvtoPlus, url, serialNumber);
                        }
                    }
                    return responseAvtoPlus;
                }
        );
        Response responseUkrParts = new Response();

        CompletableFuture<Response> result2 = CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("find second spare parts " + Thread.currentThread().getName());
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            for (String url : urls) {
                if (url.contains("ukrparts")) {
                    extractDataFromUkrparts(responseUkrParts, url, serialNumber);
                }
            }
            return responseUkrParts;
        });

        CompletableFuture<Response> responseCompletableFuture = result1.thenCombine(result2, (res, res1) -> {
            logger.info("combineResult parts " + Thread.currentThread().getName());
            Response result = new Response();
            result.getSparePartList().addAll(res.getSparePartList());
            result.getSparePartList().addAll(res1.getSparePartList());
            List<SparePart> sortByCost = result.getSparePartList().stream().sorted(Comparator.comparingInt(SparePart::getCost))
                    .collect(Collectors.toList());
            result.setSparePartList(sortByCost);
            return result;
        });
        return responseCompletableFuture;


/*        long start = System.currentTimeMillis();
        logger.info("find spare parts " + Thread.currentThread().getName());
        Response response = new Response();
        for (String url : urls) {
            if (url.contains("ukrparts")) {
                extractDataFromUkrparts(response, url, serialNumber);
            }
            if (url.contains("avto-plus")) {
                extractDataFromAvtoPlus(response, url, serialNumber);
            }
        }
        List<SparePart> sortByCost = response.getSparePartList().stream().sorted(Comparator.comparingInt(SparePart::getCost))
                    .collect(Collectors.toList());
            response.setSparePartList(sortByCost);
        long end = System.currentTimeMillis();
        logger.info("Total time {}", (end - start));
        return CompletableFuture.completedFuture(response);*/

    }


    private void extractDataFromAvtoPlus(Response response, String url, String serialNumber) {

        try {
            // url = patternFromProperties + page + patterForIterationPages + serialNumber(nameSparePart)
            String patternForIterationPages = "/?search=";
            int id = 1;
            Document searchPage = Jsoup.connect(url + "1" + patternForIterationPages + serialNumber).get();
            // search  quantity pages
            Element listPages = searchPage.
                    getElementsByClass("wm-pagination__btn js-submit-pagination load-more-search").first();
            int pagesALL = 1;
            if (listPages != null) {
                Element pages = listPages.getElementsByAttribute("data-total").first();
                pagesALL = Integer.parseInt(pages.attr("data-total"));
                if (pagesALL > 10) { // if pages > 10, let's leave just 10;
                    pagesALL = 10;
                }
            }
            //  iteration for pages -> search sparePart
            for (int i = 1; i <= pagesALL; i++) {
                Document document = Jsoup.connect(url + i + patternForIterationPages + serialNumber).get();
                List<Element> listElementInside = new ArrayList<>(document.
                        getElementsByClass("goods__item product-card product-card--categoryPage"));
                for (Element e : listElementInside) {
                    Elements elements = e.getElementsByTag("a");
                    boolean isFirst = true;
                    for (Element ads : elements) {
                        SparePart sparePart = new SparePart();
                        if (!StringUtils.isEmpty(ads.attr("href"))) {
                            if (isFirst) {
                                isFirst = false;
                                continue;
                            }
                            sparePart.setDescription(ads.text());
                            sparePart.setId(id);
                            sparePart.setSerialNumber(serialNumber);
                            Elements elementsCost = e.getElementsByClass("basket-button__uah");
                            String text = elementsCost.text();
                            String cost = text.replaceAll(REPLACE_TEXT_IN_PRICE, "");
                            sparePart.setCost(Integer.parseInt(cost));
                            sparePart.setUrl(AVTO_PLUS_SITE + ads.attr("href"));
                        }
                        if (sparePart.getUrl() != null) {
                            response.getSparePartList().add(sparePart);
                            id++;
                            break;
                        }
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    private void extractDataFromUkrparts(Response response, String url, String serialNumber) {
        try {
            int id = 1;
            Document document = Jsoup.connect(url + serialNumber).get();
            List<Element> listElement = new ArrayList<>(document.getElementsByClass("col-xs-12 col-md-4 part_box_wrapper"));
            for (Element e : listElement) {
                Elements elementsName = e.getElementsByClass("part_article");
                for (Element el : listElement) {
                    SparePart sparePart = new SparePart();
                    sparePart.setDescription(elementsName.text());
                    sparePart.setId(id);
                    sparePart.setSerialNumber(serialNumber);
                    Elements elementPrice = e.getElementsByClass("price_min");
                    String text = elementPrice.text();
                    String cost = text.replaceAll(REPLACE_TEXT_IN_PRICE, "");
                    if (cost.isEmpty()) {
                        break;
                    }
                    sparePart.setCost(Integer.parseInt(cost));
                    Elements elementsURL = e.getElementsByTag("a");
                    sparePart.setUrl(UKRPARTS_SITE + elementsURL.attr("href"));
                    if (sparePart.getUrl() != null) {
                        response.getSparePartList().add(sparePart);
                        id++;
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
