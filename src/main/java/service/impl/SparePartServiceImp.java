package service.impl;


import dto.Request;
import dto.Response;
import entity.SparePart;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import service.SparePartService;
import util.WebScraperHelper;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class SparePartServiceImp implements SparePartService {

    //Reading data from property file to a list
    @Value("#{'${website.urls}'.split(',')")
    List<String> urls;

    @Override
    public Set<Response> getSparePartBySerialNumber(String serialNumber) {
        //Using a set here to only store unique elements
        Set<Response> responseDTOS = new HashSet<>();
        //Traversing through the urls
        for (String url: urls) {
            if (url.contains("avto-plus")) {
                //method to extract data from Ikman.lk
                extractDataFromAvtoPlus(responseDTOS, url + serialNumber);
            } else if (url.contains("riyasewana")) {
                //method to extract Data from riyasewana.com
                extractDataFromIkman(responseDTOS, url + serialNumber);
            }

        }

        return responseDTOS;
    }

    private void extractDataFromAvtoPlus(Set<Response> responseDTOS, String url) {

        try {
            //loading the HTML to a Document Object
            Document document = Jsoup.connect(url).get();
            //Selecting the element which contains the ad list
            Element element = document.getElementsByClass("wrapper").first();
            //getting all the <a> tag elements inside the content div tag

            Elements elements = element.getElementsByTag("a");

            for (Element ads: elements) {

                Response responseDTO = new Response();

                if (!StringUtils.isEmpty(ads.attr("href"))) {
                    //mapping data to our model class
                    responseDTO.setDescription(ads.attr("alt"));
                    responseDTO.setUrl(ads.attr("href"));
                }
                if (responseDTO.getUrl() != null) responseDTOS.add(responseDTO);

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }



    private void extractDataFromIkman(Set<Response> responseDTOS, String s) {
    }

}
