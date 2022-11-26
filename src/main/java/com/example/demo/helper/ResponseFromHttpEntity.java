package com.example.demo.helper;

import com.example.demo.entity.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;

public interface ResponseFromHttpEntity {
     default Response getResponseFromHttpEntity(HttpEntity<String> response, String path) throws JsonProcessingException {
        Response result = new Response();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        JsonNode arrayNode = root.findPath(path);
        if (arrayNode.isArray()) {
            extractJsonNode(result, arrayNode);
        }
        return result;
    }
    void extractJsonNode(Response result, JsonNode arrayNode);

}
