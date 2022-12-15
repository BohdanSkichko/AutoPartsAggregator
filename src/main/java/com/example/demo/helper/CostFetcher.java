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
public class FetchCost {

    private final static String APPLICATION = "Application";
    private final static String COMA = ",";
    private final static String DOT = ".";
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Executor executor;

    private String getByClass;
    private List<SparePart> sparePartList;

    public void fetchCost(int number) {
        CompletableFuture.supplyAsync(() -> {
            String urlCost = sparePartList.get(number).getUrl();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.USER_AGENT, APPLICATION);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            HttpEntity<String> response = restTemplate.exchange(urlCost, HttpMethod.GET, entity, String.class);
            Document document = Jsoup.parse(response.getBody());
            Element element = document.getElementsByClass(getByClass).first();
            if (element != null) {
                String cost = element.text()
                        .replaceAll(PropertiesReader.getProperties(PathHolder.REPLACE_TEXT.getPath()), "")
                        .replaceAll(COMA, DOT);
                sparePartList.get(number).setCost(Double.parseDouble(cost));
            }
            log.debug("AvtoProService fetchCost() result: " + sparePartList);
            return sparePartList;
        }, executor);
    }
}
