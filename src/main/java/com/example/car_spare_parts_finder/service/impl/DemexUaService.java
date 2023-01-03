package com.example.car_spare_parts_finder.service.impl;

import com.example.car_spare_parts_finder.dto.Response;
import com.example.car_spare_parts_finder.string_enum.BusinessNameHolder;
import com.example.car_spare_parts_finder.helper.SiteParser;
import com.example.car_spare_parts_finder.service.SparePartService;
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
        return siteParser.searchSparePartBySerialNumber(serialNumber, url, BusinessNameHolder.NEW_PRICE.getPath());
    }
}
