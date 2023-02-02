package com.aggregator.autoparts.service.impl;

import com.aggregator.autoparts.dto.Response;
import com.aggregator.autoparts.dto.SparePart;
import com.aggregator.autoparts.helper.UserExcelExporter;
import com.aggregator.autoparts.service.SparePartService;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
        return result;
    }

    private List<Response> interrogateRemoteHosts(String serialNumber) {
        List<SparePartService> servicesToCall = Arrays.asList(avtozapchastiService, avtoProService,
                avtoPlusService, demexUaService, existUaService, ukrPartsService);
        return servicesToCall.parallelStream()
                .map(s -> (s.searchSparePartBySerialNumber(serialNumber)))
                .unordered()
                .collect(Collectors.toList());
    }

    public ResponseEntity<InputStreamResource> exportToExcel(List<SparePart> sparePartList) {
        ResponseEntity<InputStreamResource> result;
        ContentDisposition contentDisposition = ContentDisposition.builder(ATTACHMENT)
                .filename(EXCEL_FILE_NAME, StandardCharsets.UTF_8)
                .build();
        UserExcelExporter userExcelExporter = new UserExcelExporter(sparePartList);
        try {
            result = ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .headers(httpHeaders -> httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString()))
                    .headers(httpHeaders -> httpHeaders.add("X-Accel-Buffering", "no"))
                    .body(new InputStreamResource(userExcelExporter.export()));
        } catch (IOException e) {
            log.error(e.getMessage() +
                    " when trying to save a file on client side " + e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return result;
    }
}
