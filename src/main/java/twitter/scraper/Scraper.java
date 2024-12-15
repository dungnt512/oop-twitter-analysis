package twitter.scraper;

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
    protected TaskVoid task;

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

    public void printProgress(int progress, boolean forced) {
        progressPrinter.printProgress(progress, forced);
        if (task != null) {
            task.updateProgress(progressPrinter.getLastPercent(), progressPrinter.getMAX_PERCENT());
            task.updateMessage(progressPrinter.getLastMessage());
        }
    }

    public void quitDriver() {
        driver.quit();
    }
}
