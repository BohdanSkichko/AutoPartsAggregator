package com.example.demo.service.impl;

import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.service.SparePartService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class AvtoPlusServiceImp implements SparePartService {
    @Autowired
    Executor executor;
    @Value("#{${pages}}")
    private int pages;
    Logger logger = LoggerFactory.getLogger(AvtoPlusServiceImp.class);

    private static final String URL_FOR_SEARCH = "https://avto-plus.com.ua/ua/search/";
    private static final String REPLACE_TEXT_IN_PRICE = "[а-яА-Я: ]+";
    private static final String AVTO_PLUS_SITE = "https://avto-plus.com.ua/";
    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        Response response = new Response();
        return  getResponseCompletableFuture(serialNumber,response).join();
    }

    private CompletableFuture<Response> getResponseCompletableFuture(String serialNumber, Response response) {
        return CompletableFuture.supplyAsync(
                () -> {
                    logger.info("find spare parts AVTO PLUS " + Thread.currentThread().getName());
                    long start = System.currentTimeMillis();
                    extractDataFromAvtoPlus(response, serialNumber);
                    long end = System.currentTimeMillis();
                    System.out.println(Thread.currentThread().getName() + " finish " + (end - start));
                    return response;
                }, executor);
    }
    private void extractDataFromAvtoPlus(Response response, String serialNumber) {
        try {
            //set first page
            int quantityPages = 1;
            // url = patternFromProperties + page + patterForIterationPages + serialNumber(nameSparePart)
            String patternForIterationPages = "/?search=";
            Document searchPage = Jsoup.connect(URL_FOR_SEARCH + quantityPages + patternForIterationPages + serialNumber).get();
            // search  quantity pages
            Element listPages = searchPage.
                    getElementsByClass("wm-pagination__btn js-submit-pagination load-more-search").first();
            if (listPages != null) {
                Element elementWithQuantityPages = listPages.getElementsByAttribute("data-total").first();
                quantityPages = Integer.parseInt(elementWithQuantityPages.attr("data-total"));
                if (quantityPages > pages) { // if pages > 10, let's leave just 10;
                    quantityPages = pages;
                }
            }
            for (int i = 1; i <= quantityPages; i++) {
                getSparePartOnPageAvtoPlus(response, serialNumber, patternForIterationPages, i);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void getSparePartOnPageAvtoPlus(Response response, String serialNumber,
                                            String patternForIterationPages, int pages) {
        CompletableFuture.supplyAsync(() -> {
            try {
                Document document = Jsoup.connect(URL_FOR_SEARCH + pages + patternForIterationPages + serialNumber).get();
                List<Element> listElementInside = new ArrayList<>(document.
                        getElementsByClass("goods__item product-card product-card--categoryPage"));
                for (Element e : listElementInside) {
                    Elements elements = e.getElementsByTag("a");
                    boolean isFirst = true;
                    for (Element el : elements) {
                        SparePart sparePart = new SparePart();
                        if (StringUtils.hasText(el.attr("href"))) {
                            if (isFirst) {
                                isFirst = false;
                                continue;
                            }
                            sparePart.setDescription(el.text());
                            Elements elementsCost = e.getElementsByClass("basket-button__uah");
                            String text = elementsCost.text();
                            String cost = text.replaceAll(REPLACE_TEXT_IN_PRICE, "");
                            sparePart.setCost(Integer.parseInt(cost));
                            sparePart.setUrl(AVTO_PLUS_SITE + el.attr("href"));
                            response.getSparePartList().add(sparePart);
                            break;
                        }
                    }
                }
                return response;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }
}
