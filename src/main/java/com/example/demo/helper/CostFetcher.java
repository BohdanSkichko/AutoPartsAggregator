package com.example.demo.helper;

import com.example.demo.entity.SparePart;
import com.example.demo.exeptionhendler.BusinessException;
import com.example.demo.service.impl.AvtoPlusService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Slf4j
@Getter
@Setter
public class CostFetcher {
    private RestTemplate restTemplate;
    private Executor executor;
    private String getByClass;
    private List<SparePart> sparePartList;

    public List<SparePart> setCostFromRemoteHost() {
        List<CompletableFuture<SparePart>> completableFutures = new ArrayList<>();
        for (int indexSparePart = 0; indexSparePart < sparePartList.size(); indexSparePart++) {
            completableFutures.add(fetchCost(indexSparePart));
        }
        sparePartList = completableFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        return sparePartList;
    }

    private CompletableFuture<SparePart> fetchCost(int indexSparePart) {
        return CompletableFuture.supplyAsync(() -> {
            Element element = getElementFromRemoteHost(indexSparePart);
            if (element != null) {
                String cost = element.text()
                        .replaceAll(BusinessNameHolder.REPLACE_TEXT.getPath(), "")
                        .replaceAll(BusinessNameHolder.COMA.getPath(), BusinessNameHolder.DOT.getPath())
                        .replaceAll(BusinessNameHolder.REPLACE_HYPHEN_PRICE.getPath(), "");
                if (!cost.isEmpty()) sparePartList.get(indexSparePart).setCost(Double.parseDouble(cost));
            }
            return sparePartList.get(indexSparePart);
        }, executor);
    }

    public List<SparePart> setCostFromRemoteHost(String tagName) {
        List<CompletableFuture<SparePart>> completableFutures = new ArrayList<>();
        for (int indexSparePart = 0; indexSparePart < sparePartList.size(); indexSparePart++) {
            completableFutures.add(fetchCost(indexSparePart, tagName));
        }
        return completableFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private CompletableFuture<SparePart> fetchCost(int indexSparePart, String getByTag) {
        return CompletableFuture.supplyAsync(() -> {
            Element element = getElementFromRemoteHost(indexSparePart);
            if (element != null) {
                String cost = element.getElementsByTag(getByTag).first().text()
                        .replaceAll(BusinessNameHolder.COMA.getPath(), BusinessNameHolder.DOT.getPath())
                        .replaceAll(BusinessNameHolder.WHITE_SPACE.getPath(), "").trim();
                if (!cost.isEmpty()) sparePartList.get(indexSparePart).setCost(Double.parseDouble(cost));
            }
            return sparePartList.get(indexSparePart);
        }, executor);
    }

    private Element getElementFromRemoteHost(int indexSparePart) {
        String url = sparePartList.get(indexSparePart).getUrl();
        Element document = AvtoPlusService.getDocumentFromRemoteHost(url, BusinessNameHolder.APPLICATION.getPath(), restTemplate);
        return document.getElementsByClass(getByClass).first();
    }
}
