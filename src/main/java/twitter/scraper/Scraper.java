package twitter.scraper;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openqa.selenium.WebDriver;
import twitter.controller.DriverManager;
import twitter.entity.ProgressPrinter;
import twitter.navigators.SiteQuery;
import twitter.navigators.SiteScroller;
import twitter.entity.TaskVoid;

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
    protected ProgressPrinter progressPrinter;
    protected DoubleProperty progress;
    protected StringProperty message;

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

    public Scraper(WebDriver driver, SiteScroller siteScroller, SiteQuery siteQuery) {
        this.driver = driver;
        this.siteScroller = siteScroller;
        this.siteQuery = siteQuery;
    }

    public void printProgress(long progress, boolean forced) {
//        System.err.println(this.progress + " " + this.message);
        progressPrinter.printProgress(progress, forced);
        this.progress.set((double)progressPrinter.getLastPercent() / progressPrinter.getMAX_PERCENT());
        this.message.set(progressPrinter.getLastMessage());
    }

    public void quitDriver() {
        driver.quit();
    }
}
