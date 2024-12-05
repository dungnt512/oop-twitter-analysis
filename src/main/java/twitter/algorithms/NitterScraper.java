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

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NitterScraper extends Scraper {
    private NitterTweetScraper nitterTweetScraper;

    public NitterScraper(String proxy, boolean headless) {
        super(proxy, headless);
        nitterTweetScraper = new NitterTweetScraper(driver, siteScroller, siteQuery);
    }
    public NitterScraper(WebDriver driver) {
        super(driver);
        nitterTweetScraper = new NitterTweetScraper(driver, siteScroller, siteQuery);
    }
}
