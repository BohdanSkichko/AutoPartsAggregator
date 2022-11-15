package com.example.demo.service;

import com.example.demo.entity.Response;
import com.example.demo.exeptionhendler.BusinessHandledException;


public interface SparePartService {
    Response searchSparePartBySerialNumber(String serialNumber) throws BusinessHandledException;
}
