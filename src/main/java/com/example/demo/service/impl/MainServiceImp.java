package com.example.demo.service.impl;

import com.example.demo.entity.Response;
import com.example.demo.helper.PropertiesReader;
import com.example.demo.service.SparePartService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class MainServiceImp implements SparePartService {
    @Autowired
    private AvtoProServiceImp avtoProServiceImp;
    @Autowired
    private AvtoPlusServiceImp avtoPlusServiceImp;
    @Autowired
    private UkrPartsServiceImp ukrPartsServiceImp;

    @Override
    public Response searchSparePartBySerialNumber(String serialNumber)  {
        List<Response> listResponse = interrogateRemoteHosts(serialNumber);
        Response result = new Response();
        for (Response response : listResponse) {
            result.getSparePartList().addAll(response.getSparePartList());
        }
        return result;
    }

    private List<Response> interrogateRemoteHosts(String serialNumber) {
        List<SparePartService> servicesToCall = new ArrayList<>();
        servicesToCall.add(avtoPlusServiceImp);
        servicesToCall.add(avtoProServiceImp);
        servicesToCall.add(ukrPartsServiceImp);
        List<Response> responses = new ArrayList<>();
        for (SparePartService sparePartService : servicesToCall) {
            try {
                responses.add(sparePartService.searchSparePartBySerialNumber(serialNumber));
            } catch (Exception e) {
                log.error("Service " + sparePartService + " thrown Exception: "
                        + e.getMessage() + ". Skipping the error and call others.");
            }
        }
        return responses;
    }
    public ResponseEntity<InputStreamResource> saveInfoToFileTxt(String cost, String description, String url) {
        File file = null;
        ContentDisposition contentDisposition = null;
        InputStreamResource resource = null;
        try {
            file = new File(description + PropertiesReader.getProperties("txt"));
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(PropertiesReader.getProperties("Cost") + cost + "\n");
            fileWriter.write(PropertiesReader.getProperties("Url") + url + "\n");
            resource = new InputStreamResource(Files.newInputStream(file.toPath()), ".txt");
            fileWriter.close();
            contentDisposition = ContentDisposition.builder(PropertiesReader.getProperties("attachment"))
                    .filename(file.getName(), StandardCharsets.UTF_8)
                    .build();
        } catch (IOException e) {
            log.error("MainController thrown Exception: " + e.getMessage() + " when trying to save a file " + description);
        }
        ContentDisposition finalContentDisposition = contentDisposition;
        return ResponseEntity.ok()
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers(httpHeaders -> httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, finalContentDisposition.toString()))
                .body(resource);
    }
}
