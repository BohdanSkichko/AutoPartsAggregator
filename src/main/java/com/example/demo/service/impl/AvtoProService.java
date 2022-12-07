package com.example.demo.service.impl;

import com.example.demo.dto.Query;
import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.exeptionhendler.BusinessException;
import com.example.demo.helper.PathHolder;
import com.example.demo.helper.PropertiesReader;
import com.example.demo.helper.StringHttpWorker;
import com.example.demo.service.SparePartService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Slf4j
public class AvtoProService implements SparePartService, StringHttpWorker {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Executor executor;
    @Value("#{${pages}}")
    private int pages;
    @Value("#{'${website.urls}'.split(',')}")
    private List<String> urls;
    private static final String APPLICATION = "Application";

    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        HttpEntity<String> response = callRemoteHost(serialNumber);
        return getResponseFromHttpEntity(response, PropertiesReader.getProperties(PathHolder.SUGGESTIONS.getPath()),
                executor).join();
    }

    @Override
    public HttpEntity<String> callRemoteHost(String serialNumber) {
        Query query = new Query();
        query.setQuery(serialNumber);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<Query> requestBody = new HttpEntity<>(query, headers);

        return restTemplate.exchange(getUrl(), HttpMethod.PUT, requestBody, String.class);
    }

    @Override
    public List<SparePart> extractJsonNode(JsonNode arrayNode) {
        List<SparePart> sparePartList = new ArrayList<>();
        Set<SparePart> sss = new HashSet<>();
        try {
            for (JsonNode node : arrayNode) {
                SparePart sparePart = new SparePart();
                String description = node.path(PropertiesReader.getProperties(PathHolder.FOUND_PART.getPath()))
                        .path(PropertiesReader.getProperties(PathHolder.PART.getPath()))
                        .path(PropertiesReader.getProperties(PathHolder.CROSSGROUP.getPath()))
                        .path(PropertiesReader.getProperties(PathHolder.CATEGORY.getPath()))
                        .path(PropertiesReader.getProperties(PathHolder.NAME.getPath())).asText();
                if (!node.path(PropertiesReader.getProperties(PathHolder.URI.getPath())).asText().equals("null")) {
                    sparePart.setUrl(PropertiesReader.getProperties(PathHolder.URL_AVTO_PRO.getPath()) +
                            node.path(PropertiesReader.getProperties(PathHolder.URI.getPath())).asText());
                    sparePart.setDescription(node.path(PropertiesReader.getProperties(PathHolder.TITLE.getPath())).asText() +
                            PropertiesReader.getProperties(PathHolder.WHITE_SPACE.getPath()) + description);
                    if (sparePartList.size() == pages) {
                        break;
                    }
                    sparePartList.add(sparePart);
                }
            }
            for (int i = 0; i < sparePartList.size(); i++) {
                setCost(sparePartList, i);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(PropertiesReader.getProperties("avtoProEx"),
                    "Exception occurred in extractJsonNode(): " + e.getMessage(), e);
        }

        return sparePartList;
    }

    private void setCost(List<SparePart> sparePartList, int number) {
        List<SparePart> sparePartListWithCost = new ArrayList<>();
        CompletableFuture.supplyAsync(() -> {
            String url = sparePartList.get(number).getUrl();
            int start_uri = url.indexOf("uri=");
            int finish_uri = url.indexOf("&", start_uri);
            String uri = url.substring(start_uri + 4, finish_uri).replace("%2F", "");
            String urlCost = "https://avto.pro/" + uri;
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.USER_AGENT, APPLICATION);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            HttpEntity<String> response = restTemplate.exchange(urlCost, HttpMethod.GET, entity, String.class);
            Document document = Jsoup.parse(response.getBody());
            Element element = document.getElementsByClass("feed-microdata__price").first();
            if (element != null) {
                String cost = element.getElementsByTag("span")
                        .first().text()
                        .replaceAll(",", ".")
                        .replaceAll(" ", "").trim();
                sparePartList.get(number).setCost(Double.parseDouble(cost));

            }
            return sparePartList;
        }, executor);
    }

    private String getUrl() {
        return urls.stream()
                .filter(s -> s.contains(PropertiesReader.getProperties("UrlAvtoPro"))).findFirst().get();
    }
}