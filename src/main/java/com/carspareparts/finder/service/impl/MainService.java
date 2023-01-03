package com.carspareparts.finder.service.impl;

import com.carspareparts.finder.dto.Response;
import com.carspareparts.finder.dto.SparePart;
import com.carspareparts.finder.helper.UserExcelExporter;
import com.carspareparts.finder.service.SparePartService;
import com.carspareparts.finder.helper.stringenumholder.WordAndPunctuationHolder;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
    private final static String EXCEL_FILE_NAME = "Spare Parts.xlsx";
    private final static String ATTACHMENT = "ATTACHMENT";
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
        List<SparePartService> servicesToCall = Arrays.asList(avtozapchastiService, avtoProService,
                avtoPlusService, demexUaService, existUaService, ukrPartsService);
        List<Response> list = servicesToCall.parallelStream()
                .map(s -> (s.searchSparePartBySerialNumber(serialNumber)))
                .unordered()
                .collect(Collectors.toList());
        log.debug("List Responses: " + list);
        return list;
    }

    public ResponseEntity<InputStreamResource> saveFileClientSide(String cost, String fileName, String url) {
        ResponseEntity<InputStreamResource> result;
        ContentDisposition contentDisposition = ContentDisposition.builder(ATTACHMENT)
                .filename(fileName + WordAndPunctuationHolder.TXT.getPath(), StandardCharsets.UTF_8)
                .build();
        try {
            String content = WordAndPunctuationHolder.COST.getPath() + cost + "\n" +
                    WordAndPunctuationHolder.URL + url + "\n";
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
            log.error(e.getMessage() +
                    " when trying to save a file on client side " + e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return result;
    }

    public ResponseEntity<InputStreamResource> saveDataToExelClientSide(List<SparePart> sparePartList) {
        ResponseEntity<InputStreamResource> result;
        ContentDisposition contentDisposition = ContentDisposition.builder(ATTACHMENT)
                .filename(EXCEL_FILE_NAME, StandardCharsets.UTF_8)
                .build();
        UserExcelExporter userExcelExporter = new UserExcelExporter(sparePartList);
        try {
            result = ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .headers(httpHeaders -> httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString()))
                    .body(new InputStreamResource(userExcelExporter.export()));
        } catch (IOException e) {
            log.error(e.getMessage() +
                    " when trying to save a file on client side " + e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return result;
    }
}
