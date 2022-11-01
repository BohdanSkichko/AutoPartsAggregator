package com.example.demo.controller;


import com.example.demo.dto.Response;
import com.example.demo.service.SparePartService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/api")
public class MainController {
    @Autowired
    SparePartService spareService;

    @GetMapping(path = "/{serialNumber}")
    @ApiOperation(value = "Get price and reference",
            notes = "Find spare part by SN")
    @ApiImplicitParam(name = "serialNumber", required = true,
            dataType = "String")
    @ApiResponses({
            @ApiResponse(code = 200, message = "ok"),
            @ApiResponse(code = 400, message = "bad request syntax"),
            @ApiResponse(code = 404, message = "not found")
    })
    public CompletableFuture<Response> getSparePartBySerialNumber(@PathVariable String serialNumber)
            throws ExecutionException, InterruptedException {
        return spareService.searchSparePartBySerialNumber(serialNumber);
    }
}
