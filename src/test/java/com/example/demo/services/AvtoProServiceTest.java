/*
package com.example.demo.services;

import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import com.example.demo.helper.CostFetcher;
import com.example.demo.service.impl.AvtoProService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class AvtoProServiceTest {

//    @InjectMocks
//    private AvtoProServiceImp avtoProServiceImp;

    @Mock
    private Executor executor;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private List<String> strings;
    @Mock
    private CostFetcher costFetcher;
    @Spy
    AvtoProService avtoProService = new AvtoProService(restTemplate, executor, costFetcher, 10, strings);

    @Test
    public void getListSparePart() {
        List<SparePart> parts = Mockito.spy(new ArrayList<>());
        SparePart sparePart = new SparePart("url", 100, "2002 part");
        SparePart sparePart1 = new SparePart("url", 12, "2001 part2");
        Response response = Mockito.mock(Response.class);
        response.setSparePartList(parts);
        parts.add(sparePart);
        parts.add(sparePart1);
        when(response.getSparePartList()).thenReturn(parts);
        assertEquals(response.getSparePartList(), parts);
        verify(response).getSparePartList();
        Mockito.verify(parts).add(sparePart);
        Mockito.verify(parts).add(sparePart1);
        assertEquals(2, parts.size());
    }


    @Test
    public void extractDate() throws JsonProcessingException {
        HttpEntity<String> node = new HttpEntity<>(
                "{\n" +
                        "    \"FoundByDescription\": null,\n" +
                        "    \"Region\": {\n" +
                        "        \"Id\": 1,\n" +
                        "        \"Name\": \"Europe\",\n" +
                        "        \"Link\": \"catalog\"\n" +
                        "    },\n" +
                        "    \"Make\": null,\n" +
                        "    \"Model\": null,\n" +
                        "    \"EngineId\": null,\n" +
                        "    \"Category\": null,\n" +
                        "    \"Applicability\": null,\n" +
                        "    \"CrossgroupId\": null,\n" +
                        "    \"BrandName\": null,\n" +
                        "    \"PartNumber\": null,\n" +
                        "    \"ResultEngineId\": null,\n" +
                        "    \"ResultCrossgroupId\": null,\n" +
                        "    \"Part\": null,\n" +
                        "    \"PartFitsVehicle\": null,\n" +
                        "    \"SuggestionType\": \"Part\",\n" +
                        "    \"Suggestions\": [\n" +
                        "        {\n" +
                        "            \"Title\": \"AC Delco 20\",\n" +
                        "            \"TitleBlocks\": [\n" +
                        "                {\n" +
                        "                    \"Type\": \"Text\",\n" +
                        "                    \"Str\": \"AC Delco \"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"Type\": \"MatchesQuery\",\n" +
                        "                    \"Str\": \"20\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"NextQueryString\": \"\",\n" +
                        "            \"Uri\": \"/system/search/result/.aspx?sId=20221116202341-6a5d4861-8c6d-456b-a7e1-2a8e808f81c5&seqId=0&sInd=0&uri=%2Fpart-20-AC_DELCO-435%2F&queryId=20221116213327-81b1930b-6cad-4f88-8a49-4985ecf35cbd&selectSuggest=AC%20Delco%2020\",\n" +
                        "            \"FoundMake\": null,\n" +
                        "            \"FoundModel\": null,\n" +
                        "            \"FoundEngine\": null,\n" +
                        "            \"FoundCategory\": null,\n" +
                        "            \"FoundCrossgroupApplicability\": null,\n" +
                        "            \"FoundPart\": {\n" +
                        "                \"Part\": {\n" +
                        "                    \"Brand\": {\n" +
                        "                        \"Id\": 195,\n" +
                        "                        \"Name\": \"AC Delco\",\n" +
                        "                        \"Link\": \"ac-delco\",\n" +
                        "                        \"ViewName\": \"AC Delco\",\n" +
                        "                        \"Country\": null,\n" +
                        "                        \"Site\": null,\n" +
                        "                        \"AverangeRating\": 0.0,\n" +
                        "                        \"CountFeedback\": 0,\n" +
                        "                        \"Path\": \"AC_DELCO\",\n" +
                        "                        \"FoundationYear\": 0,\n" +
                        "                        \"Description\": null,\n" +
                        "                        \"CountUniquePart\": 0,\n" +
                        "                        \"CountSubType\": 0,\n" +
                        "                        \"Logo\": null,\n" +
                        "                        \"Concern\": null,\n" +
                        "                        \"ConcernParticipants\": null,\n" +
                        "                        \"IsOriginal\": false\n" +
                        "                    },\n" +
                        "                    \"FullNr\": \"20\",\n" +
                        "                    \"ShortNr\": \"20\",\n" +
                        "                    \"CrossgroupId\": 2084178521,\n" +
                        "                    \"Crossgroup\": null,\n" +
                        "                    \"Images\": null,\n" +
                        "                    \"Parameters\": null,\n" +
                        "                    \"IsRestoration\": false,\n" +
                        "                    \"PartFeedUri\": null,\n" +
                        "                    \"IsSyntheticPartNumber\": false\n" +
                        "                },\n" +
                        "                \"SellersCount\": null,\n" +
                        "                \"PriceMin\": null,\n" +
                        "                \"PriceMax\": null,\n" +
                        "                \"Description\": null\n" +
                        "            },\n" +
                        "            \"PartFitsVehicle\": null,\n" +
                        "            \"PartFitsEngine\": null\n" +
                        "        }, {\n" +
                        "            \"Title\": \"ШЛАНГ НА ПОДОГРЕВ 20\",\n" +
                        "            \"TitleBlocks\": [\n" +
                        "                {\n" +
                        "                    \"Type\": \"Text\",\n" +
                        "                    \"Str\": \"ШЛАНГ НА ПОДОГРЕВ \"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"Type\": \"MatchesQuery\",\n" +
                        "                    \"Str\": \"20\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"NextQueryString\": \"\",\n" +
                        "            \"Uri\": \"/system/search/result/.aspx?sId=20221116202341-6a5d4861-8c6d-456b-a7e1-2a8e808f81c5&seqId=0&sInd=71&uri=%2Fzapchasti-20-%D0%A8%D0%9B%D0%90%D0%9D%D0%93_%D0%9D%D0%90_%D0%9F%D0%9E%D0%94%D0%9E%D0%93%D0%A0%D0%95%D0%92%2F&queryId=20221116213327-81b1930b-6cad-4f88-8a49-4985ecf35cbd&selectSuggest=%D0%A8%D0%9B%D0%90%D0%9D%D0%93%20%D0%9D%D0%90%20%D0%9F%D0%9E%D0%94%D0%9E%D0%93%D0%A0%D0%95%D0%92%2020\",\n" +
                        "            \"FoundMake\": null,\n" +
                        "            \"FoundModel\": null,\n" +
                        "            \"FoundEngine\": null,\n" +
                        "            \"FoundCategory\": null,\n" +
                        "            \"FoundCrossgroupApplicability\": null,\n" +
                        "            \"FoundPart\": {\n" +
                        "                \"Part\": {\n" +
                        "                    \"Brand\": {\n" +
                        "                        \"Id\": null,\n" +
                        "                        \"Name\": \"ШЛАНГ НА ПОДОГРЕВ\",\n" +
                        "                        \"Link\": null,\n" +
                        "                        \"ViewName\": null,\n" +
                        "                        \"Country\": null,\n" +
                        "                        \"Site\": null,\n" +
                        "                        \"AverangeRating\": 0.0,\n" +
                        "                        \"CountFeedback\": 0,\n" +
                        "                        \"Path\": \"ШЛАНГ_НА_ПОДОГРЕВ\",\n" +
                        "                        \"FoundationYear\": 0,\n" +
                        "                        \"Description\": null,\n" +
                        "                        \"CountUniquePart\": 0,\n" +
                        "                        \"CountSubType\": 0,\n" +
                        "                        \"Logo\": null,\n" +
                        "                        \"Concern\": null,\n" +
                        "                        \"ConcernParticipants\": null,\n" +
                        "                        \"IsOriginal\": false\n" +
                        "                    },\n" +
                        "                    \"FullNr\": \"20\",\n" +
                        "                    \"ShortNr\": \"20\",\n" +
                        "                    \"CrossgroupId\": null,\n" +
                        "                    \"Crossgroup\": null,\n" +
                        "                    \"Images\": null,\n" +
                        "                    \"Parameters\": null,\n" +
                        "                    \"IsRestoration\": false,\n" +
                        "                    \"PartFeedUri\": null,\n" +
                        "                    \"IsSyntheticPartNumber\": false\n" +
                        "                },\n" +
                        "                \"SellersCount\": null,\n" +
                        "                \"PriceMin\": null,\n" +
                        "                \"PriceMax\": null,\n" +
                        "                \"Description\": \"6282031782\"\n" +
                        "            },\n" +
                        "            \"PartFitsVehicle\": null,\n" +
                        "            \"PartFitsEngine\": null\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"FeedUri\": null,\n" +
                        "    \"IsAnythingFound\": true,\n" +
                        "    \"DebugInfo\": {\n" +
                        "        \"Messages\": []\n" +
                        "    },\n" +
                        "    \"perfLog\": \"total: 13, descr: 0, mod: 1, code: 3, cnf: 7, man: 0, hypo: 0, other: 2\",\n" +
                        "    \"VIN\": null,\n" +
                        "    \"VehicleCategories\": null\n" +
                        "}"

        );

        Response response = new Response();
        SparePart first = new SparePart();
        first.setUrl("https://avto.pro//system/search/result/.aspx?sId=20221116202341-6a5d4861-8c6d-456b-a7e1-2a8e808f81c5&seqId=0&sInd=0&uri=%2Fpart-20-AC_DELCO-435%2F&queryId=20221116213327-81b1930b-6cad-4f88-8a49-4985ecf35cbd&selectSuggest=AC%20Delco%2020");
        first.setCost(0);
        first.setDescription("AC Delco 20");
        SparePart second = new SparePart();
        second.setUrl("https://avto.pro//system/search/result/.aspx?sId=20221116202341-6a5d4861-8c6d-456b-a7e1-2a8e808f81c5&seqId=0&sInd=71&uri=%2Fzapchasti-20-%D0%A8%D0%9B%D0%90%D0%9D%D0%93_%D0%9D%D0%90_%D0%9F%D0%9E%D0%94%D0%9E%D0%93%D0%A0%D0%95%D0%92%2F&queryId=20221116213327-81b1930b-6cad-4f88-8a49-4985ecf35cbd&selectSuggest=%D0%A8%D0%9B%D0%90%D0%9D%D0%93%20%D0%9D%D0%90%20%D0%9F%D0%9E%D0%94%D0%9E%D0%93%D0%A0%D0%95%D0%92%2020");
        second.setCost(0);
        second.setDescription("ШЛАНГ НА ПОДОГРЕВ 20");
        response.getSparePartList().add(first);
        response.getSparePartList().add(second);


        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(node.getBody());
        JsonNode jsonNode = root.path("Suggestions");

        Response responseExtractNode = new Response();

//        avtoProServiceImp.extractJsonNode(responseExtractNode, jsonNode,);

//        CompletableFuture<Response> responseGetResponseHttpEntity = avtoProServiceImp.getResponseFromHttpEntity(node, "Suggestions", executor);

        assertEquals(response, responseExtractNode);
//        assertEquals(response, responseGetResponseHttpEntity.join());
    }
}
*/
