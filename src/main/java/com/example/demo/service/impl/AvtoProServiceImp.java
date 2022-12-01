package com.example.demo.service.impl;

import com.example.demo.dto.Query;
import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.exeptionhendler.BusinessException;
import com.example.demo.helper.PropertiesReader;
import com.example.demo.helper.StringHttpEntity;
import com.example.demo.service.SparePartService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import springfox.documentation.spring.web.json.Json;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

@Service
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Slf4j
public class AvtoProServiceImp implements SparePartService, StringHttpEntity {
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
        return getResponseFromHttpEntity(response, PropertiesReader.getProperties("Suggestions"),
                executor).join();
    }

    @Override
    public HttpEntity<String> callRemoteHost(String serialNumber) {
        Query query = new Query();
        query.setQuery(serialNumber);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<Query> requestBody = new HttpEntity<>(query, headers);

        return restTemplate.exchange(getUrl(), HttpMethod.PUT, requestBody, String.class);
    }

    @Override
    public void extractJsonNode(Response result, JsonNode arrayNode) {
        try {
            for (JsonNode node : arrayNode) {
                String description = node.path(PropertiesReader.getProperties("FoundPart"))
                        .path(PropertiesReader.getProperties("Part"))
                        .path(PropertiesReader.getProperties("Crossgroup"))
                        .path(PropertiesReader.getProperties("Category"))
                        .path(PropertiesReader.getProperties("Name")).asText();
                SparePart sparePart = new SparePart();
                if (!node.path(PropertiesReader.getProperties("Uri")).asText().equals("null")) {
                    sparePart.setUrl(PropertiesReader.getProperties("UrlAvtoPro") +
                            node.path(PropertiesReader.getProperties("Uri")).asText());

                   String url = sparePart.getUrl();
//                    HttpHeaders headers = new HttpHeaders();
//                    headers.add(HttpHeaders.USER_AGENT, "Application");
//                    HttpEntity<String> entity = new HttpEntity<>(headers);
//                    int start_sId = url.indexOf("sId=");
//                    int finish_sId = url.indexOf("&");
//                    String sId = url.substring(start_sId + 4, finish_sId);
//
//                    int start_seqId = url.indexOf("seqId=");
//                    int finish_seqId = url.indexOf("&", finish_sId + 1);
//                    String seqId = url.substring(start_seqId + 6, finish_seqId);
//
//                    int start_sInd = url.indexOf("sInd=");
//                    int finish_sInd = url.indexOf("&", finish_seqId + 1);
//                    String sInd = url.substring(start_sInd + 5, finish_sInd);
//
//                    int start_uri = url.indexOf("uri=");
//                    int finish_uri = url.indexOf("&", finish_sInd + 1);
//                    String uri = url.substring(start_uri + 4, finish_uri);
//
//                    int start_queryId = url.indexOf("queryId=");
//                    int finish_queryId = url.indexOf("&", finish_uri + 1);
//                    String queryId = url.substring(start_queryId + 8, finish_queryId);
//
//                    int start_selectSuggest = url.indexOf("selectSuggest=");
//                    String selectSuggest = url.substring(start_selectSuggest + 14, url.length() - 1);
//
//                    String ureRequest = UriComponentsBuilder.fromHttpUrl("https://avto.pro//system/search/result/")
//                            .queryParam("sId", "{sId}")
//                            .queryParam("seqId", "{seqId}")
//                            .queryParam("sInd", "{sInd}")
//                            .queryParam("uri", "{uri}")
//                            .queryParam("queryId", "{queryId}")
//                            .queryParam("selectSuggest", "{selectSuggest}")
//                            .encode()
//                            .toUriString();
//                    String as = ureRequest;
//                    Map<String, String> params = new HashMap<>();
//                    params.put("sId", sId);
//                    params.put("seqId", seqId);
//                    params.put("sInd", sInd);
//                    params.put("uri", uri);
//                    params.put("queryId", queryId);
//                    params.put("selectSuggest", selectSuggest);
//                    RestTemplate restTemplate1 = new RestTemplate();
//                    HttpEntity<String> entityCost = restTemplate1.exchange(ureRequest, HttpMethod.GET, entity, String.class, params);
//                    ObjectMapper mapper = new ObjectMapper();
//                    JsonNode root = mapper.readTree(entityCost.toString());
//                    JsonNode costNode = root.findPath("lowPrice");


//                    Document document = Jsoup.connect(url).get();
//                    Element element = Objects.requireNonNull(document.getElementsByClass("ap-feed__table-wrapper").first());
//                    Element elementCost = element.getElementsByTag("td").get(5);
//                    String cost = elementCost.getElementsByAttribute("data-value").text()
//                            .replaceAll(PropertiesReader.getProperties("ReplaceText"),"")
//                            .replaceAll(",",".");
//                    sparePart.setCost(Double.parseDouble(cost));

                    sparePart.setDescription(node.path(PropertiesReader.getProperties("Title")).asText() +
                            description + PropertiesReader.getProperties("SpareList"));
                    result.getSparePartList().add(sparePart);
                }
                if (result.getSparePartList().size() == pages) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(PropertiesReader.getProperties("avtoProEx"),
                    "Exception occurred in extractJsonNode(): " + e.getMessage(), e);
        }

    }

    private String getUrl() {
        return urls.stream()
                .filter(s -> s.contains(PropertiesReader.getProperties("UrlAvtoPro"))).findFirst().get();
    }
}