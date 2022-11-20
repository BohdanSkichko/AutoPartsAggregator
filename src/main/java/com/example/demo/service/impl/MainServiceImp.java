package com.example.demo.service.impl;

import com.example.demo.entity.Response;
import com.example.demo.service.SparePartService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
