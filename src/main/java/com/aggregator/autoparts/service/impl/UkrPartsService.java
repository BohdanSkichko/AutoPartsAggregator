package com.aggregator.autoparts.service.impl;

import com.aggregator.autoparts.dto.Response;
import com.aggregator.autoparts.dto.SparePart;
import com.aggregator.autoparts.exception.BusinessException;
import com.aggregator.autoparts.helper.UrlHolder;
import com.aggregator.autoparts.helper.enumeration.HttpElHolder;
import com.aggregator.autoparts.helper.enumeration.RegexHolder;
import com.aggregator.autoparts.service.SparePartService;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Slf4j
public class UkrPartsService implements SparePartService {
    @Autowired
    private Executor executor;
    @Autowired
    private UrlHolder urlHolder;
    @Value("#{'${UrlUrkParts}'}")
    private String url;
    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        return callRemoteHost(serialNumber).join();
    }

    private CompletableFuture<Response> callRemoteHost(String serialNumber) {
        return CompletableFuture.supplyAsync(
                () -> {
                    log.info("find spare parts UKR PARTS " + Thread.currentThread().getName());
                    try {
                        Response response = new Response();
                        response.getSparePartList().addAll(extractDataFromUkrparts(serialNumber));
                        return response;
                    } catch (Exception e) {
                        log.error(e.getMessage() + " when trying to call remote host " + e);
                        throw new BusinessException(e.getMessage(),e);
                    }
                }, executor);
    }

    private List<SparePart> extractDataFromUkrparts(String serialNumber) {
        List<SparePart> sparePartList = new ArrayList<>();
        try {
            Document document = Jsoup.connect(urlHolder.getUrl(url) + serialNumber).get();
            List<Element> listElement = new ArrayList<>(document
                    .getElementsByClass(HttpElHolder.PART_BOX_WRAPPER.getPath()));
            sparePartList.addAll(extractFromElementList(listElement));
        } catch (Exception e) {
            log.error(e.getMessage() + " when trying to extract data " + e);
            throw new BusinessException(e.getMessage(), e);
        }
        return sparePartList;
    }

    private List<SparePart> extractFromElementList(List<Element> listElement) {
        List<SparePart> sparePartList = new ArrayList<>();
        try {
            for (Element e : listElement) {
                Elements elementsName = e.
                        getElementsByClass(HttpElHolder.PART_ARTICLE.getPath());
                SparePart sparePart = new SparePart();
                sparePart.setDescription(elementsName.text());
                Elements elementPrice = e.
                        getElementsByClass(HttpElHolder.PRICE_MIN.getPath());
                String text = elementPrice.text();
                String cost = text.replaceAll(RegexHolder.REPLACE_TEXT.getPath(), "");
                if (cost.isEmpty()) {
                    break;
                }
                sparePart.setCost(Double.parseDouble(cost));
                Elements elementsURL = e.getElementsByTag(HttpElHolder.A.getPath());
                if (StringUtils.hasText(elementsURL.attr(HttpElHolder.HREF.getPath()))) {
                    sparePart.setUrl(url +
                            elementsURL.attr(HttpElHolder.HREF.getPath()));
                    sparePartList.add(sparePart);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage() + " when trying to extract spare part from Element list " + e);
            throw new BusinessException(e.getMessage(), e);
        }
        return sparePartList;
    }
}
