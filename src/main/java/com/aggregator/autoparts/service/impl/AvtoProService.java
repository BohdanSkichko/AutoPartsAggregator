package com.aggregator.autoparts.service.impl;

import com.aggregator.autoparts.dto.Response;
import com.aggregator.autoparts.helper.UrlHolder;
import com.aggregator.autoparts.helper.enumeration.HttpElHolder;
import com.aggregator.autoparts.dto.Query;
import com.aggregator.autoparts.dto.SparePart;
import com.aggregator.autoparts.exception.BusinessException;
import com.aggregator.autoparts.helper.CostFetcher;
import com.aggregator.autoparts.helper.StringHttpWorker;
import com.aggregator.autoparts.service.SparePartService;
import com.aggregator.autoparts.helper.enumeration.WordAndPunctuationHolder;
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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

@Service
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Slf4j
public class AvtoProService implements SparePartService, StringHttpWorker {
    private static final String URI_START = "uri=";
    private static final String URI_FINISH = "&";
    private static final String URL_SPACE = "%2F";
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
    @Value("#{'${UrlAvtoPro}'}")
    private String url;


    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        HttpEntity<String> response = callRemoteHost(serialNumber);
        return getResponseFromHttpEntity(response, HttpElHolder.SUGGESTIONS.getPath(), executor).join();
    }

    @Override
    public HttpEntity<String> callRemoteHost(String serialNumber) {
        Query query = new Query();
        query.setQuery(serialNumber);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<Query> requestBody = new HttpEntity<>(query, headers);

        return restTemplate.exchange(urlHolder.getUrl(url), HttpMethod.PUT, requestBody, String.class);
    }

    @Override
    public List<SparePart> extractSpareParts(JsonNode jsonNode) {
        List<SparePart> sparePartList = new ArrayList<>();
        try {
            for (JsonNode node : jsonNode) {
                if (node.path(HttpElHolder.URI.getPath()).asText().equals(WordAndPunctuationHolder.NULL.getPath()))
                    continue;
                SparePart sparePart = new SparePart();
                String description = node.path(HttpElHolder.FOUND_PART.getPath())
                        .path(HttpElHolder.PART.getPath())
                        .path(HttpElHolder.CROSSGROUP.getPath())
                        .path(HttpElHolder.CATEGORY.getPath())
                        .path(HttpElHolder.NAME.getPath()).asText();
                String uri = node.path(HttpElHolder.URI.getPath()).asText();
                int start_uri = uri.indexOf(URI_START);
                int finish_uri = uri.indexOf(URI_FINISH, start_uri);
                String url = uri.substring((start_uri + URI_START.length()), finish_uri).replace(URL_SPACE, "");
                sparePart.setUrl(this.url + url);
                sparePart.setDescription(node.path(HttpElHolder.TITLE.getPath()).asText() +
                        WordAndPunctuationHolder.WHITE_SPACE.getPath() + description);
                sparePartList.add(sparePart);
                if (sparePartList.size() == pages) break;
            }
            sparePartList = costFetcher.setCostFromRemoteHost(HttpElHolder.SPAN.getPath(), sparePartList,
                    HttpElHolder.MICRODATA.getPath());
        } catch (Exception e) {
            log.error(e.getMessage() + " when trying to extract spare part from json node" + e);
            throw new BusinessException(e.getMessage(), e);
        }
        return sparePartList;
    }
}