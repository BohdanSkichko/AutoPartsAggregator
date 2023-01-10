package com.aggregator.autoparts.helper;

import com.aggregator.autoparts.dto.Response;
import com.aggregator.autoparts.exception.BusinessException;
import com.aggregator.autoparts.helper.enumeration.HttpElHolder;
import com.aggregator.autoparts.dto.SparePart;
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
    @Autowired
    private CostFetcher costFetcher;
    @Value("#{'${website.urls}'.split(',')}")
    private List<String> urls;


    public Response searchSparePartBySerialNumber(String serialNumber, String url, String className) {
        return callRemoteHost(serialNumber, url,className).join();
    }

    private CompletableFuture<Response> callRemoteHost(String serialNumber, String url, String className) {
        return CompletableFuture.supplyAsync(
                () -> {
                    log.info("find spare parts " + getClass().getTypeName() + " " + Thread.currentThread().getName());
                    try {
                        Response result = new Response();
                        result.getSparePartList().addAll(extractData(serialNumber, url, className));
                        return result;
                    } catch (Exception e) {
                        log.error(e.getMessage() + " when trying to call remote host " + e);
                        throw new BusinessException(e.getMessage(), e);
                    }
                }, executor);
    }

    private List<SparePart> extractData(String serialNumber, String url, String className) {
        List<SparePart> sparePartList = new ArrayList<>();
        try {
            Document document = Jsoup.connect(getUrl(url) + serialNumber).get();
            Element element = document.getElementsByClass(HttpElHolder.DL_HORIZONTAL.getPath()).first();
            if (element != null) sparePartList.addAll(extractFromElementList(element, className));
        } catch (Exception e) {
            log.error(e.getMessage() + " when trying to extract data " + e);
            throw new BusinessException(e.getMessage(), e);
        }
        return sparePartList;
    }

    private List<SparePart> extractFromElementList(Element element, String className) {
        List<SparePart> sparePartList = new ArrayList<>();
        try {
            Elements elementNames = element.getElementsByTag(HttpElHolder.A.getPath());
            Elements elementDescriptions = element.getElementsByTag(HttpElHolder.STRONG.getPath());
            int descriptionIndex = 0;
            for (int i = 0; i < elementNames.size() - 1; i += 2) {
                String url = elementNames.get(i).attr(HttpElHolder.HREF.getPath());
                String description = elementDescriptions.get(descriptionIndex).text();
                descriptionIndex++;
                if (StringUtils.hasText(description)) {
                    SparePart sparePart = new SparePart();
                    sparePart.setDescription(description);
                    sparePart.setUrl(url);
                    sparePartList.add(sparePart);
                }
            }
            sparePartList = costFetcher.setCostFromRemoteHost(sparePartList, className);
        } catch (Exception e) {
            log.error(e.getMessage() + " when trying to extract spare part from element list " + e);
            throw new BusinessException(e.getMessage(), e);
        }
        return sparePartList;
    }

    private String getUrl(String url) {
        return urls.stream().filter(urls -> urls.contains(url)).findFirst().get();
    }
}
