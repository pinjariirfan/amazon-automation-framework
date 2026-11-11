package com.amazon.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Amazon search results page.
 */
public class SearchResultsPage extends BasePage {

    private final By resultItems = By.cssSelector("div[data-component-type='s-search-result']");
    // Product title link inside a search result (works for current and older layouts)
    private final By resultTitleLink = By.cssSelector(
            "div[data-component-type='s-search-result'] [data-cy='title-recipe'] a[href*='/dp/'], "
                    + "div[data-component-type='s-search-result'] h2 a[href*='/dp/'], "
                    + "div[data-component-type='s-search-result'] a:has(h2)[href*='/dp/']");

    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }

    public boolean hasResults() {
        return isDisplayed(resultItems, 15) && !findAll(resultItems).isEmpty();
    }

    /** Returns distinct, non-sponsored product URLs from the result list. */
    public List<String> getProductUrls(int max) {
        waitForVisible(resultItems);
        Set<String> urls = new LinkedHashSet<>();
        for (WebElement link : findAll(resultTitleLink)) {
            String href = link.getAttribute("href");
            if (href == null || href.contains("/sspa/") || href.contains("sspa")) {
                continue; // skip sponsored redirect links
            }
            urls.add(href);
            if (urls.size() >= max) {
                break;
            }
        }
        return new ArrayList<>(urls);
    }

    public ProductPage openProduct(String productUrl) {
        driver.get(productUrl);
        return new ProductPage(driver);
    }
}
