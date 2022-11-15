package com.example.demo.controller;


import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.exeptionhendler.BusinessHandledException;
import com.example.demo.service.impl.MainServiceImp;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/api")
public class MainController {

    @Autowired
    private final MainServiceImp mainServiceImp;

    @GetMapping(path = "/")
    public String index(Model model) {
        return "index";
    }

    @GetMapping(value = "/{serialNumber}")
    @ApiOperation(value = "Get price and reference",
            notes = "Find spare part by SN. For best results, enter the serial number or spare part number")
    @ApiImplicitParam(name = "serialNumber", required = true,
            dataType = "String")
    @ApiResponses({
            @ApiResponse(code = 200, message = "ok"),
            @ApiResponse(code = 400, message = "bad request syntax"),
            @ApiResponse(code = 404, message = "not found")
    })
    public ModelAndView getSparePartBySerialNumber(@PathVariable String serialNumber) throws BusinessHandledException {
        ModelAndView result = new ModelAndView("spare-part");
        result.addObject("spare", mainServiceImp.searchSparePartBySerialNumber(serialNumber).getSparePartList());
        return result;
    }
}
