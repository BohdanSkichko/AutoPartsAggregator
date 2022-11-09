package com.example.demo.service;

import com.example.demo.dto.Response;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public interface SparePartService {
    Response searchSparePartBySerialNumber(String serialNumber) throws ExecutionException, InterruptedException;

}
