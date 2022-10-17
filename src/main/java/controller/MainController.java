package controller;


import dto.Response;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import service.SparePartService;

import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/api")
public class MainController {
    @Autowired
    SparePartService scraperService;

    @GetMapping(path = "/{serialNumber}")
//    @ApiOperation(value = "Get price and reference",
//            notes = "Find spare part by SN")
//    @ApiImplicitParam(name = "serialNumber", required = true,
//            dataType = "String")
//    @ApiResponses({
//            @ApiResponse(code = 400, message = "bad request syntax"),
//            @ApiResponse(code = 404, message = "not ewa")
//    })
    public Set<Response> getSparePartBySerialNumber(@PathVariable String serialNumber) {
        return  scraperService.getSparePartBySerialNumber(serialNumber);
    }
}
