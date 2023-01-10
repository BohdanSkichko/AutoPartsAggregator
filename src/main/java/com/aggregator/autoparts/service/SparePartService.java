package com.aggregator.autoparts.service;

import com.aggregator.autoparts.dto.Response;


public interface SparePartService {
    Response searchSparePartBySerialNumber(String serialNumber);
}
