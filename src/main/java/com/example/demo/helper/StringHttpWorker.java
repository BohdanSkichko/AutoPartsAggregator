package com.example.demo.helper;

import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.exeptionhendler.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface StringHttpWorker {
    @Slf4j
    final class LogHolder {
    }

    default CompletableFuture<Response> getResponseFromHttpEntity(HttpEntity<String> response, String path, Executor executor) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        LogHolder.log.info("find spare parts " + getClass().getTypeName() + Thread.currentThread().getName());
                        Response result = new Response();
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(response.getBody());
                        JsonNode arrayNode = root.findPath(path);
                        if (arrayNode.isArray()) {
                         result.getSparePartList().addAll(extractJsonNode(arrayNode));
                        }
                        return result;
                    } catch (Exception e) {
                        throw new BusinessException(e.getMessage(), e);
                    }
                }, executor);
    }

    List<SparePart> extractJsonNode(JsonNode arrayNode);

    HttpEntity<String> callRemoteHost(String serialNumber);
}
