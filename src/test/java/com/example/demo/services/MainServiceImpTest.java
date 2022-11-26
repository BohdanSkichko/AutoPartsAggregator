package com.example.demo.services;

import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.service.SparePartService;
import com.example.demo.service.impl.AvtoPlusServiceImp;
import com.example.demo.service.impl.AvtoProServiceImp;
import com.example.demo.service.impl.MainServiceImp;
import com.example.demo.service.impl.UkrPartsServiceImp;
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
public class MainServiceImpTest {

    @Spy
    @InjectMocks
    private SparePartService sparePartService = new MainServiceImp();


    @Mock
    private AvtoProServiceImp avtoProServiceImp;
    @Mock
    private AvtoPlusServiceImp avtoPlusServiceImp;
    @Mock
    private UkrPartsServiceImp ukrPartsServiceImp;


    @Test
    public void returnResponseWhenSearchSparePart() {
        String serialNumber = "2002";
        Response responseAvtopro = new Response();

        SparePart first = new SparePart();
        first.setUrl("Url");
        first.setCost(0);
        first.setDescription("Name");

        SparePart second = new SparePart();
        second.setUrl("Url");
        second.setCost(0);
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

        when(avtoProServiceImp.searchSparePartBySerialNumber(serialNumber)).thenReturn(responseAvtopro);
        when(ukrPartsServiceImp.searchSparePartBySerialNumber(serialNumber)).thenReturn(responseUkrParts);
        when(avtoPlusServiceImp.searchSparePartBySerialNumber(serialNumber)).thenReturn(responseAvtoPlus);

        Assertions.assertEquals(avtoProServiceImp.searchSparePartBySerialNumber(serialNumber), responseAvtopro);
        Assertions.assertEquals(ukrPartsServiceImp.searchSparePartBySerialNumber(serialNumber), responseUkrParts);
        Assertions.assertEquals(avtoPlusServiceImp.searchSparePartBySerialNumber(serialNumber), responseAvtoPlus);

        Assertions.assertEquals(sparePartService.searchSparePartBySerialNumber(serialNumber), mainResponse);

        Mockito.verify(avtoProServiceImp, times(2)).searchSparePartBySerialNumber(serialNumber);
        Mockito.verify(ukrPartsServiceImp, times(2)).searchSparePartBySerialNumber(serialNumber);
        Mockito.verify(avtoPlusServiceImp, times(2)).searchSparePartBySerialNumber(serialNumber);

        Mockito.verify(sparePartService, times(1)).searchSparePartBySerialNumber(serialNumber);

    }

    @Test
    public void returnResponseInputStreamResourceWhenSaveInfo() {

        MainServiceImp spyMainService = Mockito.spy(MainServiceImp.class);

        File file = spyMainService.createFile("cost", "Name", "test");

        MainServiceImp mockMainService = Mockito.mock(MainServiceImp.class);

        when(mockMainService.createFile(anyString(), anyString(), anyString())).thenReturn(file);
        mockMainService.createFile("as", "as", "test");
        verify(mockMainService).createFile(anyString(), anyString(), eq("test"));
        verify(mockMainService, times(1)).createFile(any(), anyString(), anyString());

        ResponseEntity<InputStreamResource> response = spyMainService.saveFileClientSide(file);


        when(mockMainService.saveFileClientSide(any(File.class))).thenReturn(response);
        mockMainService.saveFileClientSide(file);
        verify(mockMainService).saveFileClientSide(any(File.class));

        Assertions.assertEquals(response, mockMainService.saveFileClientSide(file));
        verify(mockMainService, times(2)).saveFileClientSide(any());
    }
}
