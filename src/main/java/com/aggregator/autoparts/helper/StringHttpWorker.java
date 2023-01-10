package com.aggregator.autoparts.helper;

import com.aggregator.autoparts.dto.Response;
import com.aggregator.autoparts.exception.BusinessException;
import com.aggregator.autoparts.dto.SparePart;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface StringHttpWorker {
    default CompletableFuture<Response> getResponseFromHttpEntity(HttpEntity<String> response, String path, Executor executor) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        LogHolder.log.info("find spare parts " + getClass().getTypeName() + Thread.currentThread().getName());
                        Response result = new Response();
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(response.getBody());
                        JsonNode resultNode = root.findPath(path);
                        if (resultNode.isArray()) {
                            result.getSparePartList().addAll(extractSpareParts(resultNode));
                        }
                        return result;
                    } catch (Exception e) {
                        LogHolder.log.error(e.getMessage() + " when trying to get response from http entity" + e);
                        throw new BusinessException(e.getMessage(), e);
                    }
                }, executor);
    }

    List<SparePart> extractSpareParts(JsonNode jsonNode);

    HttpEntity<String> callRemoteHost(String serialNumber);

    @Slf4j
    final class LogHolder {
    }
}
