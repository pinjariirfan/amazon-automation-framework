package com.amazon.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Amazon home page: open site, dismiss interstitials, search.
 */
public class HomePage extends BasePage {

    private final By searchBox = By.id("twotabsearchtextbox");
    private final By searchButton = By.id("nav-search-submit-button");
    private final By logo = By.id("nav-logo-sprites");
    private final By cartCount = By.id("nav-cart-count");

    // Amazon sometimes shows a "Continue shopping" interstitial to new sessions
    private final By continueShoppingButton =
            By.xpath("//button[contains(normalize-space(.),'Continue shopping')]");
    // Location / cookie popups
    private final By dismissLocationPopup =
            By.xpath("//input[@data-action-type='DISMISS'] | //span[@data-action-type='DISMISS']//input");
    private final By acceptCookies = By.id("sp-cc-accept");

    // "Deliver to" location widget
    private final By deliverToLink = By.id("nav-global-location-popover-link");
    private final By deliverToText = By.id("glow-ingress-line2");
    private final By zipInput = By.id("GLUXZipUpdateInput");
    private final By zipApplyButton = By.cssSelector("#GLUXZipUpdate input, #GLUXZipUpdate");
    private final By locationDoneButton = By.xpath(
            "//*[@id='GLUXConfirmClose' or @name='glowDoneButton']"
                    + " | //div[contains(@class,'a-popover')]//*[(self::button or self::input or self::span)"
                    + " and (normalize-space(.)='Continue' or @value='Continue' or normalize-space(.)='Done')]");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public HomePage open(String url) {
        driver.get(url);
        handleInterstitials();
        return this;
    }

    /** Handles the "Continue shopping" interstitial and other popups if present. */
    public void handleInterstitials() {
        for (int attempt = 0; attempt < 3 && !isDisplayed(searchBox, 5); attempt++) {
            if (isPresent(continueShoppingButton)) {
                click(continueShoppingButton);
            } else {
                driver.navigate().refresh();
            }
        }
        if (isPresent(acceptCookies)) {
            click(acceptCookies);
        }
        if (isDisplayed(dismissLocationPopup, 2)) {
            click(dismissLocationPopup);
        }
    }

    /**
     * Sets the delivery location to a US ZIP code so products are shippable
     * (otherwise Amazon geo-locates by IP and may hide "Add to Cart").
     */
    public HomePage setDeliveryZip(String zip) {
        if (getDeliverToText().contains(zip)) {
            return this;
        }
        click(deliverToLink);
        type(zipInput, zip);
        click(zipApplyButton);
        if (isDisplayed(locationDoneButton, 8)) {
            click(locationDoneButton);
        }
        // The location is stored server-side; a reload guarantees the header reflects it
        driver.navigate().refresh();
        handleInterstitials();
        wait.until(d -> getDeliverToText().contains(zip));
        return this;
    }

    public String getDeliverToText() {
        try {
            return driver.findElement(deliverToText).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isLoaded() {
        return isDisplayed(searchBox, 10) && isDisplayed(logo, 5);
    }

    public SearchResultsPage searchFor(String keyword) {
        type(searchBox, keyword);
        click(searchButton);
        return new SearchResultsPage(driver);
    }

    public int getCartCount() {
        try {
            return Integer.parseInt(getText(cartCount));
        } catch (Exception e) {
            return 0;
        }
    }
}
