package com.example.demo.service.impl;

import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.exeptionhendler.BusinessException;
import com.example.demo.helper.PropertiesReader;
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
    private Executor executor;
    @Value("#{${pages}}")
    private int pages;
    @Value("#{'${website.urls}'.split(',')}")
    private List<String> urls;
    private final Logger logger = LoggerFactory.getLogger(AvtoPlusServiceImp.class);

    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        Response response = new Response();
        return callRemoteHost(serialNumber, response).join();
    }

    private CompletableFuture<Response> callRemoteHost(String serialNumber, Response response) {
        return CompletableFuture.supplyAsync(
                () -> {
                    logger.info("find spare parts AVTO PLUS " + Thread.currentThread().getName());
                    try {
                        extractDataAvtoPlus(response, serialNumber);
                    } catch (BusinessException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                    return response;
                }, executor);
    }

    private void extractDataAvtoPlus(Response response, String serialNumber) {
        int pagesQuantity = 1;
        Document docWithPageQuantity = getDocument(serialNumber, pagesQuantity);
        pagesQuantity = getPagesQuantityInDocument(pagesQuantity, docWithPageQuantity);
        for (int numberPage = 1; numberPage <= pagesQuantity; numberPage++) {
            getSparePartOnPageAvtoPlus(response, serialNumber, numberPage);
        }
    }

    private int getPagesQuantityInDocument(int pagesQuantity, Document docWithPageQuantity) {
        Element listPages = docWithPageQuantity.
                getElementsByClass(PropertiesReader.getProperties("load-more-search")).first();
        if (listPages != null) {
            Element elementWithQuantityPages = listPages
                    .getElementsByAttribute(PropertiesReader.getProperties("data-total")).first();
            pagesQuantity = Integer.parseInt(elementWithQuantityPages
                    .attr(PropertiesReader.getProperties("data-total")));
            if (pagesQuantity > pages) {
                pagesQuantity = pages;
            }
        }
        return pagesQuantity;
    }

    private void getSparePartOnPageAvtoPlus(Response response, String serialNumber, int page) {
        CompletableFuture.supplyAsync(() -> {
            try {
                Document document = getDocument(serialNumber, page);
                List<Element> listElementInside = new ArrayList<>(document.
                        getElementsByClass(PropertiesReader.getProperties("product-card--categoryPage")));
                for (Element e : listElementInside) {
                    Elements elements = e.getElementsByTag(PropertiesReader.getProperties("a"));
                    processPage(response, e, elements);
                }
                return response;
            } catch (Exception e) {
                e.printStackTrace();
                throw new BusinessException(PropertiesReader.getProperties("avtoPlusEx"),
                        e.getMessage(), e);
            }
        }, executor);
    }

    private void processPage(Response response, Element e, Elements elements) {
        try {
            boolean isFirst = true;
            for (Element el : elements) {
                SparePart sparePart = new SparePart();
                if (StringUtils.hasText(PropertiesReader.getProperties("href"))) {
                    if (isFirst) {
                        isFirst = false;
                        continue;
                    }
                    sparePart.setDescription(el.text());
                    Elements elementsCost = e
                            .getElementsByClass(PropertiesReader.getProperties("basket-button__uah"));
                    String text = elementsCost.text();
                    String cost = text.replaceAll(PropertiesReader.getProperties("ReplaceText"), "");
                    sparePart.setCost(Integer.parseInt(cost));
                    sparePart.setUrl(PropertiesReader.getProperties("UrlAvtoPlus") +
                            el.attr(PropertiesReader.getProperties("href")));
                    response.getSparePartList().add(sparePart);
                    break;
                }
            }
        } catch (Exception ex) {
            throw new BusinessException(PropertiesReader.getProperties("avtoPlusEx"), ex.getMessage(), ex);
        }
    }

    private Document getDocument(String serialNumber, int page) {
        try {
            return Jsoup.connect(getUrl() + page + PropertiesReader.getProperties("/?search") + serialNumber).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getUrl() {
        return urls.stream().filter(s -> s.contains(PropertiesReader.getProperties("UrlAvtoPlus"))).findFirst().get();
    }
}