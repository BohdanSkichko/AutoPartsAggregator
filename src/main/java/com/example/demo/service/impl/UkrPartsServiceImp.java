package com.example.demo.service.impl;

import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.exeptionhendler.BusinessException;
import com.example.demo.helper.PropertiesReader;
import com.example.demo.service.SparePartService;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UkrPartsServiceImp implements SparePartService {
    @Autowired
    private Executor executor;
    @Value("#{'${website.urls}'.split(',')}")
    private List<String> urls;
    private Logger logger = LoggerFactory.getLogger(UkrPartsServiceImp.class);

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
                    } catch (BusinessException e) {
                        throw new RuntimeException(e);
                    }
                    return response;
                }, executor);
    }

    private void extractDataFromUkrparts(Response response, String serialNumber) {
        try {
            Document document = Jsoup.connect(getUrl() + serialNumber).get();
            List<Element> listElement = new ArrayList<>(document
                    .getElementsByClass(PropertiesReader.getProperties("part_box_wrapper")));
            extractFromElementList(response, listElement);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(PropertiesReader.getProperties("ukrPartsEx"),
                    "Exception occurred in extractDataFromUkrparts: " + e.getMessage(), e);
        }
    }

    private void extractFromElementList(Response response, List<Element> listElement) {
       try {
           for (Element e : listElement) {
               Elements elementsName = e.
                       getElementsByClass(PropertiesReader.getProperties("part_article"));
               SparePart sparePart = new SparePart();
               sparePart.setDescription(elementsName.text());
               Elements elementPrice = e.
                       getElementsByClass(PropertiesReader.getProperties("price_min"));
               String text = elementPrice.text();
               String cost = text.replaceAll(PropertiesReader.getProperties("ReplaceText"), "");
               if (cost.isEmpty()) {
                   break;
               }
               sparePart.setCost(Integer.parseInt(cost));
               Elements elementsURL = e.getElementsByTag(PropertiesReader.getProperties("a"));
               if (StringUtils.hasText(elementsURL.attr(PropertiesReader.getProperties("href")))) {
                   sparePart.setUrl(PropertiesReader.getProperties("UrlUrkParts") +
                           elementsURL.attr(PropertiesReader.getProperties("href")));
                   response.getSparePartList().add(sparePart);
                   break;
               }
           }
       } catch (Exception e) {
           e.printStackTrace();
           throw new BusinessException(PropertiesReader.getProperties("ukrPartsEx"),
                   "extractFromElementList" + e.getMessage(), e);
       }
    }

    private String getUrl() {
        return urls.stream().filter(s -> s.contains(PropertiesReader.getProperties("UrlUrkParts"))).findFirst().get();
    }
}
