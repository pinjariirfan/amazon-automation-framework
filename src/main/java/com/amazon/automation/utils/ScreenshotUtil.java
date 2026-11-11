package com.amazon.automation.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ScreenshotUtil {

    private static final Path DIR = Paths.get("screenshots");

    private ScreenshotUtil() {
    }

    public static Path capture(WebDriver driver, String name) {
        try {
            Files.createDirectories(DIR);
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path target = DIR.resolve(name.replaceAll("[^a-zA-Z0-9_-]", "_") + "_" + stamp + ".png");
            Path src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE).toPath();
            Files.copy(src, target, StandardCopyOption.REPLACE_EXISTING);
            return target;
        } catch (IOException | RuntimeException e) {
            System.err.println("Could not capture screenshot: " + e.getMessage());
            return null;
        }
    }
}
