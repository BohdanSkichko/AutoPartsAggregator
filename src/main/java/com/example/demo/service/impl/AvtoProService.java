package com.example.demo.service.impl;

import com.example.demo.dto.Query;
import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.exeptionhendler.BusinessException;
import com.example.demo.helper.*;
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
    @Autowired
    private CostFetcher costFetcher;
    @Value("#{${pages}}")
    private int pages;
    @Value("#{'${website.urls}'.split(',')}")
    private List<String> urls;

    private static final String URI_START = "uri=";
    private static final String URI_FINISH = "&";
    private static final String URL_SPACE = "%2F";

    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        HttpEntity<String> response = callRemoteHost(serialNumber);
        return getResponseFromHttpEntity(response, BusinessNameHolder.SUGGESTIONS.getPath(),
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
                if (!node.path(BusinessNameHolder.URI.getPath()).asText().equals("null")) {
                    String description = node.path(BusinessNameHolder.FOUND_PART.getPath())
                            .path(BusinessNameHolder.PART.getPath())
                            .path(BusinessNameHolder.CROSSGROUP.getPath())
                            .path(BusinessNameHolder.CATEGORY.getPath())
                            .path(BusinessNameHolder.NAME.getPath()).asText();
                    String uri = node.path(BusinessNameHolder.URI.getPath()).asText();
                    int start_uri = uri.indexOf(URI_START);
                    int finish_uri = uri.indexOf(URI_FINISH, start_uri);
                    String url = uri.substring((start_uri + URI_START.length()), finish_uri).replace(URL_SPACE, "");
                    sparePart.setUrl(PropertiesReader.getProperties(PathHolder.URL_AVTO_PRO.getPath()) + url);
                    sparePart.setDescription(node.path(BusinessNameHolder.TITLE.getPath()).asText() +
                            BusinessNameHolder.WHITE_SPACE.getPath() + description);
                    if (sparePartList.size() == pages) {
                        break;
                    }
                    sparePartList.add(sparePart);
                }
            }
            sparePartList = costFetcher.setCostFromRemoteHost(BusinessNameHolder.SPAN.getPath(), sparePartList,
                    BusinessNameHolder.MICRODATA.getPath());
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(e.getMessage(), e);
        }
        return sparePartList;
    }

    private String getUrl() {
        return urls.stream()
                .filter(s -> s.contains(PropertiesReader.getProperties("UrlAvtoPro"))).findFirst().get();
    }
}