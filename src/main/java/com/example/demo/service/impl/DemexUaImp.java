package com.example.demo.service.impl;

import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.exeptionhendler.BusinessException;
import com.example.demo.helper.PropertiesReader;
import com.example.demo.service.SparePartService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Slf4j
@Getter
@Setter
public class DemexUaImp implements SparePartService {
    @Autowired
    private Executor executor;
    @Value("#{'${website.urls}'.split(',')}")
    private List<String> urls;
    private String description;
    private String url;
    private String UrlSearch = PropertiesReader.getProperties("DemexUa");

    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        Response response = new Response();
        return callRemoteHost(serialNumber, response).join();
    }

    private CompletableFuture<Response> callRemoteHost(String serialNumber, Response response) {
        return CompletableFuture.supplyAsync(
                () -> {
                    log.info("find spare parts" + getClass().getName() + " " + Thread.currentThread().getName());
                    try {
                        extractDataFromDexUa(response, serialNumber);
                    } catch (BusinessException e) {
                        throw new RuntimeException(e);
                    }
                    return response;
                }, executor);
    }

    private void extractDataFromDexUa(Response response, String serialNumber) {
        try {
            Document document = Jsoup.connect(getUrl() + serialNumber).get();
            Element element = document.getElementsByClass(PropertiesReader.getProperties("dl-horizontal")).first();
            extractFromElementList(response, element);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(PropertiesReader.getProperties("ukrPartsEx"),
                    "Exception occurred in extractDataFromUkrparts: " + e.getMessage(), e);
        }
    }

    private void extractFromElementList(Response response, Element element) {
        try {
            Elements elementsName = element.getElementsByTag("a");
            Elements elementsDescription = element.getElementsByTag(PropertiesReader.getProperties("strong"));
            int elementDescription = 0;
            for (int i = 0; i < elementsName.size() - 1; i += 2) {
                url = elementsName.get(i).attr("href");
                description = elementsDescription.get(elementDescription).text();
                elementDescription++;
                if (StringUtils.hasText(description)) {
                    SparePart sparePart = new SparePart();
                    sparePart.setDescription(description);
                    sparePart.setUrl(url);
                    response.getSparePartList().add(sparePart);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(PropertiesReader.getProperties("ukrPartsEx"),
                    "extractFromElementList" + e.getMessage(), e);
        }
    }

    private String getUrl() {
        return urls.stream().filter(a -> a.contains(UrlSearch)).findFirst().get();
    }
}
