package com.example.demo.service;

import com.example.demo.entity.Response;


public interface SparePartService {
    Response searchSparePartBySerialNumber(String serialNumber);
}
