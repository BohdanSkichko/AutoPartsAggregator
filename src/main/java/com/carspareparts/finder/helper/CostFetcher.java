package com.carspareparts.finder.helper;

import com.carspareparts.finder.helper.stringenumholder.StringRegexHolder;
import com.carspareparts.finder.helper.stringenumholder.WordAndPunctuationHolder;
import com.carspareparts.finder.dto.SparePart;
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
@Component
public class CostFetcher {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Executor executor;
    @Autowired
    private DocumentFetcher documentFetcher;


    public List<SparePart> setCostFromRemoteHost(List<SparePart> sparePartList, String className) {
        List<CompletableFuture<SparePart>> completableFutures = new ArrayList<>();
        for (int sparePartIndex = 0; sparePartIndex < sparePartList.size(); sparePartIndex++) {
            completableFutures.add(fetchCost(sparePartIndex, sparePartList, className));
        }
        sparePartList = completableFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        return sparePartList;
    }

    private CompletableFuture<SparePart> fetchCost(int sparePartIndex, List<SparePart> sparePartList, String className) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Element element = getElementFromRemoteHost(sparePartIndex, sparePartList, className);
                if (element == null){
                    element = getElementFromRemoteHost(sparePartIndex,sparePartList,className);
                }
                log.debug("Element: " + element);
                if (element != null) {
                    log.debug("Cost: " + element.text());
                    String cost = element.text()
                            .replaceAll(StringRegexHolder.REPLACE_TEXT.getPath(), "")
                            .replaceAll(WordAndPunctuationHolder.COMA.getPath(), WordAndPunctuationHolder.DOT.getPath())
                            .replaceAll(StringRegexHolder.REPLACE_HYPHEN_PRICE.getPath(), "");
                    log.debug("After parse cost: " + cost);
                    if (!cost.isEmpty()) sparePartList.get(sparePartIndex).setCost(Double.parseDouble(cost));
                }
            } catch (Exception e) {
                log.error(e.getMessage() +
                        " when trying to parse cost Spare Part" + e);
            }
            return sparePartList.get(sparePartIndex);
        }, executor);
    }

    public List<SparePart> setCostFromRemoteHost(String tagName, List<SparePart> sparePartList, String className) {
        List<CompletableFuture<SparePart>> completableFutures = new ArrayList<>();
        for (int indexSparePart = 0; indexSparePart < sparePartList.size(); indexSparePart++) {
            completableFutures.add(fetchCost(indexSparePart, tagName, sparePartList, className));
        }
        return completableFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private CompletableFuture<SparePart> fetchCost(int indexSparePart, String tagName, List<SparePart> sparePartList, String className) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Element element = getElementFromRemoteHost(indexSparePart, sparePartList, className);
                if (element != null) {
                    String cost = element.getElementsByTag(tagName).first().text()
                            .replaceAll(WordAndPunctuationHolder.COMA.getPath(), WordAndPunctuationHolder.DOT.getPath())
                            .replaceAll(WordAndPunctuationHolder.WHITE_SPACE.getPath(), "").trim();
                    if (!cost.isEmpty())
                        sparePartList.get(indexSparePart).setCost(Double.parseDouble(cost));
                }
            } catch (Exception e) {
                log.error(e.getMessage() +
                        " when trying to parse cost Spare Part" + e);
            }
            return sparePartList.get(indexSparePart);
        }, executor);
    }

    private Element getElementFromRemoteHost(int indexSparePart, List<SparePart> sparePartList, String className) {
        String url = sparePartList.get(indexSparePart).getUrl();
        Element document = documentFetcher.getDocumentFromRemoteHost(url);
        log.debug("Document: " + document);
        return document.getElementsByClass(className).first();
    }
}
