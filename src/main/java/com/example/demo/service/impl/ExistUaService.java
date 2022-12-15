package com.example.demo.service.impl;

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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@EqualsAndHashCode
public class ExistUaService implements SparePartService, StringHttpWorker {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Executor executor;
    @Value("#{${pages}}")
    private int pages;
    @Value("#{'${website.urls}'.split(',')}")
    private List<String> urls;
    final static String GET_BY_TAG = "span";

    private static final String APPLICATION = "Application";

    private final static String GET_BY_CLASS = "SearchResultsGridstyle__SearchResultsPrice-sc-1ibh0zg-15";

    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        HttpEntity<String> response = callRemoteHost(serialNumber);
        return getResponseFromHttpEntity(response, PropertiesReader.getProperties(PathHolder.MULTIPLE_RESULTS.getPath()),
                executor).join();
    }

    @Override
    public List<SparePart> extractJsonNode(JsonNode arrayNode) {
        List<SparePart> sparePartList = new ArrayList<>();
        try {
            for (JsonNode node : arrayNode) {
                SparePart sparePart = new SparePart();
                if (!node.path(PropertiesReader.getProperties(PathHolder.PRAG.getPath())).asText().equals("null")) {
                    sparePart.setUrl(PropertiesReader.getProperties(PathHolder.EXISTUA.getPath()) +
                            node.path(PropertiesReader.getProperties(PathHolder.PRAG.getPath())).asText());
                    sparePart.setDescription(node.path(PropertiesReader.getProperties(PathHolder.DESCRIPTION.getPath())).asText() +
                            PropertiesReader.getProperties(PathHolder.WHITE_SPACE.getPath()) +
                            node.path(PropertiesReader.getProperties(PathHolder.TRADEMARK.getPath())).asText());
                    sparePartList.add(sparePart);
                    if (sparePartList.size() == pages) {
                        break;
                    }
                    CostFetcher costFetcher = new CostFetcher(restTemplate, executor, GET_BY_CLASS, sparePartList);
                    for (int i = 0; i < sparePartList.size(); i++) {
                        costFetcher.getCost(i, GET_BY_TAG);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(PropertiesReader.getProperties("existUA"),
                    "Exception occurred in extractJsonNode(): " + e.getMessage(), e);
        }
        return sparePartList;
    }

    @Override
    public HttpEntity<String> callRemoteHost(String serialNumber) {

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.USER_AGENT, APPLICATION);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String ureRequest = UriComponentsBuilder.fromHttpUrl(getUrl())
                .queryParam("query", "{query}")
                .encode()
                .toUriString();

        Map<String, String> params = new HashMap<>();
        params.put("query", serialNumber);

        return restTemplate.exchange(ureRequest, HttpMethod.GET, entity, String.class, params);
    }


    private String getUrl() {
        return urls.stream()
                .filter(s -> s.contains(PropertiesReader.getProperties("ExistUa"))).findFirst().get();
    }
}
