package com.example.demo.controller;


import com.example.demo.entity.Response;
import com.example.demo.service.impl.AvtoProServiceImp;
import com.example.demo.service.SparePartService;
import com.example.demo.service.impl.MainServiceImp;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/api")
public class MainController {


    @Autowired
    MainServiceImp mainServiceImp;

    @GetMapping(path = "/{serialNumber}")
    @ApiOperation(value = "Get price and reference",
            notes = "Find spare part by SN. For best results, enter the serial number or spare part number")
    @ApiImplicitParam(name = "serialNumber", required = true,
            dataType = "String")
    @ApiResponses({
            @ApiResponse(code = 200, message = "ok"),
            @ApiResponse(code = 400, message = "bad request syntax"),
            @ApiResponse(code = 404, message = "not found")
    })
    public Response getSparePartBySerialNumber(@PathVariable String serialNumber) {
        return mainServiceImp.searchSparePartBySerialNumber(serialNumber);
    }

}
