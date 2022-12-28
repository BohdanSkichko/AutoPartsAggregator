package com.example.demo.service.impl;

import com.example.demo.entity.Response;
import com.example.demo.helpers.BusinessNameHolder;
import com.example.demo.helpers.PathHolder;
import com.example.demo.helpers.PropertiesReader;
import com.example.demo.helpers.SiteParser;
import com.example.demo.service.SparePartService;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@EqualsAndHashCode
public class AvtozapchastiService implements SparePartService {
    @Autowired
    private SiteParser siteParser;

    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        Response response = siteParser.searchSparePartBySerialNumber(serialNumber,
                PropertiesReader.getProperties(PathHolder.URL_AVTOZAPCHASTI.getPath()),
                BusinessNameHolder.NEW_PRICE_PULL.getPath());
        log.debug("AVTOZAPCHASTI: " + response.getSparePartList());
        return response;
    }
}
