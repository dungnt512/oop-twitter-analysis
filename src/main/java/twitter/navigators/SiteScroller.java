package twitter.navigators;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SiteScroller {
    private int currentPosition = 0;
    private int lastPosition = 0;
    private boolean scrolling;
    private int scrollingCount = 0;
    private WebDriver driver;
    private JavascriptExecutor js;

    public SiteScroller(WebDriver driver) {
        this.driver = driver;
        currentPosition = 0;
        js = (JavascriptExecutor) driver;
        //noinspection DataFlowIssue
        String temp = js.executeScript("return window.scrollY;").toString();
        lastPosition = Integer.parseInt(temp);
        scrolling = true;
        scrollingCount = 0;
    }

    public void reset() {
        currentPosition = 0;
        //noinspection DataFlowIssue
        String temp = js.executeScript("return window.scrollY;").toString();
        lastPosition = Integer.parseInt(temp);
        scrollingCount = 0;
    }
    public void scrollToElement(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView();", element);
    }
    public void scrollToTop() {
        js.executeScript("window.scrollTo(0, 0);");
    }
    public void scrollToBottom() {
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }
    public void updateScrollPosition() {
        //noinspection DataFlowIssue
        String temp = js.executeScript("return window.scrollY;").toString();
        currentPosition = Integer.parseInt(temp);
    }
}
