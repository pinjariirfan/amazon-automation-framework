package com.amazon.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Amazon product detail page.
 */
public class ProductPage extends BasePage {

    private final By productTitle = By.id("productTitle");
    private final By addToCartButton = By.cssSelector("#add-to-cart-button, input[name='submit.add-to-cart']");

    // Possible confirmations after adding to cart
    private final By addedToCartConfirmation = By.cssSelector(
            "#NATC_SMART_WAGON_CONF_MSG_SUCCESS, #sw-atc-details-single-container, "
                    + "#attachDisplayAddBaseAlert, #huc-v2-order-row-confirm-text, "
                    + "#sw-atc-confirmation, [data-csa-c-content-id='sw-atc-confirmation']");
    // Protection-plan / accessory upsell side sheet
    private final By noThanksCoverage = By.cssSelector(
            "#attachSiNoCoverage input, #attachSiNoCoverage, button[aria-labelledby='attachSiNoCoverage-announce']");
    private final By navCartCount = By.id("nav-cart-count");
    // Amazon's human-verification / CAPTCHA gate
    private final By botCheck = By.xpath(
            "//*[contains(normalize-space(.),'I am human') or contains(normalize-space(.),'Enter the characters you see')]"
                    + " | //form[contains(@action,'validateCaptcha')]");

    public ProductPage(WebDriver driver) {
        super(driver);
    }

    public String getProductTitle() {
        return isDisplayed(productTitle, 15) ? getText(productTitle) : "";
    }

    public boolean canAddToCart() {
        return isDisplayed(addToCartButton, 8);
    }

    /**
     * Clicks "Add to Cart", dismisses any warranty upsell and waits for the
     * cart counter to increase. Returns true if the item was added.
     */
    public boolean addToCart() {
        int before = readCartCount();
        click(addToCartButton);

        if (isDisplayed(noThanksCoverage, 4)) {
            click(noThanksCoverage);
        }

        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(addedToCartConfirmation),
                    ExpectedConditions.presenceOfElementLocated(botCheck),
                    d -> readCartCount() > before));
        } catch (Exception ignored) {
            // fall through to the checks below
        }
        if (isBotCheckDisplayed()) {
            throw new BotCheckException("Amazon presented a human-verification (CAPTCHA) page after "
                    + "'Add to Cart'. The automation does not bypass it; run in headed mode or "
                    + "from a network Amazon does not flag. URL: " + driver.getCurrentUrl());
        }
        return isPresent(addedToCartConfirmation) || readCartCount() > before;
    }

    public boolean isBotCheckDisplayed() {
        return isPresent(botCheck);
    }

    /** Raised when Amazon blocks the session with a human-verification page. */
    public static class BotCheckException extends RuntimeException {
        public BotCheckException(String message) {
            super(message);
        }
    }

    private int readCartCount() {
        try {
            return Integer.parseInt(driver.findElement(navCartCount).getText().trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public CartPage goToCart(String baseUrl) {
        driver.get(baseUrl + "/gp/cart/view.html");
        return new CartPage(driver);
    }
}
