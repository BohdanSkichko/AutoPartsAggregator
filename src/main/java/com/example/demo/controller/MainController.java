package com.example.demo.controller;

import com.example.demo.entity.SerialNumber;
import com.example.demo.helper.FileChooser;
import com.example.demo.service.impl.MainServiceImp;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;


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
    private ModelAndView save(@RequestParam String cost,
                              @RequestParam String description,
                              @RequestParam String url) throws IOException {
//        System.setProperty("java.awt.headless", "false");
        File file = new File(description + ".txt");
        OutputStream outputStream = Files.newOutputStream(file.toPath());

//        FileChooser fileChooser = new FileChooser();
//        String yourFile = fileChooser.getFile();

//        JFileChooser saveToDirectory = new JFileChooser();
//        saveToDirectory.setCurrentDirectory(new File
//                (System.getProperty("user.home") + System.getProperty("file.separator") + "Desktop"));

        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("Cost: " + cost + "\n");
        fileWriter.write("Url: " + url + "\n");
        fileWriter.close();
        outputStream.close();
        ModelAndView modelAndView = new ModelAndView("saveSparePart");
        modelAndView.addObject("description", description);


        return modelAndView;
    }

}
