package com.example;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.net.URL;
import java.time.Duration;
import java.util.List;

public class FacebookLoginTest {
    private AppiumDriver driver;
    private WebDriverWait wait;
    
    private static final String DEVICE_ID = "emulator-5554";
    private static final String APP_PACKAGE = "com.facebook.lite";
    private static final String APP_ACTIVITY = "com.facebook.lite.MainActivity";
    private static final String APPIUM_SERVER_URL = "http://127.0.0.1:4723";
    private static final int WAIT_TIMEOUT = 15;
    
    private static final String TEST_EMAIL = "email@email.com";
    private static final String TEST_PASSWORD = "password";

    @BeforeMethod
    public void setUp() throws Exception {
        clearAppData();
        forceStopApp();
        Thread.sleep(2000);
        
        UiAutomator2Options options = new UiAutomator2Options()
            .setPlatformName("Android")
            .setDeviceName(DEVICE_ID)
            .setAppPackage(APP_PACKAGE)
            .setAppActivity(APP_ACTIVITY)
            .setNoReset(false)
            .setAutomationName("UiAutomator2");
        
        driver = new AndroidDriver(new URL(APPIUM_SERVER_URL), options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT));
        
        Thread.sleep(3000);
    }

    @Test(priority = 1)
    public void testSuccessfulLogin() {
        try {
            Thread.sleep(3000);
            
            verifyAppIsRunning();
            handlePermissionDialogs();
            performLoginSteps();
            verifyLoginSuccess();
            
        } catch (Exception e) {
            printDebugInfo();
            e.printStackTrace();
            Assert.fail("Login test failed: " + e.getMessage());
        }
    }

    private void performLoginSteps() throws Exception {
        WebElement emailLabel = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//android.view.View[@text='Mobile number or email']")
        ));
        emailLabel.click();
        Thread.sleep(500);

        WebElement focusedInput = getFocusedElement();
        if (focusedInput == null) {
            printDebugInfo();
            Assert.fail("Could not find focused input after clicking email label");
        }
        focusedInput.sendKeys(TEST_EMAIL);
        Thread.sleep(500);

        WebElement passwordLabel = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//android.view.View[@text='Password']")
        ));
        passwordLabel.click();
        Thread.sleep(500);

        focusedInput = getFocusedElement();
        if (focusedInput == null) {
            printDebugInfo();
            Assert.fail("Could not find focused input after clicking password label");
        }
        focusedInput.sendKeys(TEST_PASSWORD);
        Thread.sleep(500);

        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//android.widget.Button[@content-desc='Log in']")
        ));
        loginButton.click();
        
        Thread.sleep(8000);
    }

    private void verifyLoginSuccess() {
        String pageSource = driver.getPageSource();
        if (pageSource.contains("Zapisać dane logowania?")) {
            Assert.assertTrue(true, "Login successful");
        } else {
            printDebugInfo();
            Assert.fail("Login failed");
        }
    }

    private WebElement getFocusedElement() {
        try {
            List<WebElement> all = driver.findElements(By.xpath("//*"));
            for (WebElement el : all) {
                String focused = el.getAttribute("focused");
                if ("true".equals(focused)) {
                    return el;
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding focused element: " + e.getMessage());
        }
        return null;
    }

    private void handlePermissionDialogs() {
        try {
            List<WebElement> allowButtons = driver.findElements(By.xpath(
                "//android.widget.Button[@text='Allow'] | " +
                "//android.widget.Button[@text='OK'] | " +
                "//android.widget.Button[@text='Continue'] | " +
                "//android.widget.Button[@text='Accept'] | " +
                "//android.widget.Button[contains(@text, 'Save')]"
            ));
            
            for (WebElement button : allowButtons) {
                if (button.isDisplayed()) {
                    button.click();
                    Thread.sleep(1000);
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling permission dialogs: " + e.getMessage());
        }
    }

    private void printDebugInfo() {
        try {
            String pageSource = driver.getPageSource();
            System.out.println(pageSource.substring(0, Math.min(2000, pageSource.length())));
        } catch (Exception e) {
            System.err.println("Could not get page source for debug: " + e.getMessage());
        }
    }

    private void clearAppData() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "adb", "-s", DEVICE_ID, "shell", "pm", "clear", APP_PACKAGE
            );
            Process process = pb.start();
            process.waitFor();
        } catch (Exception e) {
            System.err.println("Error clearing app data: " + e.getMessage());
        }
    }

    private void forceStopApp() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "adb", "-s", DEVICE_ID, "shell", "am", "force-stop", APP_PACKAGE
            );
            Process process = pb.start();
            process.waitFor();
        } catch (Exception e) {
            System.err.println("Error forcing stop the app: " + e.getMessage());
        }
    }

    private void verifyAppIsRunning() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                "//*[@package='" + APP_PACKAGE + "']"
            )));
            System.out.println("Facebook app is running");
        } catch (Exception e) {
            printDebugInfo();
            Assert.fail("Facebook app failed to start properly: " + e.getMessage());
        }
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
