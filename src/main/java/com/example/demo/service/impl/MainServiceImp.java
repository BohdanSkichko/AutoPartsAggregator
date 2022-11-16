package com.example.demo.service.impl;

import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.exeptionhendler.BusinessHandledException;
import com.example.demo.service.SparePartService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class MainServiceImp implements SparePartService {
    @Autowired
    private AvtoProServiceImp avtoProServiceImp;
    @Autowired
    private AvtoPlusServiceImp avtoPlusServiceImp;
    @Autowired
    private UkrPartsServiceImp ukrPartsServiceImp;

    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) throws BusinessHandledException {
        List<Response> listResponse = interrogateRemoteHosts(serialNumber);
        Response result = new Response();
        for (Response response : listResponse) {
            result.getSparePartList().addAll(response.getSparePartList());
        }
//        sortByCost(result);
        return result;
    }


    private List<Response> interrogateRemoteHosts(String serialNumber) throws BusinessHandledException {
        Response avtoPlus = avtoPlusServiceImp.searchSparePartBySerialNumber(serialNumber);
        Response avtoPro = avtoProServiceImp.searchSparePartBySerialNumber(serialNumber);
        Response ukrParts = ukrPartsServiceImp.searchSparePartBySerialNumber(serialNumber);
        return Stream.of(avtoPlus, avtoPro, ukrParts).collect(Collectors.toList());
    }


    private void sortByCost(Response result) {
        List<SparePart> sortByCost = result.getSparePartList().stream().sorted(Comparator.comparingInt(SparePart::getCost))
                .collect(Collectors.toList());
        result.setSparePartList(sortByCost);
    }
}
