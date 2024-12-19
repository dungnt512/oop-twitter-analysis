package twitter.controller;

import java.util.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.*;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class DriverManager {
    private ChromeOptions getChromeOptions(String proxyAddress, boolean headless) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-dev-shm-usage", "--ignore-certificate-error",
                "--disable-gpu", "--log-level-3", "--disable-notifications", "--disable-popup-blocking",
                "--no-sandbox");
//        options.addArguments("--user-agent={}".format(header));
//        options.addArguments("--disable-notifications", "--disable-popup-blocking",
//                "--no-sandbox");

        if (!proxyAddress.isEmpty()) {
            Proxy proxy = new Proxy();
            proxy.setHttpProxy(proxyAddress);
            proxy.setSslProxy(proxyAddress);
            options.setProxy(proxy);
        }
        if (headless) {
            options.addArguments("--headless");
        }
        return options;
    }

    public static WebDriver initializeDriver(String proxyAddress, boolean headless) {
        System.out.println("Initialize WebDriver...");
        DriverManager driverManager = new DriverManager();
        ChromeOptions options = driverManager.getChromeOptions(proxyAddress, headless);
        WebDriver driver = null;
        try {
            System.out.println("Initialize ChromeDriver...");
            driver = new ChromeDriver(options);
            driver.manage().window().maximize();
            System.out.println("Setup Complete");
            return driver;
        }
        catch (WebDriverException e) {
            System.out.println("Initialize ChromeDriver failed");
        }
        return driver;
    }

    public static Set<Cookie> getCookies(WebDriver driver) {
        return driver.manage().getCookies();
    }
    public static void addCookies(WebDriver driver, Set<Cookie> cookies) {
        for (Cookie cookie : cookies) {
            driver.manage().addCookie(cookie);
        }
    }
    public static void refresh(WebDriver driver) {
        driver.navigate().refresh();
    }
    public static void quitDriver(WebDriver driver) {
        driver.quit();
    }
}
