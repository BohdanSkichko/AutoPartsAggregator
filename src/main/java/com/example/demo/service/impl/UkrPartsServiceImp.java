package com.example.demo.service.impl;

import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.exeptionhendler.BusinessHandledException;
import com.example.demo.helper.EnumStringPathHolder;
import com.example.demo.service.SparePartService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class UkrPartsServiceImp implements SparePartService {
    @Autowired
    private Executor executor;
    @Value("#{'${website.urls}'.split(',')}")
    private List<String> urls;
    private Logger logger = LoggerFactory.getLogger(UkrPartsServiceImp.class);
    private static final String UKR_PARTS_EXCEPTION = "UKR_PARTS_EXCEPTION";

    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        Response response = new Response();
        return callRemoteHost(serialNumber, response).join();
    }

    private CompletableFuture<Response> callRemoteHost(String serialNumber, Response response) {
        return CompletableFuture.supplyAsync(
                () -> {
                    logger.info("find spare parts UKR PARTS " + Thread.currentThread().getName());
                    try {
                        extractDataFromUkrparts(response, serialNumber);
                    } catch (BusinessHandledException e) {
                        throw new RuntimeException(e);
                    }
                    return response;
                }, executor);
    }

    private void extractDataFromUkrparts(Response response, String serialNumber) throws BusinessHandledException {
        try {
            Document document = Jsoup.connect(getUrl() + serialNumber).get();
            List<Element> listElement = new ArrayList<>(document
                    .getElementsByClass(EnumStringPathHolder.UKR_PARTS_DOC_GET_EL_BY_CLASS.getName()));
            /*!!!Name*/         extractFromElementList(response, listElement);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessHandledException(UKR_PARTS_EXCEPTION, "extractDataFromUkrparts", e);
        }
    }

    private void extractFromElementList(Response response, List<Element> listElement) {
        for (Element e : listElement) {
            Elements elementsName = e.
                    getElementsByClass(EnumStringPathHolder.UKR_PARTS_EL_GET_DESCRIPTION_BY_CLASS.getName());
            SparePart sparePart = new SparePart();
            sparePart.setDescription(elementsName.text());
            Elements elementPrice = e.
                    getElementsByClass(EnumStringPathHolder.UKR_PARTS_EL_GET_PRICE_BY_CLASS.getName());
            String text = elementPrice.text();
            String cost = text.replaceAll(EnumStringPathHolder.REPLACE_TEXT_IN_PRICE.getName(), "");
            if (cost.isEmpty()) {
                break;
            }
            sparePart.setCost(Integer.parseInt(cost));
            Elements elementsURL = e.getElementsByTag(EnumStringPathHolder.GET_EL_BY_TAG_A.getName());
            if (StringUtils.hasText(elementsURL.attr(EnumStringPathHolder.GET_ATTRIBUTE_HREF.getName()))) {
                sparePart.setUrl(EnumStringPathHolder.URL_UKR_PARTS.getName() +
                        elementsURL.attr(EnumStringPathHolder.GET_ATTRIBUTE_HREF.getName()));
                response.getSparePartList().add(sparePart);
                break;
            }
        }
    }
    private String getUrl() {
        return urls.stream().filter(s -> s.contains(EnumStringPathHolder.URL_UKR_PARTS.getName())).findFirst().get();
    }
}
