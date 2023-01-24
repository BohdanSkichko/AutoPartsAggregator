package com.aggregator.autoparts.controller;

import com.aggregator.autoparts.dto.Response;
import com.aggregator.autoparts.dto.SerialNumber;
import com.aggregator.autoparts.service.impl.MainService;
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

@Slf4j
@RestController
@AllArgsConstructor
@SessionAttributes("response")
@RequestMapping(path = "/")
public class MainController {
    @Autowired
    private final MainService mainService;

    @GetMapping(value = "search")
    @ApiOperation(value = "Get price and reference",
            notes = "Find spare part by SN. For best results, enter the serial number or spare part number")
    @ApiImplicitParam(name = "serialNumber", required = true,
            dataType = "String")
    @ApiResponses({
            @ApiResponse(code = 200, message = "ok"),
            @ApiResponse(code = 400, message = "bad request syntax"),
            @ApiResponse(code = 404, message = "not found")
    })
    public ModelAndView getSparePartBySerialNumber(@ModelAttribute("serialNumber") String serialNumber,
                                                   @ModelAttribute("response") Response response

    ) {
        ModelAndView result = new ModelAndView("spare-part");
        try {
            response = mainService.searchSparePartBySerialNumber(serialNumber);
        } catch (Exception e) {
            response.setError(e.getMessage());
        }
        result.addObject("response", response);
        return result;
    }

    @ModelAttribute("response")
    public Response createResponse() {
        return new Response();
    }

    @GetMapping(path = {"index", "/"})
    private ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView("index");
        SerialNumber serialNumber = new SerialNumber();
        modelAndView.addObject("serialNumber", serialNumber);
        return modelAndView;
    }

    @PostMapping(path = "saveToXlsx")
    private ResponseEntity<InputStreamResource> exportToExcel(@ModelAttribute("response") Response response) {
        log.debug("ExportToExcel Response: " + response);
        return mainService.exportToExcel(response.getSparePartList());
    }
}
