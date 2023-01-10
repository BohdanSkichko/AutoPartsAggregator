package com.aggregator.autoparts.helper;

import com.aggregator.autoparts.exception.BusinessException;
import com.aggregator.autoparts.helper.enumeration.HttpElHolder;
import com.aggregator.autoparts.helper.enumeration.HttpHeaderHolder;
import com.aggregator.autoparts.helper.enumeration.WordAndPunctuationHolder;
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
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
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
    @Autowired
    private RetryTemplate retryTemplate;

    public Document getDocumentFromRemoteHost(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.USER_AGENT, HttpHeaderHolder.APPLICATION.getPath());
            headers.set(HttpHeaderHolder.X_Forwarded_For.getPath(), HttpHeaderHolder.IP_UA.getPath());
            HttpEntity<String> entity = new HttpEntity<>(headers);
            HttpEntity<String> body = retryTemplate.execute(context ->
                    restTemplate.exchange(url, HttpMethod.GET, entity, String.class));
            return Jsoup.parse(Objects.requireNonNull(body.getBody()));
        } catch (Exception exception) {
            throw new BusinessException(exception.getMessage(), exception);
        }
    }
}
