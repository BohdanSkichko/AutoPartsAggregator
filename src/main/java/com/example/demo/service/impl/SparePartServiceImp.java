package com.example.demo.service.impl;


import com.example.demo.dto.Response;
import com.example.demo.entity.SparePart;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
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
import org.springframework.core.env.Environment;
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
    @Value("#{'${pages}'}")
    private int pages;

    @Autowired
    private Environment environment;
    @Autowired
    Executor executor;
    private static final String AVTOPRO = "https://avto.pro";
    private static final String REPLACE_TEXT_IN_PRICE = "[а-яА-Я: ]+";
    private static final String ALERT_AVTOPRO = "По вашему запросу ничего не найдено";
    private static final String AVTO_PLUS_SITE = "https://avto-plus.com.ua/";
    private static final String UKRPARTS_SITE = "https://ukrparts.com.ua";
    private static final int CORE = 6;


    public Response searchSparePartBySerialNumber(String serialNumber) {
        List<Response> listResponse = getListResponse(serialNumber);
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

    @NotNull
    private List<Response> getListResponse(String serialNumber) {
        Response responseAvtoPlus = new Response();
        CompletableFuture<Response> result1 = getResponseCompletableFuture(serialNumber, responseAvtoPlus, AVTO_PLUS_SITE);
        Response responseUkrParts = new Response();
        CompletableFuture<Response> result2 = getResponseCompletableFuture(serialNumber, responseUkrParts, UKRPARTS_SITE);
        Response responseAvtoPro = new Response();
        CompletableFuture<Response> result3 = getResponseCompletableFuture(serialNumber, responseAvtoPro, AVTOPRO);
        return Stream.of(result1, result2, result3).map(CompletableFuture::join).collect(Collectors.toList());
    }

    @NotNull
    private CompletableFuture<Response> getResponseCompletableFuture(String serialNumber, Response response,
                                                                     String snippetSiteName) {
        return CompletableFuture.supplyAsync(
                () -> {
                    logger.info("find first spare parts " + Thread.currentThread().getName());
                    long start = System.currentTimeMillis();
                    extractedForAllSite(serialNumber, response, snippetSiteName);
                    long end = System.currentTimeMillis();
                    System.out.println(Thread.currentThread().getName() + " finish " + (end - start));
                    return response;
                }, executor);
    }

    private void extractedForAllSite(String serialNumber, Response response, String snippetSiteName) {
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

    private void sortByCost(Response result) {
        List<SparePart> sortByCost = result.getSparePartList().stream().sorted(Comparator.comparingInt(SparePart::getCost))
                .collect(Collectors.toList());
        result.setSparePartList(sortByCost);
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
            //  iteration for pages -> search sparePart
            List<Integer> integerListPages = new ArrayList<>();
            for (int i = 1; i <= quantityPages; i++) {
                integerListPages.add(i);
            }
            getSparePartFromParallelStreamAvtoPlus(response, url, serialNumber, patternForIterationPages, integerListPages);
        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void getSparePartFromParallelStreamAvtoPlus(
            Response response, String url, String serialNumber, String patternForIterationPages,
            List<Integer> integerListPages) throws InterruptedException, ExecutionException {
        ForkJoinPool myPool = new ForkJoinPool(CORE);
        myPool.submit(() ->
                integerListPages.parallelStream()
                        .forEach(pageNumber -> {
                            try {
                                System.out.println(Thread.currentThread().getName());
                                getSparePartOnPageUkrparts(response, url, serialNumber, patternForIterationPages,
                                        pageNumber);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })).get();
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

    private void getSparePartOnPageUkrparts(Response response, String url, String serialNumber,
                                            String patternForIterationPages, int pages) throws IOException {
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
                    sparePart.setSerialNumber(serialNumber);
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
    }

    private void extractAvtoPro(Response response, String url, String serialNumber) {

        try {
            System.setProperty("webdriver.chrome.driver", Objects.requireNonNull(environment.getProperty("chromeDriver")));
            //WebDriverManager.firefoxdriver().setup();
            WebDriver driver = new ChromeDriver();
            //get connection with site
            driver.get(url);
            //class search with value for part searching and set SerialNumber
            driver.findElement(By.className("ap-search__input")).sendKeys(serialNumber);
            //wait selenium input my value
            Thread.sleep(1000);
            WebElement webElementAlert = driver.findElement
                    (By.xpath("//*[@id=\"ap-search\"]/div/div[2]/div/div[1]/div[1]"));
            // check
            if (!webElementAlert.getText().contains(ALERT_AVTOPRO)) {
                //  iteration for pages -> search sparePart
                List<Integer> integerListPages = new ArrayList<>();
                List<WebElement> elementsListWithSparePart = driver.findElement(By.className("ap-search__result-wrapper"))
                        .findElements(By.tagName("a"));
                int quantityElements = elementsListWithSparePart.size();
                if (elementsListWithSparePart.size() > pages) {
                    quantityElements = pages;
                }
                for (int i = 0; i < quantityElements; i++) {
                    integerListPages.add(i);
                }
                getSparePartFromParallelStreamAvtopro(response, serialNumber, integerListPages, elementsListWithSparePart);
            }
            driver.quit();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void getSparePartFromParallelStreamAvtopro(
            Response response, String serialNumber, List<Integer> integerListPages,
            List<WebElement> elementsListWithSparePart) throws InterruptedException, ExecutionException {
        ForkJoinPool myPool = new ForkJoinPool(CORE);
        myPool.submit(() ->
                integerListPages.parallelStream()
                        .forEach(numberWebElement -> {
                            System.out.println(Thread.currentThread().getName());
                            getSparePartFromWebElementAvtoPro(response, elementsListWithSparePart, serialNumber, numberWebElement);
                        })).get();
    }

    private void getSparePartFromWebElementAvtoPro(Response response, @NotNull List<WebElement> elementsListWithSparePart, String serialNumber, int numberWebElement) {
        SparePart sparePart = new SparePart();
        sparePart.setDescription(elementsListWithSparePart.get(numberWebElement)
                .findElement(By.className("ap-search__column"))
                .getText().replaceAll("\n", " ")
                .concat(" ListWithReferences"));
        sparePart.setSerialNumber(serialNumber);
        if (StringUtils.hasText(elementsListWithSparePart.get(numberWebElement).getAttribute("href"))) {
            sparePart.setUrl(elementsListWithSparePart.get(numberWebElement).getAttribute("href"));
            response.getSparePartList().add(sparePart);
        }
    }
}
