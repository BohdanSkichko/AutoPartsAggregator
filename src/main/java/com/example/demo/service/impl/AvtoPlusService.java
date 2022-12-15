package com.example.demo.service.impl;

import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.exeptionhendler.BusinessException;
import com.example.demo.helper.PathHolder;
import com.example.demo.helper.PropertiesReader;
import com.example.demo.service.SparePartService;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AvtoPlusService implements SparePartService {
    @Autowired
    private Executor executor;
    private static final String APPLICATION = "Application";
    @Autowired
    private RestTemplate restTemplate;
    @Value("#{${pages}}")
    private int pages;
    @Value("#{'${website.urls}'.split(',')}")
    private List<String> urls;
    private final Logger logger = LoggerFactory.getLogger(AvtoPlusService.class);

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
        Document docWithPageQuantity = getDocumentFirstPage(serialNumber, pagesQuantity);
        pagesQuantity = getPagesQuantityInDocument(pagesQuantity, docWithPageQuantity);
        for (int numberPage = 1; numberPage <= pagesQuantity; numberPage++) {
            getSparePartOnPageAvtoPlus(response, serialNumber, numberPage);
        }
    }

    private int getPagesQuantityInDocument(int pagesQuantity, Document docWithPageQuantity) {
        Element listPages = docWithPageQuantity.
                getElementsByClass(PropertiesReader.getProperties(PathHolder.LOAD_MORE.getPath())).first();
        if (listPages != null) {
            Element elementWithQuantityPages = listPages
                    .getElementsByAttribute(PropertiesReader.getProperties(PathHolder.DATA_TOTAL.getPath())).first();
            pagesQuantity = Integer.parseInt(elementWithQuantityPages
                    .attr(PropertiesReader.getProperties(PathHolder.DATA_TOTAL.getPath())));
            if (pagesQuantity > pages) {
                pagesQuantity = pages;
            }
        }
        return pagesQuantity;
    }

    private void getSparePartOnPageAvtoPlus(Response response, String serialNumber, int page) {
        CompletableFuture.supplyAsync(() -> {
            try {
                String urlCost = getUrl() + page + PropertiesReader.getProperties(PathHolder.SEARCH.getPath()) + serialNumber;
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.USER_AGENT, APPLICATION);
                HttpEntity<String> entity = new HttpEntity<>(headers);
                HttpEntity<String> body = restTemplate.exchange(urlCost, HttpMethod.GET, entity, String.class);
                Document document = Jsoup.parse(body.getBody());
                List<Element> listElementInside = new ArrayList<>(document.
                        getElementsByClass(PropertiesReader.getProperties(PathHolder.PRODUCT.getPath())));
                for (Element element : listElementInside) {
                    processPage(response, element);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new BusinessException(PropertiesReader.getProperties("avtoPlusEx"),
                        e.getMessage(), e);
            }
            return response;
        }, executor);
    }

    private void processPage(Response response, Element element) {
        try {
            Element elementWithItem = element.getElementsByTag(PropertiesReader.getProperties(PathHolder.A.getPath())).get(1);
            SparePart sparePart = new SparePart();
            if (StringUtils.hasText(elementWithItem.text())) {
                sparePart.setDescription(elementWithItem.text());
                String cost = element
                        .getElementsByClass(PropertiesReader.getProperties(PathHolder.BUTTON_UAH.getPath()))
                        .text()
                        .replaceAll(PropertiesReader.getProperties(PathHolder.REPLACE_TEXT.getPath()), "");
                sparePart.setCost(Integer.parseInt(cost));
                sparePart.setUrl(PropertiesReader.getProperties(PathHolder.URL_AVTO_PLUS.getPath()) +
                        elementWithItem.attr(PropertiesReader.getProperties(PathHolder.HREF.getPath())));
                response.getSparePartList().add(sparePart);
            }
        } catch (
                Exception ex) {
            throw new BusinessException(PropertiesReader.getProperties("avtoPlusEx"), ex.getMessage(), ex);
        }

    }

    private Document getDocumentFirstPage(String serialNumber, int page) {
        try {
            return Jsoup.connect(getUrl() + page + PropertiesReader.getProperties(PathHolder.SEARCH.getPath())
                    + serialNumber).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getUrl() {
        return urls.stream().filter(s -> s.contains(PropertiesReader.getProperties("UrlAvtoPlus"))).findFirst().get();
    }
}