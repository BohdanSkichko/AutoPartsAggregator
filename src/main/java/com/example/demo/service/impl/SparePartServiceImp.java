/*
package com.example.demo.service.impl;


import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.demo.service.SparePartService;
import org.springframework.util.StringUtils;


import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@AllArgsConstructor
@NoArgsConstructor
public class SparePartServiceImp implements SparePartService {

    Logger logger = LoggerFactory.getLogger(SparePartServiceImp.class);
    @Value("#{'${website.urls}'.split(',')}")
    List<String> urls;
    @Value("#{${pages}}")
    private int pages;
    @Autowired
    Executor executor;
    @Autowired
    AvtoProServiceImp avtoProServiceImp;
    private static final String AVTOPRO = "https://avto.pro";
    private static final String REPLACE_TEXT_IN_PRICE = "[а-яА-Я: ]+";
    private static final String ALERT_AVTOPRO = "По вашему запросу ничего не найдено";
    private static final String AVTO_PLUS_SITE = "https://avto-plus.com.ua/";
    private static final String UKRPARTS_SITE = "https://ukrparts.com.ua";
    private static final int CORE = 4;


    public Response searchSparePartBySerialNumber(String serialNumber) {
        List<Response> listResponse = getResponseList(serialNumber);
        Response result = new Response();
        for (Response response : listResponse) {
            result.getSparePartList().addAll(response.getSparePartList());
        }
        int idSparePart = 1;
        for (int i = 0; i < result.getSparePartList().size(); i++) {
            result.getSparePartList().get(i).setId(idSparePart);
            idSparePart++;
        }
        sortByCost(result);
        return result;
    }


    private List<Response> getResponseList(String serialNumber) {
        Response responseAvtoPlus = new Response();
        CompletableFuture<Response> result1 = getResponseCompletableFuture(serialNumber, responseAvtoPlus, AVTO_PLUS_SITE);
        Response responseUkrParts = new Response();
        CompletableFuture<Response> result2 = getResponseCompletableFuture(serialNumber, responseUkrParts, UKRPARTS_SITE);
        Response responseAvtoPro = new Response();
        CompletableFuture<Response> result3 = getResponseCompletableFuture(serialNumber, responseAvtoPro, AVTOPRO);
        return Stream.of(result1, result2, result3).map(CompletableFuture::join).collect(Collectors.toList());
    }

    private CompletableFuture<Response> getResponseCompletableFuture(String serialNumber, Response response,
                                                                     String snippetSiteName) {
        return CompletableFuture.supplyAsync(
                () -> {
                    logger.info("find spare parts " + Thread.currentThread().getName());
                    long start = System.currentTimeMillis();
                    extractedForAllSites(serialNumber, response, snippetSiteName);
                    long end = System.currentTimeMillis();
                    System.out.println(Thread.currentThread().getName() + " finish " + (end - start));
                    return response;
                }, executor);
    }

    private void extractedForAllSites(String serialNumber, Response response, String snippetSiteName) {
        for (String url : urls) {
            if (url.contains(snippetSiteName) && url.contains(AVTO_PLUS_SITE)) {
                extractDataFromAvtoPlus(response, url, serialNumber);
            }
            if (url.contains(snippetSiteName) && url.contains(AVTOPRO)) {
                extractAvtoPro(response, url, serialNumber);
            }
            if (url.contains(snippetSiteName) && url.contains(UKRPARTS_SITE)) {
                extractDataFromUkrparts(response, url, serialNumber);
            }
        }
    }

    private void extractDataFromUkrparts(Response response, String url, String serialNumber) {
        try {
            Document document = Jsoup.connect(url + serialNumber).get();
            List<Element> listElement = new ArrayList<>(document.getElementsByClass("col-xs-12 col-md-4 part_box_wrapper"));
            for (Element e : listElement) {
                Elements elementsName = e.getElementsByClass("part_article");
                SparePart sparePart = new SparePart();
                sparePart.setDescription(elementsName.text());
                sparePart.setSerialNumber(serialNumber);
                Elements elementPrice = e.getElementsByClass("price_min");
                String text = elementPrice.text();
                String cost = text.replaceAll(REPLACE_TEXT_IN_PRICE, "");
                if (cost.isEmpty()) {
                    break;
                }
                sparePart.setCost(Integer.parseInt(cost));
                Elements elementsURL = e.getElementsByTag("a");
                if (StringUtils.hasText(elementsURL.attr("href"))) {
                    sparePart.setUrl(UKRPARTS_SITE + elementsURL.attr("href"));
                    response.getSparePartList().add(sparePart);
                    break;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void extractDataFromAvtoPlus(Response response, String url, String serialNumber) {
        try {
            //set first page
            int quantityPages = 1;
            // url = patternFromProperties + page + patterForIterationPages + serialNumber(nameSparePart)
            String patternForIterationPages = "/?search=";
            Document searchPage = Jsoup.connect(url + quantityPages + patternForIterationPages + serialNumber).get();
            // search  quantity pages
            Element listPages = searchPage.
                    getElementsByClass("wm-pagination__btn js-submit-pagination load-more-search").first();
            if (listPages != null) {
                Element elementWithQuantityPages = listPages.getElementsByAttribute("data-total").first();
                quantityPages = Integer.parseInt(elementWithQuantityPages.attr("data-total"));
                if (quantityPages > pages) { // if pages > 10, let's leave just 10;
                    quantityPages = pages;
                }
            }
            for (int i = 1; i <= quantityPages; i++) {
                getSparePartOnPageAvtoPlus(response, url, serialNumber, patternForIterationPages, i);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void getSparePartOnPageAvtoPlus(Response response, String url, String serialNumber,
                                            String patternForIterationPages, int pages) {
        CompletableFuture.supplyAsync(() -> {
            try {
                Document document = Jsoup.connect(url + pages + patternForIterationPages + serialNumber).get();
                List<Element> listElementInside = new ArrayList<>(document.
                        getElementsByClass("goods__item product-card product-card--categoryPage"));
                for (Element e : listElementInside) {
                    Elements elements = e.getElementsByTag("a");
                    boolean isFirst = true;
                    for (Element el : elements) {
                        SparePart sparePart = new SparePart();
                        if (StringUtils.hasText(el.attr("href"))) {
                            if (isFirst) {
                                isFirst = false;
                                continue;
                            }
                            sparePart.setDescription(el.text());
                            Elements elementsCost = e.getElementsByClass("basket-button__uah");
                            String text = elementsCost.text();
                            String cost = text.replaceAll(REPLACE_TEXT_IN_PRICE, "");
                            sparePart.setCost(Integer.parseInt(cost));
                            sparePart.setUrl(AVTO_PLUS_SITE + el.attr("href"));
                            response.getSparePartList().add(sparePart);
                            break;
                        }
                    }
                }
                return response;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

//    private void extractAvtoPro(Response response, String url, String serialNumber) {
//
//        try {
//            WebDriver driver = new ChromeDriver();
//            //get connection with site
//            driver.get(url);
//            //class search with value for part searching and set SerialNumber
//            driver.findElement(By.className("ap-search__input")).sendKeys(serialNumber);
//            //wait selenium input my value
//            Thread.sleep(1000);
//            WebElement webElementAlert = driver.findElement
//                    (By.xpath("//*[@id=\"ap-search\"]/div/div[2]/div/div[1]/div[1]"));
//            // check
//            if (!webElementAlert.getText().contains(ALERT_AVTOPRO)) {
//                //  iteration for pages -> search sparePart
//                List<Integer> integerListPages = new ArrayList<>();
//                List<WebElement> elementsListWithSparePart = driver.findElement(By.className("ap-search__result-wrapper"))
//                        .findElements(By.tagName("a"));
//                int quantityElements = elementsListWithSparePart.size();
//                if (elementsListWithSparePart.size() > pages) {
//                    quantityElements = pages;
//                }
//                for (int i = 0; i < quantityElements; i++) {
//                    integerListPages.add(i);
//                }
//                getSparePartFromParallelStreamAvtopro(response, serialNumber, integerListPages, elementsListWithSparePart);
//            }
//            driver.quit();
//        } catch (InterruptedException | ExecutionException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private void getSparePartFromParallelStreamAvtopro(
//            Response response, String serialNumber, List<Integer> integerListPages,
//            List<WebElement> elementsListWithSparePart) throws InterruptedException, ExecutionException {
//        ForkJoinPool myPool = new ForkJoinPool();
//        myPool.submit(() ->
//                integerListPages.parallelStream()
//                        .forEach(numberWebElement -> {
//                            System.out.println(Thread.currentThread().getName());
//                            getSparePartFromWebElementAvtoPro(response, elementsListWithSparePart, serialNumber, numberWebElement);
//                        })).get();
//    }
//
//    private void getSparePartFromWebElementAvtoPro(
//            Response response, List<WebElement> elementsListWithSparePart, String serialNumber, int numberWebElement) {
//        SparePart sparePart = new SparePart();
//        sparePart.setDescription(elementsListWithSparePart.get(numberWebElement)
//                .findElement(By.className("ap-search__column"))
//                .getText().replaceAll("\n", " ")
//                .concat(" ListWithReferences"));
//        sparePart.setSerialNumber(serialNumber);
//        if (StringUtils.hasText(elementsListWithSparePart.get(numberWebElement).getAttribute("href"))) {
//            sparePart.setUrl(elementsListWithSparePart.get(numberWebElement).getAttribute("href"));
//            response.getSparePartList().add(sparePart);
//        }
//    }

    private void sortByCost(Response result) {
        List<SparePart> sortByCost = result.getSparePartList().stream().sorted(Comparator.comparingInt(SparePart::getCost))
                .collect(Collectors.toList());
        result.setSparePartList(sortByCost);
    }


//    @Override
//    public String response(String serialNumber) {
//        HttpClient httpClient = HttpClientBuilder.create().build();
//        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
//        String url = "https://avto.pro/api/v1/search/query/";
//        RestTemplate restTemplate = new RestTemplate(requestFactory);
//
//        Query query = new Query();
//        query.setQuery(serialNumber);
//        query.setRegionId(1);
//        query.setSuggestionType("Regular");
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Accept", MediaType.ALL_VALUE);
//        headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
//        HttpEntity<Query> requestBody = new HttpEntity<>(query, headers);
//        ResponseEntity<Suggestions> response = restTemplate.exchange(url, HttpMethod.PUT, requestBody, Suggestions.class);
//        return response.toString();


//        RequestCallback requestCallback(final Foo updatedInstance) {
//            return clientHttpRequest -> {
//                ObjectMapper mapper = new ObjectMapper();
//                mapper.writeValue(clientHttpRequest.getBody(), updatedInstance);
//                clientHttpRequest.getHeaders().add(
//                        HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
//                clientHttpRequest.getHeaders().add(
//                        HttpHeaders.AUTHORIZATION, "Basic " + getBase64EncodedLogPass());
//            };
//        }
//        String url = "https://avto.pro/api/v1/search/query/";
//        RestTemplate restTemplate = new RestTemplate();
//        HttpEntity<Query> request = new HttpEntity<>(new Query(serialNumber));
//        ResponseEntity<Suggestions> response = restTemplate
//                .exchange(url, HttpMethod.POST, request, Suggestions.class);

//        Suggestions updatedInstance = new Suggestions();
//        updatedInstance.setTitle(response.getBody().getTitle());
//        String resourceUrl =fooResourceUrl + '/' + response.getBody().getId();
//        restTemplate.execute(
//                resourceUrl,
//                HttpMethod.PUT,
//                requestCallback(updatedInstance),
//                clientHttpResponse -> null);

    }

*/
