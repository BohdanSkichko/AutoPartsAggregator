package com.example.demo.service.impl;

import com.example.demo.entity.Response;
import com.example.demo.helper.PathHolder;
import com.example.demo.helper.PropertiesReader;
import com.example.demo.helper.SiteParser;
import com.example.demo.service.SparePartService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@EqualsAndHashCode
@Slf4j
public class DemexUaService implements SparePartService {
    @Autowired
    private SiteParser myParser = new SiteParser();

    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        myParser.setUrlSearch(PropertiesReader.getProperties(PathHolder.URL_DEMEX_UA.getPath()));
        return myParser.searchSparePartBySerialNumber(serialNumber);
    }
}
