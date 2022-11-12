package com.example.demo.service;

import com.example.demo.entity.Response;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.concurrent.ExecutionException;


public interface SparePartService {
    Response searchSparePartBySerialNumber(String serialNumber);
}
