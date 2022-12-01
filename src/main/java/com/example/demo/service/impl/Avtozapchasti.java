package com.example.demo.service.impl;

import com.example.demo.entity.Response;
import com.example.demo.helper.PropertiesReader;
import com.example.demo.service.SparePartService;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.jsoup.parser.CharacterReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.stream.events.Characters;
import java.util.concurrent.Executor;

@Service
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@EqualsAndHashCode
public class Avtozapchasti implements SparePartService {
    @Autowired
    private DemexUaImp demexUaImp;
    @Autowired
    private Executor executor;

    @Override
    public Response searchSparePartBySerialNumber(String serialNumber) {
        demexUaImp.setUrlSearch(PropertiesReader.getProperties("Avtozapchasti"));
        demexUaImp.setExecutor(executor);
        return demexUaImp.searchSparePartBySerialNumber(serialNumber);
    }
}
