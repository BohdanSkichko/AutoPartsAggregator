package com.carspareparts.finder.helper;

import com.carspareparts.finder.exception.BusinessException;
import com.carspareparts.finder.helper.stringenumholder.HttpHeaderNameHolder;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Component
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Slf4j
public class DocumentFetcher {
    @Autowired
    private RestTemplate restTemplate;

    public Document getDocumentFromRemoteHost(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.USER_AGENT, HttpHeaderNameHolder.APPLICATION.getPath());
            HttpEntity<String> entity = new HttpEntity<>(headers);
            HttpEntity<String> body = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return Jsoup.parse(Objects.requireNonNull(body.getBody()));
        } catch (Exception exception) {
            throw new BusinessException(exception.getMessage(), exception);
        }
    }
}
