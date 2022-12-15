package com.example.demo.service;

import com.example.demo.entity.Response;

import java.util.concurrent.CompletableFuture;


public interface SparePartService {
    Response searchSparePartBySerialNumber(String serialNumber);
}
