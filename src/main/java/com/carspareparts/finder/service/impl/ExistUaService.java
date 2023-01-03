package com.carspareparts.finder.service.impl;

import com.carspareparts.finder.dto.Response;
import com.carspareparts.finder.helper.StringHttpWorker;
import com.carspareparts.finder.service.SparePartService;
import com.carspareparts.finder.helper.stringenumholder.BusinessNameHolder;
import com.carspareparts.finder.helper.stringenumholder.HttpHeaderNameHolder;
import com.carspareparts.finder.helper.stringenumholder.WordAndPunctuationHolder;
import com.carspareparts.finder.dto.SparePart;
import com.carspareparts.finder.helper.CostFetcher;
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
    @Value("#{${pages}}")
    private int pages;
    @Value("#{'${ExistUa}'}")
    private String url;
    @Value("#{'${website.urls}'.split(',')}")
    private List<String> urls;

    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        HttpEntity<String> response = callRemoteHost(serialNumber);
        return getResponseFromHttpEntity(response, BusinessNameHolder.MULTIPLE_RESULTS.getPath(),
                executor).join();
    }

    @Override
    public List<SparePart> extractSpareParts(JsonNode jsonNode) {
        List<SparePart> sparePartList = new ArrayList<>();
        try {
            for (JsonNode node : jsonNode) {
                if (node.path(BusinessNameHolder.PRAG_ID.getPath()).asText().equals(WordAndPunctuationHolder.NULL.getPath()))
                    continue;
                SparePart sparePart = new SparePart();
                sparePart.setUrl(BusinessNameHolder.EXIST_UA_SEARCH.getPath() +
                        node.path(BusinessNameHolder.PRAG_ID.getPath()).asText());
                sparePart.setDescription(node.path(BusinessNameHolder.DESCRIPTION.getPath()).asText() +
                        WordAndPunctuationHolder.WHITE_SPACE.getPath() +
                        node.path(BusinessNameHolder.TRADEMARK_DESCRIPTION.getPath()).asText());
                sparePartList.add(sparePart);
                if (sparePartList.size() == pages) break;
            }
            sparePartList = costFetcher.setCostFromRemoteHost(BusinessNameHolder.SPAN.getPath(), sparePartList, BusinessNameHolder.EXIST_GET_BY_CLASS.getPath());
        } catch (Exception e) {
            log.error(e.getMessage() + "trying to extract Json Node ", e);
        }
        return sparePartList;
    }

    @Override
    public HttpEntity<String> callRemoteHost(String serialNumber) {

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.USER_AGENT, HttpHeaderNameHolder.APPLICATION.getPath());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String ureRequest = UriComponentsBuilder.fromHttpUrl(getUrl())
                .queryParam(PARAM_NAME, PARAM_VALUE)
                .encode()
                .toUriString();

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_NAME, serialNumber);

        return restTemplate.exchange(ureRequest, HttpMethod.GET, entity, String.class, params);
    }


    private String getUrl() {
        return urls.stream().filter(s -> s.contains(url)).findFirst().get();
    }
}
