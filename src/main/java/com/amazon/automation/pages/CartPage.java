package com.amazon.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Amazon shopping cart page.
 */
public class CartPage extends BasePage {

    private final By cartHeader = By.cssSelector("#sc-active-cart h2, #sc-active-cart h1, div.sc-cart-header");
    private final By activeCartItems = By.cssSelector(
            "#sc-active-cart div[data-asin][data-itemtype='active'], #sc-active-cart .sc-list-item[data-asin]");
    private final By itemTitles = By.cssSelector(
            "#sc-active-cart .sc-product-title, #sc-active-cart .sc-grid-item-product-title");
    private final By subtotal = By.cssSelector("#sc-subtotal-amount-activecart, #sc-subtotal-amount-buybox");
    private final By proceedToCheckout = By.cssSelector(
            "input[name='proceedToRetailCheckout'], #sc-buy-box-ptc-button input, #sc-buy-box-ptc-button");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isDisplayed(cartHeader, 15);
    }

    public int getItemCount() {
        return findAll(activeCartItems).size();
    }

    public List<String> getItemTitles() {
        return findAll(itemTitles).stream().map(WebElement::getText).map(String::trim)
                .filter(t -> !t.isEmpty()).distinct().toList();
    }

    public String getSubtotal() {
        return isDisplayed(subtotal, 5) ? getText(subtotal) : "";
    }

    public CheckoutPage proceedToCheckout() {
        click(proceedToCheckout);
        return new CheckoutPage(driver);
    }
}
