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
import org.springframework.beans.factory.annotation.Value;
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
@Component
public class SiteParser {

    @Autowired
    private Executor executor;
    @Value("#{'${website.urls}'.split(',')}")
    private List<String> urls;
    @Autowired
    private RestTemplate restTemplate;
    private String urlSearch;

    private static final String PRICE = "new-price";

    private static final String APPLICATION = "Application";

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
            Element element = document.getElementsByClass(PropertiesReader.getProperties(PathHolder.DL.getPath())).first();
            sparePartList.addAll(extractFromElementList(element));
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(PropertiesReader.getProperties("ukrPartsEx"),
                    "Exception occurred in extractDataFromUkrparts: " + e.getMessage(), e);
        }
        return sparePartList;
    }

    private List<SparePart> extractFromElementList(Element element) {
        List<SparePart> sparePartList = new ArrayList<>();
        try {
            Elements elementsName = element.getElementsByTag(PathHolder.A.getPath());
            Elements elementsDescription = element.getElementsByTag(PropertiesReader.getProperties(PathHolder.STRONG.getPath()));
            int elementDescription = 0;
            for (int i = 0; i < elementsName.size() - 1; i += 2) {
                String url = elementsName.get(i).attr(PathHolder.HREF.getPath());
                String description = elementsDescription.get(elementDescription).text();
                elementDescription++;
                if (StringUtils.hasText(description)) {
                    SparePart sparePart = new SparePart();
                    sparePart.setDescription(description);
                    sparePart.setUrl(url);
                    sparePartList.add(sparePart);
                }
            }
            CostFetcher costFetcher = new CostFetcher(restTemplate, executor, PRICE, sparePartList);
            for (int i = 0; i < sparePartList.size(); i++) {
                costFetcher.getCost(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(PropertiesReader.getProperties("ukrPartsEx"),
                    "extractFromElementList" + e.getMessage(), e);
        }
        return sparePartList;
    }

    private String getUrl() {
        return urls.stream().filter(urls -> urls.contains(urlSearch)).findFirst().get();
    }
}
