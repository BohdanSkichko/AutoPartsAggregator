package com.example.demo.services;

import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.helper.PropertiesReader;
import com.example.demo.service.SparePartService;
import com.example.demo.service.impl.AvtoPlusServiceImp;
import com.example.demo.service.impl.AvtoProServiceImp;
import com.example.demo.service.impl.MainServiceImp;
import com.example.demo.service.impl.UkrPartsServiceImp;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MainServiceTest {
    @InjectMocks
    private MainServiceImp mainServiceImp;

    @Mock
    private AvtoProServiceImp avtoProServiceImp;
    @Mock
    private AvtoPlusServiceImp avtoPlusServiceImp;
    @Mock
    private UkrPartsServiceImp ukrPartsServiceImp;

    @Test
    public void createFile() {


        File file = new File("Name.txt");
        Mockito.when(mainServiceImp.createFile("1", "Name", "test")).thenReturn(file);

        Assertions.assertEquals(file, mainServiceImp.createFile("1", "Name", "test"));

    }

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

        Assertions.assertEquals(mainServiceImp.searchSparePartBySerialNumber("2002"), mainResponse);

        Mockito.verify(avtoPlusServiceImp, times(2)).searchSparePartBySerialNumber(serialNumber);
        Mockito.verify(ukrPartsServiceImp, times(2)).searchSparePartBySerialNumber(serialNumber);
        Mockito.verify(avtoProServiceImp, times(2)).searchSparePartBySerialNumber(serialNumber);

    }
}
