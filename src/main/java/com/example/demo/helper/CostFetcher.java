package com.example.demo.helper;

import com.example.demo.entity.SparePart;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Slf4j
@Getter
@Setter
@Component
public class CostFetcher {

    private final static String APPLICATION = "Application";
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Executor executor;
    private String getByClass;
    private List<SparePart> sparePartList;


    public void getCost(int number) {
        CompletableFuture.supplyAsync(() -> {
            Element element = getElementRemoteHost(number);
            if (element != null) {
                String cost = element.text()
                        .replaceAll(PropertiesReader.getProperties(PathHolder.REPLACE_TEXT.getPath()), "")
                        .replaceAll(PropertiesReader.getProperties(PathHolder.COMA.getPath()),
                                PropertiesReader.getProperties(PathHolder.DOT.getPath()));
                sparePartList.get(number).setCost(Double.parseDouble(cost));
            }
            log.debug("AvtoProService fetchCost() result: " + sparePartList);
            return sparePartList;
        }, executor);
    }

    public void getCost(int number, String getByTag) {
        CompletableFuture.supplyAsync(() -> {
            Element element = getElementRemoteHost(number);
            if (element != null) {
                String cost = element.getElementsByTag(getByTag).first().text()
                        .replaceAll(PropertiesReader.getProperties(PathHolder.COMA.getPath()),
                                PropertiesReader.getProperties(PathHolder.DOT.getPath()))
                        .replaceAll(PropertiesReader.getProperties(PathHolder.WHITE_SPACE.getPath()), "").trim();
                sparePartList.get(number).setCost(Double.parseDouble(cost));
            }
            log.debug("AvtoProService fetchCost() result: " + sparePartList);
            return sparePartList;
        }, executor);
    }

    private Element getElementRemoteHost(int number) {
        String url = sparePartList.get(number).getUrl();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.USER_AGENT, APPLICATION);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        Document document = Jsoup.parse(response.getBody());
        return document.getElementsByClass(getByClass).first();
    }
}
