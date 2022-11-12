package com.example.demo.service.impl;

import com.example.demo.dto.Query;
import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.service.SparePartService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
@AllArgsConstructor
@NoArgsConstructor
public class AvtoProServiceImp implements SparePartService {
    @Autowired
    RestTemplate restTemplate;
    private final static String SITE = "https://avto.pro/";
    private final static String SITE_FOR_PUT_REQUEST = "https://avto.pro/api/v1/search/query/";

    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        HttpEntity<String> response = getHttpEntityStringMethodPUT(serialNumber);
        return getResponseFromHttpEntityString(response);
    }

    @NotNull
    private HttpEntity<String> getHttpEntityStringMethodPUT(String serialNumber) {

        Query query = new Query();
        query.setQuery(serialNumber);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.ALL_VALUE);
        headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<Query> requestBody = new HttpEntity<>(query, headers);

        return restTemplate.exchange(SITE_FOR_PUT_REQUEST, HttpMethod.PUT, requestBody, String.class);
    }

    @NotNull
    private Response getResponseFromHttpEntityString(HttpEntity<String> response) {
        try {
            Response result = new Response();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode contactNode = root.path("Suggestions");
            if (contactNode.isArray()) {
                for (JsonNode node : contactNode) {
                    String description = node.path("FoundPart").path("Part").path("Crossgroup").path("Category")
                            .path("Name").asText();
                    SparePart sparePart = new SparePart();
                    sparePart.setUrl(SITE + node.path("Uri").asText());
                    sparePart.setDescription(node.path("Title").asText() + " " + description);
                    result.getSparePartList().add(sparePart);
                }
            }
            return result;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}