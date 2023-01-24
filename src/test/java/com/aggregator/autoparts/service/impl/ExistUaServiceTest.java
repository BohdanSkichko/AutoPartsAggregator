package com.aggregator.autoparts.service.impl;

import com.aggregator.autoparts.dto.SparePart;
import com.aggregator.autoparts.helper.CostFetcher;
import com.aggregator.autoparts.helper.UrlHolder;
import com.aggregator.autoparts.helper.enumeration.HttpElHolder;
import com.aggregator.autoparts.helper.enumeration.WordAndPunctuationHolder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExistUaServiceTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private CostFetcher costFetcher;
    @Mock
    private UrlHolder urlHolder;
    @InjectMocks
    private ExistUaService existUaService = new ExistUaService();


    @Test
    void assertEqualsDataSparePartsWhenExtractSpareParts() throws IOException {
        byte[] expectedBytes = Files.readAllBytes(Paths.get("src/test/java/resources/ExistUaJsonNodeResponse"));
        String expectedString = new String(expectedBytes);

        HttpEntity<String> expectedEntity = new HttpEntity<>(expectedString);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode expectedRoot = mapper.readTree(expectedEntity.getBody());
        JsonNode expectedNode = expectedRoot.findPath("multipleResults");

        SparePart first = new SparePart();
        first.setCost(0);
        first.setDescription("Wheel Brake Cylinder" + WordAndPunctuationHolder.WHITE_SPACE.getPath() + "ABS");
        first.setUrl(HttpElHolder.EXIST_UA_SEARCH.getPath() + "8189556");

        SparePart second = new SparePart();
        second.setCost(0);
        second.setDescription("Distributor cap" + WordAndPunctuationHolder.WHITE_SPACE.getPath() + "Angli");
        second.setUrl(HttpElHolder.EXIST_UA_SEARCH.getPath() + "28108559");

        List<SparePart> expectedList = new ArrayList<>();
        expectedList.add(first);
        expectedList.add(second);

        when(costFetcher.setCostFromRemoteHost(anyString(), eq(expectedList), anyString())).thenReturn(expectedList);

        List<SparePart> resultList = existUaService.extractSpareParts(expectedNode);

        assertEquals(expectedList, resultList);

    }

    @Test
    void assertEqualsSerialNumberAndPutParams() {
        ResponseEntity<String> entity = new ResponseEntity<>("excepted Entity", HttpStatus.OK);

        Map<String, String> params = new HashMap<>();
        params.put("query", "MySerialNumber");

        when(urlHolder.getUrl(null)).thenReturn("http://someUrl");

        when(restTemplate.exchange(anyString(),
                eq(HttpMethod.GET),
                ArgumentMatchers.any(),
                eq(String.class),
                eq(params))).thenReturn(entity);

        HttpEntity<String> test = existUaService.callRemoteHost("MySerialNumber");

        assertEquals(entity, test);
    }
}