package twitter.scraper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openqa.selenium.WebDriver;
import twitter.controller.DriverManager;
import twitter.navigators.SiteQuery;
import twitter.navigators.SiteScroller;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Scraper {
    protected final String DATA_ROOT_DIR = "data/";
    protected final String X_LOGIN_DATA_ROOT_DIR = "data/x_account/";
    protected WebDriver driver;
    protected SiteScroller siteScroller;
    protected SiteQuery siteQuery;

    public Scraper(String proxy, boolean headless) {
        driver = DriverManager.initializeDriver(proxy, headless);
        if (driver == null) {
            return ;
        }
        siteScroller = new SiteScroller(driver);
    }
    public Scraper(WebDriver driver) {
        this.driver = driver;
        siteScroller = new SiteScroller(driver);
    }
    public void quitDriver() {
        driver.quit();
    }
}
