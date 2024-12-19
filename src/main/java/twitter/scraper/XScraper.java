package twitter.scraper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openqa.selenium.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class XScraper extends Scraper {
    TwitterScraper twitterScraper;
    NitterScraper nitterScraper;

    public XScraper(String proxy, boolean headless, boolean loginWithCookies) {
        super(proxy, headless);
        twitterScraper = new TwitterScraper(driver, loginWithCookies);
        nitterScraper = new NitterScraper(driver);
    }
    public XScraper(WebDriver driver, boolean loginWithCookies) {
        super(driver);
        twitterScraper = new TwitterScraper(driver, loginWithCookies);
        nitterScraper = new NitterScraper(driver);
    }
}
