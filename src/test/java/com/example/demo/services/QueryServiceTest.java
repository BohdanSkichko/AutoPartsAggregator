package com.example.demo.services;

import com.example.demo.entity.SparePart;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryServiceTest {

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
