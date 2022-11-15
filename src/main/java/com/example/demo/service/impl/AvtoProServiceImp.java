package com.example.demo.service.impl;

import com.example.demo.dto.Query;
import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.exeptionhendler.BusinessHandledException;
import com.example.demo.helper.EnumStringPathHolder;
import com.example.demo.service.SparePartService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Service
@AllArgsConstructor
@NoArgsConstructor
public class AvtoProServiceImp implements SparePartService {
    @Autowired
    private RestTemplate restTemplate;
    @Value("#{${pages}}")
    private int pages;
    @Value("#{'${website.urls}'.split(',')}")
    private List<String> urls;
    private final static String AVTO_PRO_EXCEPTION = "AVTO_PRO_EXCEPTION";


    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) throws BusinessHandledException {
        HttpEntity<String> response = callRemoteHost(serialNumber);
        return getResponseFromHttpEntity(response);
    }

    //thymeleaf
    private HttpEntity<String> callRemoteHost(String serialNumber) {
        Query query = new Query();
        query.setQuery(serialNumber);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<Query> requestBody = new HttpEntity<>(query, headers);

        return restTemplate.exchange(getUrl(), HttpMethod.PUT, requestBody, String.class);
    }


    private Response getResponseFromHttpEntity(HttpEntity<String> response) throws BusinessHandledException {
        try {
            Response result = new Response();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode arrayNode = root.path(EnumStringPathHolder.JSON_NODE_SUGGESTIONS.getName());
            if (arrayNode.isArray()) {
                extractedJsonNode(result, arrayNode);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessHandledException(AVTO_PRO_EXCEPTION, "getResponseFromHttpEntity", e);
        }
    }

    private void extractedJsonNode(Response result, JsonNode arrayNode) throws BusinessHandledException {
        try {
            for (JsonNode node : arrayNode) {
                String description = node.path(EnumStringPathHolder.JSON_NODE_FOUND_PART.getName())
                        .path(EnumStringPathHolder.JSON_NODE_PART.getName())
                        .path(EnumStringPathHolder.JSON_NODE_CROSSGROUP.getName())
                        .path(EnumStringPathHolder.JSON_NODE_CATEGORY.getName())
                        .path(EnumStringPathHolder.JSON_NODE_NAME.getName())
                        .asText();
                SparePart sparePart = new SparePart();
                if (node.path(EnumStringPathHolder.JSON_NODE_URI.getName()) != null) {
                    sparePart.setUrl(EnumStringPathHolder.URL_AVTO_PRO.getName() +
                            node.path(EnumStringPathHolder.JSON_NODE_URI.getName()).asText());
                    sparePart.setDescription(node.path(EnumStringPathHolder.JSON_NODE_TITLE.getName()).asText() +
                            " " + description);
                    result.getSparePartList().add(sparePart);
                    if (result.getSparePartList().size() == pages) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessHandledException(AVTO_PRO_EXCEPTION, "extractedJsonNode", e);
        }
    }

    private String getUrl() {
        return urls.stream()
                .filter(s -> s.contains(EnumStringPathHolder.URL_AVTO_PRO.getName())).findFirst().get();
    }
}