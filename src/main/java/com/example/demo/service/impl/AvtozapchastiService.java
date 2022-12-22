package com.example.demo.service.impl;

import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.helper.PathHolder;
import com.example.demo.helper.PropertiesReader;
import com.example.demo.helper.SiteParser;
import com.example.demo.service.SparePartService;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.Executor;

@Service
@Slf4j
@EqualsAndHashCode
public class AvtozapchastiService implements SparePartService {
    @Autowired
    private Executor executor;
    @Autowired
    private RestTemplate restTemplate;
    @Value("#{'${website.urls}'.split(',')}")
    private List<String> urls;

    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        SiteParser myParser = new SiteParser(executor,restTemplate,urls
                ,PropertiesReader.getProperties(PathHolder.URL_AVTOZAPCHASTI.getPath()));
        return myParser.searchSparePartBySerialNumber(serialNumber);
    }
}
