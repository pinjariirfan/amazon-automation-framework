package com.amazon.automation.driver;

import com.amazon.automation.config.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Creates and holds a thread-safe WebDriver instance. Drivers are resolved
 * automatically by Selenium Manager (no WebDriverManager / manual driver needed).
 */
public final class DriverFactory {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    // A regular desktop UA so headless Chrome isn't served a stripped/blocked page.
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

    private DriverFactory() {
    }

    public static void initDriver() {
        String browser = ConfigReader.get("browser").toLowerCase();
        boolean headless = ConfigReader.getBoolean("headless");

        WebDriver driver = switch (browser) {
            case "edge" -> new EdgeDriver(edgeOptions(headless));
            case "firefox" -> new FirefoxDriver(firefoxOptions(headless));
            default -> new ChromeDriver(chromeOptions(headless));
        };

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getInt("page.load.timeout")));
        driver.manage().timeouts().implicitlyWait(Duration.ZERO); // explicit waits only
        if (!headless) {
            driver.manage().window().maximize();
        }
        DRIVER.set(driver);
    }

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("WebDriver not initialised. Call DriverFactory.initDriver() first.");
        }
        return driver;
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            DRIVER.remove();
        }
    }

    private static ChromeOptions chromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments(
                "--window-size=1920,1080",
                "--disable-notifications",
                "--disable-blink-features=AutomationControlled",
                "--lang=en-US",
                "--user-agent=" + USER_AGENT);
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("prefs", Map.of("intl.accept_languages", "en-US,en"));
        return options;
    }

    private static EdgeOptions edgeOptions(boolean headless) {
        EdgeOptions options = new EdgeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1920,1080", "--disable-notifications",
                "--disable-blink-features=AutomationControlled", "--user-agent=" + USER_AGENT);
        return options;
    }

    private static FirefoxOptions firefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }
        options.addArguments("--width=1920", "--height=1080");
        return options;
    }
}
