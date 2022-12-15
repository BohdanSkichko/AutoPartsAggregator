package com.example.demo.service.impl;

import com.example.demo.dto.Query;
import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.exeptionhendler.BusinessException;
import com.example.demo.helper.CostFetcher;
import com.example.demo.helper.PathHolder;
import com.example.demo.helper.PropertiesReader;
import com.example.demo.helper.StringHttpWorker;
import com.example.demo.service.SparePartService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
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
    private static final String URI_START = "uri=";
    private static final String URI_FINISH = "&";
    private static final String URL_SPACE = "%2F";
    private static final String GET_BY_CLASS = "feed-microdata__price";
    private static final String GET_BY_TAG = "span";

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
        try {
            for (JsonNode node : arrayNode) {
                SparePart sparePart = new SparePart();
                String description = node.path(PropertiesReader.getProperties(PathHolder.FOUND_PART.getPath()))
                        .path(PropertiesReader.getProperties(PathHolder.PART.getPath()))
                        .path(PropertiesReader.getProperties(PathHolder.CROSSGROUP.getPath()))
                        .path(PropertiesReader.getProperties(PathHolder.CATEGORY.getPath()))
                        .path(PropertiesReader.getProperties(PathHolder.NAME.getPath())).asText();
                if (!node.path(PropertiesReader.getProperties(PathHolder.URI.getPath())).asText().equals("null")) {
                    String uri = node.path(PropertiesReader.getProperties(PathHolder.URI.getPath())).asText();
                    int start_uri = uri.indexOf(URI_START);
                    int finish_uri = uri.indexOf(URI_FINISH, start_uri);
                    String url = uri.substring((start_uri + URI_START.length()), finish_uri).replace(URL_SPACE, "");
                    sparePart.setUrl(PropertiesReader.getProperties(PathHolder.URL_AVTO_PRO.getPath()) + url);
                    sparePart.setDescription(node.path(PropertiesReader.getProperties(PathHolder.TITLE.getPath())).asText() +
                            PropertiesReader.getProperties(PathHolder.WHITE_SPACE.getPath()) + description);
                    if (sparePartList.size() == pages) {
                        break;
                    }
                    sparePartList.add(sparePart);
                }
            }
            CostFetcher costFetcher = new CostFetcher(restTemplate, executor, GET_BY_CLASS, sparePartList);
            for (int i = 0; i < sparePartList.size(); i++) {
                costFetcher.getCost(i, GET_BY_TAG);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(PropertiesReader.getProperties("avtoProEx"),
                    "Exception occurred in extractJsonNode(): " + e.getMessage(), e);
        }
        return sparePartList;
    }

    private String getUrl() {
        return urls.stream()
                .filter(s -> s.contains(PropertiesReader.getProperties("UrlAvtoPro"))).findFirst().get();
    }
}