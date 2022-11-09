package com.example.demo.services;

import com.example.demo.dto.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.service.impl.SparePartServiceImp;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;


import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RequestServiceTest {
    @Mock
    private SparePartServiceImp sparePartServiceImp;


    public void getRequestBySerialNumber() {
        Mockito.mock(SparePart.class);
        SparePart sparePart;
        sparePart = new SparePart(1, "url", "2002", 100, "2002 part");
        Response response = Mockito.mock(Response.class);
        response.getSparePartList().add(sparePart);
        Mockito.when(sparePartServiceImp.searchSparePartBySerialNumber("2002")).thenReturn(response);
    }

    @Test
    public void getListSparePart() {
        List<SparePart> parts = Mockito.spy(new ArrayList<>());
        SparePart sparePart = new SparePart(1, "url", "2002", 100, "2002 part");
        SparePart sparePart1 = new SparePart(2, "url", "2002", 12, "2001 part2");
        parts.add(sparePart);
        parts.add(sparePart1);
        Mockito.verify(parts).add(sparePart);
        Mockito.verify(parts).add(sparePart1);
        assertEquals(2, parts.size());
    }

}
