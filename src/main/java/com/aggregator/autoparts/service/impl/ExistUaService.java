package com.aggregator.autoparts.service.impl;

import com.aggregator.autoparts.dto.Response;
import com.aggregator.autoparts.helper.StringHttpWorker;
import com.aggregator.autoparts.helper.UrlHolder;
import com.aggregator.autoparts.helper.enumeration.HttpElHolder;
import com.aggregator.autoparts.helper.enumeration.HttpHeaderHolder;
import com.aggregator.autoparts.helper.enumeration.WordAndPunctuationHolder;
import com.aggregator.autoparts.service.SparePartService;
import com.aggregator.autoparts.dto.SparePart;
import com.aggregator.autoparts.helper.CostFetcher;
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
import java.util.concurrent.Executor;

@Service
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@EqualsAndHashCode
public class ExistUaService implements SparePartService, StringHttpWorker {
    private final String PARAM_NAME = "query";
    private final String PARAM_VALUE = "{query}";
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Executor executor;
    @Autowired
    private CostFetcher costFetcher;
    @Autowired
    private UrlHolder urlHolder;
    @Value("#{${pages}}")
    private int pages;
    @Value("#{'${ExistUa}'}")
    private String url;

    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        HttpEntity<String> response = callRemoteHost(serialNumber);
        return getResponseFromHttpEntity(response, HttpElHolder.MULTIPLE_RESULTS.getPath(),
                executor).join();
    }

    @Override
    public List<SparePart> extractSpareParts(JsonNode jsonNode) {
        List<SparePart> sparePartList = new ArrayList<>();
        try {
            for (JsonNode node : jsonNode) {
                if (node.path(HttpElHolder.PRAG_ID.getPath()).asText().equals(WordAndPunctuationHolder.NULL.getPath()))
                    continue;
                SparePart sparePart = new SparePart();
                sparePart.setUrl(HttpElHolder.EXIST_UA_SEARCH.getPath() +
                        node.path(HttpElHolder.PRAG_ID.getPath()).asText());
                sparePart.setDescription(node.path(HttpElHolder.DESCRIPTION.getPath()).asText() +
                        WordAndPunctuationHolder.WHITE_SPACE.getPath() +
                        node.path(HttpElHolder.TRADEMARK_DESCRIPTION.getPath()).asText());
                sparePartList.add(sparePart);
                if (sparePartList.size() == pages) break;
            }
            sparePartList = costFetcher.setCostFromRemoteHost(HttpElHolder.SPAN.getPath(), sparePartList, HttpElHolder.EXIST_GET_BY_CLASS.getPath());
        } catch (Exception e) {
            log.error(e.getMessage() + "trying to extract Json Node ", e);
        }
        return sparePartList;
    }

    @Override
    public HttpEntity<String> callRemoteHost(String serialNumber) {

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.USER_AGENT, HttpHeaderHolder.APPLICATION.getPath());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String ureRequest = UriComponentsBuilder.fromHttpUrl(urlHolder.getUrl(url))
                .queryParam(PARAM_NAME, PARAM_VALUE)
                .encode()
                .toUriString();

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_NAME, serialNumber);

        return restTemplate.exchange(ureRequest, HttpMethod.GET, entity, String.class, params);
    }
}
