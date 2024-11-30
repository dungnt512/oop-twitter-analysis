package org.example;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.sql.Driver;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Scroller {
    int currentPosition = 0;
    int lastPosition = 0;
    boolean scrolling;
    int scrollingCount = 0;
    WebDriver driver;
    JavascriptExecutor js;

    Scroller(WebDriver driver) {
        this.driver = driver;
        currentPosition = 0;
        js = (JavascriptExecutor) driver;
        //noinspection DataFlowIssue
        String temp = js.executeScript("return window.scrollY;").toString();
        lastPosition = Integer.parseInt(temp);
        scrolling = true;
        scrollingCount = 0;
    }

    void reset() {
        currentPosition = 0;
        //noinspection DataFlowIssue
        String temp = js.executeScript("return window.scrollY;").toString();
        lastPosition = Integer.parseInt(temp);
        scrollingCount = 0;
    }
    void scrollToElement(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView();", element);
    }
    void scrollToTop() {
        js.executeScript("window.scrollTo(0, 0);");
    }
    void scrollToBottom() {
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }
    void updateScrollPosition() {
        //noinspection DataFlowIssue
        String temp = js.executeScript("return window.scrollY;").toString();
        currentPosition = Integer.parseInt(temp);
    }
}
