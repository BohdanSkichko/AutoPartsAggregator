package com.example.demo.service.impl;

import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.exeptionhendler.BusinessHandledException;
import com.example.demo.helper.EnumStringPathHolder;
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
    private final static String AVTO_PLUS_EXCEPTION = "AVTO_PLUS_EXCEPTION";
    private final Logger logger = LoggerFactory.getLogger(AvtoPlusServiceImp.class);
    private static final String PATTERN_FOR_ITERATION_PAGES = "/?search=";

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
                        extractDataFromAvtoPlus(response, serialNumber);
                    } catch (BusinessHandledException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                    return response;
                }, executor);
    }

    private void extractDataFromAvtoPlus(Response response, String serialNumber) throws BusinessHandledException {
        try {
            int pagesQuantity = 1;
            String url = getUrl();
            Document searchPage = Jsoup.connect(url + pagesQuantity + PATTERN_FOR_ITERATION_PAGES + serialNumber).get();
            Element listPages = searchPage.
                    getElementsByClass(EnumStringPathHolder.AVTO_PLUS_DOC_SEARCH_GET_EL_BY_CLASS.getName()).first();
            if (listPages != null) {
                Element elementWithQuantityPages = listPages
                        .getElementsByAttribute(EnumStringPathHolder
                                .GET_EL_BY_ATTRIBUTE_DATA_TOTAL.getName()).first();
                pagesQuantity = Integer.parseInt(elementWithQuantityPages
                        .attr(EnumStringPathHolder.GET_EL_BY_ATTRIBUTE_DATA_TOTAL.getName()));
                if (pagesQuantity > pages) {
                    pagesQuantity = pages;
                }
            }
            for (int i = 1; i <= pagesQuantity; i++) {
                getSparePartOnPageAvtoPlus(response, serialNumber, i);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessHandledException(AVTO_PLUS_EXCEPTION, "extractDataFromAvtoPlus", e);
        }
    }


    private void getSparePartOnPageAvtoPlus(Response response, String serialNumber, int pages) {
        CompletableFuture.supplyAsync(() -> {
            try {
                Document document = Jsoup.connect(getUrl() + pages + PATTERN_FOR_ITERATION_PAGES + serialNumber).get();
                List<Element> listElementInside = new ArrayList<>(document.
                        getElementsByClass(EnumStringPathHolder.AVTO_PLUS_DOC_GET_EL_BY_CLASS.getName()));
                for (Element e : listElementInside) {
                    Elements elements = e.getElementsByTag(EnumStringPathHolder.GET_EL_BY_TAG_A.getName());
                    processPage(response, e, elements);
                }
                return response;
            } catch (IOException | BusinessHandledException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }, executor);
    }

    private void processPage(Response response, Element e, Elements elements) throws BusinessHandledException {
        try {
            boolean isFirst = true;
            for (Element el : elements) {
                SparePart sparePart = new SparePart();
                if (StringUtils.hasText(el.attr(EnumStringPathHolder.GET_ATTRIBUTE_HREF.getName()))) {
                    if (isFirst) {
                        isFirst = false;
                        continue;
                    }
                    sparePart.setDescription(el.text());
                    Elements elementsCost = e
                            .getElementsByClass(EnumStringPathHolder.AVTO_PLUS_EL_GET_PRICE_BY_CLASS.getName());
                    String text = elementsCost.text();
                    String cost = text.replaceAll(EnumStringPathHolder.REPLACE_TEXT_IN_PRICE.getName(), "");
                    sparePart.setCost(Integer.parseInt(cost));
                    sparePart.setUrl(EnumStringPathHolder.URL_AVTO_PLUS.getName() + el.attr(EnumStringPathHolder.GET_ATTRIBUTE_HREF.getName()));
                    response.getSparePartList().add(sparePart);
                    break;
                }
            }
        } catch (Exception ex) {
            throw new BusinessHandledException(AVTO_PLUS_EXCEPTION, "processPage", ex);
        }
    }

    private String getUrl() {
        return urls.stream().filter(s -> s.contains(EnumStringPathHolder.URL_AVTO_PLUS.getName())).findFirst().get();
    }

}
