package com.amazon.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page reached after clicking "Proceed to checkout".
 * For a guest (not signed-in) session Amazon redirects to the Sign-In page —
 * this is the expected end point of the automated checkout flow. The framework
 * intentionally never places a real order.
 */
public class CheckoutPage extends BasePage {

    private final By signInEmailField = By.cssSelector("#ap_email, #ap_email_login, input[name='email']");
    private final By signInHeading = By.cssSelector("h1");
    private final By checkoutHeader = By.cssSelector("#checkout-page-header, .checkout-header, #spc-header");

    public CheckoutPage(WebDriver driver) {
        super(driver);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/ap/signin"),
                ExpectedConditions.urlContains("/checkout"),
                ExpectedConditions.urlContains("/gp/buy"),
                ExpectedConditions.visibilityOfElementLocated(signInEmailField)));
    }

    /** True if Amazon asked the guest user to sign in to continue checkout. */
    public boolean isSignInRequired() {
        return getCurrentUrl().contains("/ap/signin") || isDisplayed(signInEmailField, 10);
    }

    /** True if a signed-in session reached the actual checkout page. */
    public boolean isCheckoutPageDisplayed() {
        return getCurrentUrl().contains("/checkout") || getCurrentUrl().contains("/gp/buy")
                || isDisplayed(checkoutHeader, 5);
    }

    public String getHeading() {
        return isDisplayed(signInHeading, 5) ? getText(signInHeading) : "";
    }
}
