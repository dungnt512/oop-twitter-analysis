package twitter.scraper;
import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openqa.selenium.*;
import twitter.controller.JsonFileManager;
import twitter.entity.LoginAccount;
import twitter.navigators.*;

import java.util.*;

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
