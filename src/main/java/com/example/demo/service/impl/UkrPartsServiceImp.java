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
public class UkrPartsServiceImp implements SparePartService {
    @Autowired
    Executor executor;
    Logger logger = LoggerFactory.getLogger(UkrPartsServiceImp.class);
    private static final String REPLACE_TEXT_IN_PRICE = "[а-яА-Я: ]+";
    private static final String UKRPARTS_SITE = "https://ukrparts.com.ua";
    private static final String URL_SEARCH = "https://ukrparts.com.ua/search/";
    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        Response response = new Response();
        return getResponseCompletableFuture(serialNumber,response).join();
    }

    private CompletableFuture<Response> getResponseCompletableFuture(String serialNumber, Response response) {
        return CompletableFuture.supplyAsync(
                () -> {
                    logger.info("find spare parts UKR PARTS " + Thread.currentThread().getName());
                    long start = System.currentTimeMillis();
                    extractDataFromUkrparts(response, serialNumber);
                    long end = System.currentTimeMillis();
                    System.out.println(Thread.currentThread().getName() + " finish " + (end - start));
                    return response;
                }, executor);
    }

    private void extractDataFromUkrparts(Response response, String serialNumber) {
        try {
            Document document = Jsoup.connect(URL_SEARCH + serialNumber).get();
            List<Element> listElement = new ArrayList<>(document.getElementsByClass("col-xs-12 col-md-4 part_box_wrapper"));
            for (Element e : listElement) {
                Elements elementsName = e.getElementsByClass("part_article");
                SparePart sparePart = new SparePart();
                sparePart.setDescription(elementsName.text());
                sparePart.setSerialNumber(serialNumber);
                Elements elementPrice = e.getElementsByClass("price_min");
                String text = elementPrice.text();
                String cost = text.replaceAll(REPLACE_TEXT_IN_PRICE, "");
                if (cost.isEmpty()) {
                    break;
                }
                sparePart.setCost(Integer.parseInt(cost));
                Elements elementsURL = e.getElementsByTag("a");
                if (StringUtils.hasText(elementsURL.attr("href"))) {
                    sparePart.setUrl(UKRPARTS_SITE + elementsURL.attr("href"));
                    response.getSparePartList().add(sparePart);
                    break;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
