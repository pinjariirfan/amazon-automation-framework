# Amazon Automation Framework

End-to-end UI automation for **amazon.com** using **Selenium WebDriver (Java)**, **TestNG**, **Maven** and the **Page Object Model**.

## Flow covered

1. Open amazon.com and verify the home page loads
2. Set a US delivery ZIP (so products are shippable and show *Add to Cart*)
3. Search for a product (`search.keyword`)
4. Open a product from the results and **add it to the cart**
5. Open the cart and verify the item and subtotal
6. Click **Proceed to checkout** and verify that checkout starts (a guest session is sent to the Amazon Sign-In page)

> The framework **never places a real order**. Checkout ends at the Sign-In / checkout page.

## Tech stack

| Tool | Version |
|------|---------|
| Java | 21 |
| Selenium | 4.33.0 (Selenium Manager resolves the driver automatically) |
| TestNG | 7.10.2 |
| Maven | 3.9+ |

## Project structure

```
src/main/java/com/amazon/automation
├── config/ConfigReader.java        # config.properties + -D overrides
├── driver/DriverFactory.java       # ThreadLocal WebDriver (chrome | edge | firefox, headless)
├── listeners/TestListener.java     # logging + screenshot on failure
├── pages/                          # Page Objects
│   ├── BasePage.java
│   ├── HomePage.java
│   ├── SearchResultsPage.java
│   ├── ProductPage.java
│   ├── CartPage.java
│   └── CheckoutPage.java
└── utils/ScreenshotUtil.java
src/test/java/com/amazon/automation/tests
├── BaseTest.java                   # driver setup / teardown
└── AddToCartCheckoutTest.java      # end-to-end test
src/test/resources/config.properties
testng.xml
```

## Running

```bash
# headed (default)
mvn clean test

# headless
mvn clean test -Dheadless=true

# other browser / keyword
mvn clean test -Dbrowser=edge -Dsearch.keyword="usb c cable"
```

Screenshots of each step (and of failures) go to `screenshots/`. TestNG reports go to `target/surefire-reports/`.

## Configuration (`src/test/resources/config.properties`)

| Key | Default | Description |
|-----|---------|-------------|
| `base.url` | `https://www.amazon.com` | Site under test |
| `search.keyword` | `wireless mouse` | Product to search |
| `delivery.zip` | `10001` | US ZIP used as the delivery location |
| `max.products.to.try` | `6` | Results to try until one can be added to the cart |
| `browser` | `chrome` | `chrome`, `edge` or `firefox` |
| `headless` | `false` | Run without a visible browser |
| `explicit.wait` | `20` | Explicit wait, in seconds |

## Known limitation: Amazon bot checks

Amazon sometimes shows a human-verification ("I am human" / CAPTCHA) page to automated or headless sessions, often after *Add to Cart*. The framework detects this and fails with a clear `BotCheckException` message. It deliberately does **not** try to bypass the check. If you hit it, run in headed mode or from a network Amazon does not flag.
