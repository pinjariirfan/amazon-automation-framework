package com.amazon.automation.tests;

import com.amazon.automation.config.ConfigReader;
import com.amazon.automation.driver.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public abstract class BaseTest {

    protected WebDriver driver;
    protected String baseUrl;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        DriverFactory.initDriver();
        driver = DriverFactory.getDriver();
        baseUrl = ConfigReader.get("base.url");
        System.out.println("Browser: " + ConfigReader.get("browser")
                + " | Headless: " + ConfigReader.getBoolean("headless")
                + " | URL: " + baseUrl);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}
