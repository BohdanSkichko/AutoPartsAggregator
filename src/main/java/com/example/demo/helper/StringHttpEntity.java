package com.example.demo.helper;

import com.example.demo.entity.Response;
import com.example.demo.exeptionhendler.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpEntity;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


public interface StringHttpEntity {
    @Slf4j
    final class LogHolder {
    }

    default CompletableFuture<Response> getResponseFromHttpEntity(HttpEntity<String> response, String path, Executor executor) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
   /*                   HttpEntity<String> response = String serialNumber;
                        HttpEntity<String> hostResponse = callRemoteHost(String serialNumber);*/
                        LogHolder.log.info("find spare parts " + getClass().getTypeName() + Thread.currentThread().getName());
                        Response result = new Response();
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(response.getBody());
                        JsonNode arrayNode = root.findPath(path);
                        if (arrayNode.isArray()) {
                            extractJsonNode(result, arrayNode);
                        }
                        return result;
                    } catch (BusinessException | JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }, executor);
    }

    void extractJsonNode(Response result, JsonNode arrayNode);

    HttpEntity<String> callRemoteHost(String serialNumber);
}
