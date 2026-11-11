package com.amazon.automation.listeners;

import com.amazon.automation.driver.DriverFactory;
import com.amazon.automation.utils.ScreenshotUtil;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.nio.file.Path;

/**
 * Logs test status and captures a screenshot on failure.
 */
public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("\n>>> START: " + result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("<<< PASS : " + result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("<<< FAIL : " + result.getMethod().getMethodName() + " -> " + result.getThrowable());
        try {
            Path shot = ScreenshotUtil.capture(DriverFactory.getDriver(), "FAILED_" + result.getMethod().getMethodName());
            if (shot != null) {
                System.out.println("    Screenshot: " + shot.toAbsolutePath());
            }
        } catch (IllegalStateException ignored) {
            // driver not available
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("<<< SKIP : " + result.getMethod().getMethodName());
    }
}
