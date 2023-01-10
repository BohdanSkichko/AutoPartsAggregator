package com.aggregator.autoparts.service.impl;

import com.aggregator.autoparts.dto.Response;
import com.aggregator.autoparts.helper.SiteParser;
import com.aggregator.autoparts.helper.enumeration.HttpElHolder;
import com.aggregator.autoparts.service.SparePartService;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@EqualsAndHashCode
@Slf4j
public class DemexUaService implements SparePartService {

    @Autowired
    private SiteParser siteParser;
    @Value("#{'${DemexUa}'}")
    private String url;

    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        return siteParser.searchSparePartBySerialNumber(serialNumber, url, HttpElHolder.NEW_PRICE.getPath());
    }
}
