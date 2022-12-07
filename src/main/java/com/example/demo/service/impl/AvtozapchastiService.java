package com.example.demo.service.impl;

import com.example.demo.entity.Response;
import com.example.demo.helper.PathHolder;
import com.example.demo.helper.PropertiesReader;
import com.example.demo.helper.SiteParser;
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
    private SiteParser myParser = new SiteParser();
    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        myParser.setUrlSearch(PropertiesReader.getProperties(PathHolder.URL_AVTOZAPCHASTI.getPath()));
        return myParser.searchSparePartBySerialNumber(serialNumber);
    }
}
