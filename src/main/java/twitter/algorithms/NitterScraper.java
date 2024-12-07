package twitter.algorithms;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import twitter.controller.DriverManager;
import twitter.navigators.NitterQuery;
import twitter.navigators.SiteQuery;
import twitter.navigators.SiteScroller;
import twitter.navigators.TwitterQuery;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NitterScraper extends Scraper {
    private NitterTweetScraper nitterTweetScraper;
    private NitterUserScraper nitterUserScraper;

    public NitterScraper(String proxy, boolean headless) {
        super(proxy, headless);
        siteQuery = new NitterQuery(driver);
        nitterTweetScraper = new NitterTweetScraper(driver, siteScroller, siteQuery);
        nitterUserScraper = new NitterUserScraper(driver, siteScroller, siteQuery);
        siteQuery.goToHome();
        try {
            Thread.sleep(3000);
        }
        catch (InterruptedException _) {}
    }
    public NitterScraper(WebDriver driver) {
        super(driver);
        siteQuery = new NitterQuery(driver);
        nitterTweetScraper = new NitterTweetScraper(driver, siteScroller, siteQuery);
        siteQuery.goToHome();
        try {
            Thread.sleep(3000);
        }
        catch (InterruptedException _) {}
    }
}
