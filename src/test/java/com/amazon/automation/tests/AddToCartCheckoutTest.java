package com.amazon.automation.tests;

import com.amazon.automation.config.ConfigReader;
import com.amazon.automation.pages.CartPage;
import com.amazon.automation.pages.CheckoutPage;
import com.amazon.automation.pages.HomePage;
import com.amazon.automation.pages.ProductPage;
import com.amazon.automation.pages.SearchResultsPage;
import com.amazon.automation.utils.ScreenshotUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * End-to-end flow on amazon.com:
 * open home page -> search -> open product -> add to cart -> verify cart -> proceed to checkout.
 * The steps depend on each other, so they run in order via dependsOnMethods.
 */
public class AddToCartCheckoutTest extends BaseTest {

    private HomePage homePage;
    private SearchResultsPage resultsPage;
    private String addedProductTitle;
    private CartPage cartPage;

    @Test(description = "Open amazon.com and verify the home page loads")
    public void openAmazonHomePage() {
        homePage = new HomePage(driver).open(baseUrl);
        Assert.assertTrue(homePage.isLoaded(), "Amazon home page did not load (search box/logo missing)");
        Assert.assertTrue(homePage.getTitle().contains("Amazon"), "Unexpected title: " + homePage.getTitle());
        System.out.println("Home page title: " + homePage.getTitle());

        String zip = ConfigReader.get("delivery.zip");
        homePage.setDeliveryZip(zip);
        System.out.println("Delivery location: " + homePage.getDeliverToText());
        Assert.assertTrue(homePage.getDeliverToText().contains(zip), "Delivery ZIP was not applied");
        ScreenshotUtil.capture(driver, "01_home_page");
    }

    @Test(dependsOnMethods = "openAmazonHomePage", description = "Search for a product")
    public void searchProduct() {
        String keyword = ConfigReader.get("search.keyword");
        resultsPage = homePage.searchFor(keyword);
        Assert.assertTrue(resultsPage.hasResults(), "No search results for: " + keyword);
        System.out.println("Searched for: " + keyword);
        ScreenshotUtil.capture(driver, "02_search_results");
    }

    @Test(dependsOnMethods = "searchProduct", description = "Open a product and add it to the cart")
    public void addItemToCart() {
        int maxTries = ConfigReader.getInt("max.products.to.try");
        List<String> productUrls = resultsPage.getProductUrls(maxTries);
        Assert.assertFalse(productUrls.isEmpty(), "No product links found on results page");

        for (String url : productUrls) {
            ProductPage productPage = resultsPage.openProduct(url);
            String title = productPage.getProductTitle();
            if (!productPage.canAddToCart()) {
                System.out.println("Skipping (no direct Add to Cart): " + title);
                continue;
            }
            if (productPage.addToCart()) {
                addedProductTitle = title;
                System.out.println("Added to cart: " + title);
                ScreenshotUtil.capture(driver, "03_added_to_cart");
                cartPage = productPage.goToCart(baseUrl);
                return;
            }
        }
        Assert.fail("Could not add any of the first " + productUrls.size() + " products to the cart");
    }

    @Test(dependsOnMethods = "addItemToCart", description = "Verify the item is present in the cart")
    public void verifyCart() {
        Assert.assertTrue(cartPage.isLoaded(), "Cart page did not load");
        Assert.assertTrue(cartPage.getItemCount() >= 1, "Cart is empty");
        List<String> titles = cartPage.getItemTitles();
        System.out.println("Cart items: " + titles);
        System.out.println("Cart subtotal: " + cartPage.getSubtotal());

        String expectedPrefix = addedProductTitle.length() > 25 ? addedProductTitle.substring(0, 25) : addedProductTitle;
        boolean found = titles.stream().anyMatch(t -> t.startsWith(expectedPrefix) || addedProductTitle.startsWith(t.replace("…", "").trim()));
        Assert.assertTrue(found, "Added product '" + addedProductTitle + "' not found in cart: " + titles);
        ScreenshotUtil.capture(driver, "04_cart");
    }

    @Test(dependsOnMethods = "verifyCart", description = "Proceed to checkout")
    public void proceedToCheckout() {
        CheckoutPage checkoutPage = cartPage.proceedToCheckout();
        ScreenshotUtil.capture(driver, "05_checkout");
        System.out.println("Checkout URL: " + checkoutPage.getCurrentUrl());

        // Guest users are sent to Sign-In; a signed-in session lands on the checkout page.
        boolean reachedCheckout = checkoutPage.isSignInRequired() || checkoutPage.isCheckoutPageDisplayed();
        Assert.assertTrue(reachedCheckout, "Checkout did not start. URL: " + checkoutPage.getCurrentUrl());
        if (checkoutPage.isSignInRequired()) {
            System.out.println("Checkout reached Amazon Sign-In page: '" + checkoutPage.getHeading()
                    + "' (expected for a guest session — no order is placed)");
        }
    }
}
