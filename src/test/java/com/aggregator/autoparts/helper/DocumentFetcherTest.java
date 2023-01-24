package com.aggregator.autoparts.helper;

import org.jsoup.nodes.Document;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentFetcherTest {
    @InjectMocks
    private DocumentFetcher documentFetcher = new DocumentFetcher();
    @Mock
    private RestTemplate restTemplate;

    @Test
    public void asserEqualsDocBodyTextWhenGetDocumentFromRemoteHost() throws IOException {
        byte[] expectedBytes = Files.readAllBytes(Paths.get("src/test/java/resources/DocumentDocumentFetcher"));
        String expectedDocumentBody = new String(expectedBytes);

        ResponseEntity<String> entity = new ResponseEntity<>("some text in this Document", HttpStatus.OK);

        when(restTemplate.exchange(eq("testUrl"), ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(HttpEntity.class), eq(String.class))).thenReturn(entity);

        Document resultDocument = documentFetcher.getDocumentFromRemoteHost("testUrl");
        String resultDocumentBody = resultDocument.body().text();
        assertEquals(expectedDocumentBody, resultDocumentBody);
    }
}