package com.aggregator.autoparts.service.impl;

import com.aggregator.autoparts.dto.SparePart;
import com.aggregator.autoparts.helper.CostFetcher;
import com.aggregator.autoparts.helper.enumeration.WordAndPunctuationHolder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class AvtoProServiceTest {

    @InjectMocks
    private AvtoProService avtoProService = new AvtoProService();
    @Mock
    private CostFetcher costFetcher;

    @Test
    public void asserEqualsDataSparePartsWhenExtractSpareParts() throws IOException {
        byte[] testBytes = Files.readAllBytes(Paths.get("src/test/java/resources/AvtoProJsonResponseWithSparePArts"));
        String testString = new String(testBytes);

        HttpEntity<String> testEntity = new HttpEntity<>(testString);

        SparePart firstReference = new SparePart();
        firstReference.setUrl("null" + "part-20-AC_DELCO-435");
        firstReference.setCost(0);
        firstReference.setDescription("AC Delco 20" + WordAndPunctuationHolder.WHITE_SPACE.getPath());

        SparePart secondReference = new SparePart();
        secondReference.setUrl("null" + "zapchasti-20-%D0%A8%D0%9B%D0%90%D0%9D%D0%93_%D0%9D%D0%90_%D0%9F%D0%9E%D0%94%D0%9E%D0%93%D0%A0%D0%95%D0%92");
        secondReference.setCost(0);
        secondReference.setDescription("Brisk 20" + WordAndPunctuationHolder.WHITE_SPACE.getPath());

        List<SparePart> referenceList = new ArrayList<>();
        referenceList.add(firstReference);
        referenceList.add(secondReference);


        ObjectMapper mapper = new ObjectMapper();
        JsonNode testRoot = mapper.readTree(testEntity.getBody());
        JsonNode testNode = testRoot.findPath("Suggestions");

        when(costFetcher.setCostFromRemoteHost(anyString(), eq(referenceList), anyString())).thenReturn(referenceList);
        List<SparePart> result = avtoProService.extractSpareParts(testNode);
        assertEquals(result, referenceList);
    }
}
