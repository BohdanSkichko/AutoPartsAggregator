package com.example.demo.service.impl;

import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.exeptionhendler.BusinessException;
import com.example.demo.helper.PropertiesReader;
import com.example.demo.helper.StringHttpEntity;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@Service
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@EqualsAndHashCode
public class ExistUaImp implements SparePartService, StringHttpEntity {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Executor executor;
    @Value("#{${pages}}")
    private int pages;
    @Value("#{'${website.urls}'.split(',')}")
    private List<String> urls;

    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        HttpEntity<String> response = callRemoteHost(serialNumber);
        return getResponseFromHttpEntity(response, PropertiesReader.getProperties("multipleResults"),
                executor).join();
    }

    @Override
    public void extractJsonNode(Response result, JsonNode arrayNode) {
        try {
            for (JsonNode node : arrayNode) {
                SparePart sparePart = new SparePart();
                if (!node.path(PropertiesReader.getProperties("prag_id")).asText().equals("null")) {
                    sparePart.setUrl(PropertiesReader.getProperties("exist.ua/search/?product_id") +
                            node.path(PropertiesReader.getProperties("prag_id")).asText());
                    sparePart.setDescription(node.path(PropertiesReader.getProperties("description")).asText() +
                            node.path(PropertiesReader.getProperties("trademark_description")).asText() +
                            PropertiesReader.getProperties("SpareList"));
                    result.getSparePartList().add(sparePart);
                    if (result.getSparePartList().size() == pages) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(PropertiesReader.getProperties("existUA"),
                    "Exception occurred in extractJsonNode(): " + e.getMessage(), e);
        }
    }

    @Override
    public HttpEntity<String> callRemoteHost(String serialNumber) {

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.USER_AGENT, "Application");
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
