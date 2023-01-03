package com.carspareparts.finder.service.impl;

import com.carspareparts.finder.dto.Response;
import com.carspareparts.finder.dto.SparePart;
import com.carspareparts.finder.exception.BusinessException;
import com.carspareparts.finder.helper.DocumentFetcher;
import com.carspareparts.finder.service.SparePartService;
import com.carspareparts.finder.helper.stringenumholder.BusinessNameHolder;
import com.carspareparts.finder.helper.stringenumholder.StringRegexHolder;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Slf4j
public class AvtoPlusService implements SparePartService {
    private final static int FIRST_PAGE = 1;
    @Autowired
    private Executor executor;
    @Autowired
    private DocumentFetcher documentFetcher;
    @Autowired
    private RestTemplate restTemplate;
    @Value("#{${pages}}")
    private int pages;
    @Value("#{'${UrlAvtoPlus}'}")
    private String url;
    @Value("#{'${website.urls}'.split(',')}")
    private List<String> urls;

    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        Response response = new Response();
        return callRemoteHost(serialNumber, response).join();
    }

    private CompletableFuture<Response> callRemoteHost(String serialNumber, Response response) {
        return CompletableFuture.supplyAsync(
                () -> {
                    log.info("find spare parts AVTO PLUS " + Thread.currentThread().getName());
                    try {
                        response.getSparePartList().addAll(extractDataOnAllPages(serialNumber));
                    } catch (Exception e) {
                        log.error(e.getMessage() + " when trying to call Remote host " + e);
                        throw new BusinessException(e.getMessage(), e);
                    }
                    return response;
                }, executor);
    }

    private int getPagesQuantityInDocument(Document docWithPageQuantity) {
        int pagesQuantity = FIRST_PAGE;
        Element listPages = docWithPageQuantity.
                getElementsByClass(BusinessNameHolder.LOAD_MORE_SEARCH.getPath()).first();
        if (listPages != null) {
            Element elementWithQuantityPages = listPages
                    .getElementsByAttribute(BusinessNameHolder.DATA_TOTAL.getPath()).first();
            assert elementWithQuantityPages != null;
            pagesQuantity = Integer.parseInt(elementWithQuantityPages
                    .attr(BusinessNameHolder.DATA_TOTAL.getPath()));
            if (pagesQuantity > pages) pagesQuantity = pages;
        }
        return pagesQuantity;
    }

    private CompletableFuture<List<SparePart>> getSparePartOnPage(String serialNumber, int page) {
        return CompletableFuture.supplyAsync(() -> {
            List<SparePart> sparePartList = new ArrayList<>();
            try {
                String url = getUrl() + page + BusinessNameHolder.SEARCH.getPath() + serialNumber;
                Document document = documentFetcher.getDocumentFromRemoteHost(url);
                List<Element> listElementInside = new ArrayList<>(document.
                        getElementsByClass(BusinessNameHolder.GOOD_ITEM.getPath()));
                for (Element element : listElementInside) {
                    sparePartList.addAll(extractDataOnPage(element));
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return sparePartList;
        }, executor);
    }

    private List<SparePart> extractDataOnAllPages(String serialNumber) {
        List<SparePart> sparePartList = new ArrayList<>();
        try {
            Document docWithPageQuantity = getDocumentFirstPage(serialNumber);
            int pagesQuantity = getPagesQuantityInDocument(docWithPageQuantity);
            List<CompletableFuture<List<SparePart>>> completableFutures = new ArrayList<>();
            for (int pageNumber = FIRST_PAGE; pageNumber <= pagesQuantity; pageNumber++) {
                completableFutures.add(getSparePartOnPage(serialNumber, pageNumber));
            }
            List<List<SparePart>> listList = completableFutures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
            for (List<SparePart> list : listList) sparePartList.addAll(list);
        } catch (Exception e) {
            log.error(e.getMessage() + " when trying to extract data on all pages " + e);
            throw new BusinessException(e.getMessage(), e);
        }
        return sparePartList;
    }

    private List<SparePart> extractDataOnPage(Element element) {
        List<SparePart> sparePartList = new ArrayList<>();
        try {
            Element elementWithItem = element.getElementsByTag(BusinessNameHolder.A.getPath()).get(1);
            SparePart sparePart = new SparePart();
            if (StringUtils.hasText(elementWithItem.text())) {
                sparePart.setDescription(elementWithItem.text());
                String cost = element
                        .getElementsByClass(BusinessNameHolder.BASKET_BUTTON.getPath())
                        .text()
                        .replaceAll(StringRegexHolder.REPLACE_TEXT.getPath(), "");
                sparePart.setCost(Integer.parseInt(cost));
                sparePart.setUrl(url +
                        elementWithItem.attr(BusinessNameHolder.HREF.getPath()));
                sparePartList.add(sparePart);
            }
        } catch (Exception e) {
            log.error(e.getMessage() + " when trying to extract spare part on page " + e);
            throw new BusinessException(e.getMessage(), e);
        }
        return sparePartList;
    }

    private Document getDocumentFirstPage(String serialNumber) {
        try {
            return Jsoup.connect(getUrl() + FIRST_PAGE + BusinessNameHolder.SEARCH.getPath() + serialNumber).get();
        } catch (IOException e) {
            log.error(e.getMessage() + " when trying to get document " + e);
            throw new BusinessException(e.getMessage(), e);
        }
    }

    private String getUrl() {
        return urls.stream().filter(s -> s.contains(url)).findFirst().get();
    }
}