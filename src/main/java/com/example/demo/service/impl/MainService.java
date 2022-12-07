package com.example.demo.service.impl;

import com.example.demo.entity.Response;
import com.example.demo.helper.PropertiesReader;
import com.example.demo.service.SparePartService;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

@Service
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@EqualsAndHashCode
public class MainService implements SparePartService {
    @Autowired
    private AvtoProService avtoProService;
    @Autowired
    private AvtoPlusService avtoPlusService;
    @Autowired
    private UkrPartsService ukrPartsService;
    @Autowired
    private ExistUaService existUaService;
    @Autowired
    private AvtozapchastiService avtozapchastiService;
    @Autowired
    private DemexUaService demexUaService;
    @Autowired
    private Executor executor;

    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        Response result = new Response();
        List<Response> listResponse = interrogateRemoteHosts(serialNumber);
        for (Response response : listResponse) {
            result.getSparePartList().addAll(response.getSparePartList());
        }
        return result;
    }

    private List<Response> interrogateRemoteHosts(String serialNumber) {
        List<SparePartService> servicesToCall = new ArrayList<>();
        servicesToCall.add(avtoPlusService);
        servicesToCall.add(avtoProService);
        servicesToCall.add(ukrPartsService);
        servicesToCall.add(existUaService);
        servicesToCall.add(demexUaService);
        servicesToCall.add(avtozapchastiService);
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

    private InputStreamResource getResource(File file) throws IOException {
        return new InputStreamResource(Files.newInputStream(file.toPath()));

    }

    public ResponseEntity<InputStreamResource> saveFileClientSide(File file) {
        ResponseEntity<InputStreamResource> result = null;
        ContentDisposition contentDisposition = ContentDisposition.builder("ATTACHMENT")
                .filename(file.getName(), StandardCharsets.UTF_8)
                .build();
        try {
            result = ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .headers(httpHeaders -> httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString()))
                    .body(getResource(file));
        } catch (IOException e) {
            log.error("MainServiceImp thrown Exception: " + e.getMessage() +
                    " when trying to save a file on client side " + file.getName().trim());
        } finally {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                log.error("MainServiceImp thrown Exception: " + e.getMessage() +
                        " when trying to delete a file: " + file.getName());
            }
        }
        return result;
    }

    public File createFile(String cost, String fileName, String url) {
        File file = null;
        try {
            file = new File(fileName.trim() + PropertiesReader.getProperties("txt"));
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(PropertiesReader.getProperties("Cost") + cost + "\n");
            fileWriter.write(PropertiesReader.getProperties("Url") + url + "\n");
            fileWriter.close();
        } catch (IOException e) {
            log.error("MainServiceImp throw Exception " + e.getMessage() + " when trying create file: " + fileName.trim());
        }
        return file;
    }
}
