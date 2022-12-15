package com.example.demo.services;

import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.service.SparePartService;
import com.example.demo.service.impl.AvtoPlusService;
import com.example.demo.service.impl.AvtoProService;
import com.example.demo.service.impl.MainService;
import com.example.demo.service.impl.UkrPartsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;

import java.io.File;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MainServiceTest {
    @Spy
    @InjectMocks
    private SparePartService sparePartService = new MainService();
    @Mock
    private AvtoProService avtoProService;
    @Mock
    private AvtoPlusService avtoPlusService;
    @Mock
    private UkrPartsService ukrPartsService;


    @Test
    public void returnResponseWhenSearchSparePart() {
        String serialNumber = "2002";
        Response responseAvtopro = new Response();

        SparePart first = new SparePart();
        first.setUrl("Url");
        first.setCost(0.0);
        first.setDescription("Name");

        SparePart second = new SparePart();
        second.setUrl("Url");
        second.setCost(0.0);
        second.setDescription("Description");

        responseAvtopro.getSparePartList().add(first);
        responseAvtopro.getSparePartList().add(second);

        Response responseUkrParts = new Response();
        responseUkrParts.getSparePartList().add(first);

        Response responseAvtoPlus = new Response();
        responseAvtoPlus.getSparePartList().add(second);

        Response mainResponse = new Response();
        mainResponse.getSparePartList().addAll(responseAvtoPlus.getSparePartList());
        mainResponse.getSparePartList().addAll(responseAvtopro.getSparePartList());
        mainResponse.getSparePartList().addAll(responseUkrParts.getSparePartList());

        when(avtoProService.searchSparePartBySerialNumber(serialNumber)).thenReturn(responseAvtopro);
        when(ukrPartsService.searchSparePartBySerialNumber(serialNumber)).thenReturn(responseUkrParts);
        when(avtoPlusService.searchSparePartBySerialNumber(serialNumber)).thenReturn(responseAvtoPlus);

        Assertions.assertEquals(avtoProService.searchSparePartBySerialNumber(serialNumber), responseAvtopro);
        Assertions.assertEquals(ukrPartsService.searchSparePartBySerialNumber(serialNumber), responseUkrParts);
        Assertions.assertEquals(avtoPlusService.searchSparePartBySerialNumber(serialNumber), responseAvtoPlus);

        Assertions.assertEquals(sparePartService.searchSparePartBySerialNumber(serialNumber), mainResponse);

        Mockito.verify(avtoProService, times(2)).searchSparePartBySerialNumber(serialNumber);
        Mockito.verify(ukrPartsService, times(2)).searchSparePartBySerialNumber(serialNumber);
        Mockito.verify(avtoPlusService, times(2)).searchSparePartBySerialNumber(serialNumber);

        Mockito.verify(sparePartService, times(1)).searchSparePartBySerialNumber(serialNumber);

    }
}
