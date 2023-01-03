package com.example.car_spare_parts_finder.service;

import com.example.car_spare_parts_finder.dto.Response;


public interface SparePartService {
    Response searchSparePartBySerialNumber(String serialNumber);
}
