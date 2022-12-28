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
@EqualsAndHashCode
@Slf4j
public class DemexUaService implements SparePartService {

    @Autowired
    private SiteParser siteParser;

    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        return siteParser.searchSparePartBySerialNumber(serialNumber,
                PropertiesReader.getProperties(PathHolder.URL_DEMEX_UA.getPath()),
                BusinessNameHolder.NEW_PRICE.getPath());
    }
}
