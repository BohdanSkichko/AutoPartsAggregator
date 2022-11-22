package com.example.demo.controller;

import com.example.demo.entity.SerialNumber;
import com.example.demo.service.impl.MainServiceImp;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/api")
public class MainController {

    @Autowired
    private final MainServiceImp mainServiceImp;

    @GetMapping(value = "/search")
    @ApiOperation(value = "Get price and reference",
            notes = "Find spare part by SN. For best results, enter the serial number or spare part number")
    @ApiImplicitParam(name = "serialNumber", required = true,
            dataType = "String")
    @ApiResponses({
            @ApiResponse(code = 200, message = "ok"),
            @ApiResponse(code = 400, message = "bad request syntax"),
            @ApiResponse(code = 404, message = "not found")
    })
    public ModelAndView getSparePartBySerialNumber(@ModelAttribute("serialNumber") String serialNumber) {
        ModelAndView result = new ModelAndView("spare-part");
        result.addObject("spare", mainServiceImp.searchSparePartBySerialNumber(serialNumber).getSparePartList());
        return result;
    }

    @GetMapping(path = {"index", "/"})
    private ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView("index");
        SerialNumber serialNumber = new SerialNumber();
        modelAndView.addObject("serialNumber", serialNumber);
        return modelAndView;
    }

    @GetMapping(path = "save")
    private ResponseEntity<InputStreamResource> saveResultClientSide(@RequestParam String cost,
                                                                     @RequestParam String description,
                                                                     @RequestParam String url) {
        return mainServiceImp.saveInfoClientSide(cost, description, url);
    }
}
