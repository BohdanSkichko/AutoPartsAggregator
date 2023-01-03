package com.carspareparts.finder.service.impl;

import com.carspareparts.finder.dto.Response;
import com.carspareparts.finder.helper.SiteParser;
import com.carspareparts.finder.helper.stringenumholder.BusinessNameHolder;
import com.carspareparts.finder.service.SparePartService;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@EqualsAndHashCode
public class AvtozapchastiService implements SparePartService {
    @Autowired
    private SiteParser siteParser;
    @Value("#{'${Avtozapchasti}'}")
    private String url;

    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        Response response = siteParser.searchSparePartBySerialNumber(serialNumber, url,
                BusinessNameHolder.NEW_PRICE_PULL.getPath());
        log.debug("AVTOZAPCHASTI: " + response.getSparePartList());
        return response;
    }
}
