package com.example.demo.service.impl;

import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.service.SparePartService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class MainServiceImp implements SparePartService {
    @Autowired
    Executor executor;
    @Autowired
    AvtoProServiceImp avtoProServiceImp;
    @Autowired
    AvtoPlusServiceImp avtoPlusServiceImp;
    @Autowired
    UkrPartsServiceImp ukrPartsServiceImp;

    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        List<Response> listResponse = getResponseList(serialNumber);
        Response result = new Response();
        for (Response response : listResponse) {
            result.getSparePartList().addAll(response.getSparePartList());
        }
        int idSparePart = 1;
        for (int i = 0; i < result.getSparePartList().size(); i++) {
            result.getSparePartList().get(i).setId(idSparePart);
            result.getSparePartList().get(i).setSerialNumber(serialNumber);
            idSparePart++;
        }
        sortByCost(result);
        return result;
    }


    private List<Response> getResponseList(String serialNumber) {
      Response response1 = avtoPlusServiceImp.searchSparePartBySerialNumber(serialNumber);
      Response response2 = avtoProServiceImp.searchSparePartBySerialNumber(serialNumber);
      Response response3 = ukrPartsServiceImp.searchSparePartBySerialNumber(serialNumber);
      return Stream.of(response1,response2,response3).collect(Collectors.toList());
    }


    private void sortByCost(Response result) {
        List<SparePart> sortByCost = result.getSparePartList().stream().sorted(Comparator.comparingInt(SparePart::getCost))
                .collect(Collectors.toList());
        result.setSparePartList(sortByCost);
    }
}
