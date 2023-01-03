package com.carspareparts.finder.service;

import com.carspareparts.finder.dto.Response;


public interface SparePartService {
    Response searchSparePartBySerialNumber(String serialNumber);
}
