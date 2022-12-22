package com.example.demo.helper;

import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.exeptionhendler.BusinessException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Slf4j
@Getter
@Setter
public class SiteParser {

    private Executor executor;
    private RestTemplate restTemplate;

    private List<String> urls;

    private String urlSearch;


    public Response searchSparePartBySerialNumber(String serialNumber) {
        return callRemoteHost(serialNumber).join();
    }

    private CompletableFuture<Response> callRemoteHost(String serialNumber) {
        return CompletableFuture.supplyAsync(
                () -> {
                    log.info("find spare parts" + getClass().getTypeName() + " " + Thread.currentThread().getName());
                    try {
                        Response result = new Response();
                        result.getSparePartList().addAll(extractData(serialNumber));
                        return result;
                    } catch (BusinessException e) {
                        throw new RuntimeException(e);
                    }
                }, executor);
    }

    private List<SparePart> extractData(String serialNumber) {
        List<SparePart> sparePartList = new ArrayList<>();
        try {
            Document document = Jsoup.connect(getUrl() + serialNumber).get();
            Element element = document.getElementsByClass(BusinessNameHolder.DL_HORIZONTAL.getPath()).first();
            if (element != null) sparePartList.addAll(extractFromElementList(element));
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(SiteParser.class.getName(),
                    " exception occurred in extractData: " + e.getMessage(), e);
        }
        return sparePartList;
    }

    private List<SparePart> extractFromElementList(Element element) {
        List<SparePart> sparePartList = new ArrayList<>();
        try {
            Elements elementsName = element.getElementsByTag(BusinessNameHolder.A.getPath());
            Elements elementsDescription = element.getElementsByTag(BusinessNameHolder.STRONG.getPath());
            int elementDescription = 0;
            for (int i = 0; i < elementsName.size() - 1; i += 2) {
                String url = elementsName.get(i).attr(BusinessNameHolder.HREF.getPath());
                String description = elementsDescription.get(elementDescription).text();
                elementDescription++;
                if (StringUtils.hasText(description)) {
                    SparePart sparePart = new SparePart();
                    sparePart.setDescription(description);
                    sparePart.setUrl(url);
                    sparePartList.add(sparePart);
                }
            }
            CostFetcher costFetcher = new CostFetcher(restTemplate, executor,
                    BusinessNameHolder.NEW_PRICE.getPath(), sparePartList);
            sparePartList = costFetcher.setCostFromRemoteHost();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(SiteParser.class.getName(),
                    " extractFromElementList" + e.getMessage(), e);
        }
        return sparePartList;
    }

    private String getUrl() {
        return urls.stream().filter(urls -> urls.contains(urlSearch)).findFirst().get();
    }
}
