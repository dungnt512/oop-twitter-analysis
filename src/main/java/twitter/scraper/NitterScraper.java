package twitter.scraper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openqa.selenium.WebDriver;
import twitter.navigators.NitterQuery;

public class NitterScraper extends Scraper {
    private NitterTweetScraper nitterTweetScraper;
    private NitterUserScraper nitterUserScraper;

    public NitterScraper(String proxy, boolean headless) {
        super(proxy, headless);
        siteQuery = new NitterQuery(driver);
        nitterTweetScraper = new NitterTweetScraper(driver, siteScroller, siteQuery);
        nitterUserScraper = new NitterUserScraper(driver, siteScroller, siteQuery);
//        siteQuery.goToHome();
//        try {
//            Thread.sleep(3000);
//        }
//        catch (InterruptedException _) {}
    }
    public NitterScraper(WebDriver driver) {
        super(driver);
        siteQuery = new NitterQuery(driver);
        nitterTweetScraper = new NitterTweetScraper(driver, siteScroller, siteQuery);
        nitterUserScraper = new NitterUserScraper(driver, siteScroller, siteQuery);
//        siteQuery.goToHome();
//        try {
//            Thread.sleep(3000);
//        }
//        catch (InterruptedException _) {}
    }

    public NitterTweetScraper getNitterTweetScraper() {
        return nitterTweetScraper;
    }

    public NitterUserScraper getNitterUserScraper() {
        return nitterUserScraper;
    }
}
