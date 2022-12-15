package com.example.demo.service.impl;

import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.helper.PathHolder;
import com.example.demo.helper.PropertiesReader;
import com.example.demo.helper.UserExcelExporter;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

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

    private final static String NAME_EXCEL = "Spare Parts.xlsx";
    @Autowired
    private Executor executor;


    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        Response result = new Response();
        List<Response> listResponse = interrogateRemoteHosts(serialNumber);
        for (Response response : listResponse) {
            result.getSparePartList().addAll(response.getSparePartList());
        }
        List<SparePart> withCost = result.getSparePartList().stream()
                .filter(sparePart -> sparePart.getCost() != 0.0)
                .sorted(Comparator.comparingDouble(SparePart::getCost))
                .collect(Collectors.toList());
        result.setSparePartList(withCost);
        log.debug("MainService searchSparePartBySerialNumber sorted result: " + withCost);
        result.setSparePartList(withCost);
        return result;
    }

    public List<Response> interrogateRemoteHosts(String serialNumber) {
        List<SparePartService> servicesToCall =Arrays.asList(avtozapchastiService, avtoProService,
                avtoPlusService, demexUaService, existUaService, ukrPartsService);
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

    public ResponseEntity<InputStreamResource> saveFileClientSide(String cost, String fileName, String url) {
        ResponseEntity<InputStreamResource> result = null;
        ContentDisposition contentDisposition = ContentDisposition.builder("ATTACHMENT")
                .filename(fileName + ".txt", StandardCharsets.UTF_8)
                .build();
        try {
            String content = PropertiesReader.getProperties(PathHolder.COST.getPath()) + cost + "\n" +
                    PropertiesReader.getProperties(PathHolder.URL.getPath()) + url + "\n";
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(content.getBytes().length);
            outputStream.write(content.getBytes());
            outputStream.close();
            byte[] bytes = outputStream.toByteArray();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            result = ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .headers(httpHeaders -> httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString()))
                    .body(new InputStreamResource(inputStream));
        } catch (IOException e) {
            log.error("MainServiceImp thrown Exception: " + e.getMessage() +
                    " when trying to save a file on client side");
        }
        return result;
    }

    public ResponseEntity<InputStreamResource> saveDataToExelClientSide(List<SparePart> sparePartList) {
        ResponseEntity<InputStreamResource> result = null;
        ContentDisposition contentDisposition = ContentDisposition.builder("ATTACHMENT")
                .filename(NAME_EXCEL, StandardCharsets.UTF_8)
                .build();
        UserExcelExporter userExcelExporter = new UserExcelExporter(sparePartList);
        try {
            result = ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .headers(httpHeaders -> httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString()))
                    .body(new InputStreamResource(userExcelExporter.export()));
        } catch (IOException e) {
            log.error("MainServiceImp thrown Exception: " + e.getMessage() +
                    " when trying to save a file on client side");
        }
        return result;
    }
}
