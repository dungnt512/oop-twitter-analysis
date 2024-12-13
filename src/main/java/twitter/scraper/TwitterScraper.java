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
public class TwitterScraper extends Scraper {
    private List<Map<String, String>> scrapedTweets = new ArrayList<>();
    private TwitterLogin siteLogin;
    private TwitterUserScraper twitterUserScraper;
    private final String SITE_LOGIN_COOKIES_FILE = X_LOGIN_DATA_ROOT_DIR + "siteLoginCookies.json";

    public TwitterScraper(LoginAccount loginAccount, String proxy, boolean headless, boolean loginWithCookies) {
        super(proxy, headless);
        siteLogin = new TwitterLogin(driver, loginAccount);
        siteQuery = new TwitterQuery(driver);
        twitterUserScraper = new TwitterUserScraper(driver, siteScroller, siteQuery);
        login(loginWithCookies);
    }
    public TwitterScraper(String proxy, boolean headless, boolean loginWithCookies) {
        super(proxy, headless);
        siteLogin = new TwitterLogin(driver);
        siteQuery = new TwitterQuery(driver);
        twitterUserScraper = new TwitterUserScraper(driver, siteScroller, siteQuery);
        login(loginWithCookies);

    }
    public TwitterScraper(WebDriver driver, boolean loginWithCookies) {
        super(driver);
        siteLogin = new TwitterLogin(driver);
        siteQuery = new TwitterQuery(driver);
        twitterUserScraper = new TwitterUserScraper(driver, siteScroller, siteQuery);
//        System.err.println(SITE_LOGIN_COOKIES_FILE);
        login(loginWithCookies);
    }
    public void login(boolean loginWithCookies) {
        if (!loginWithCookies) {
            Set<Cookie> beforeLoginCookies = driver.manage().getCookies();
            siteLogin.login();
            Set<Cookie> afterLoginCookies = driver.manage().getCookies();
            afterLoginCookies.removeAll(beforeLoginCookies);
            JsonFileManager.toJson(SITE_LOGIN_COOKIES_FILE, afterLoginCookies, false);
        }
        else {
            siteQuery.goToHome();
            Set<Cookie> loginCookies = JsonFileManager.fromJson(SITE_LOGIN_COOKIES_FILE, false, new TypeToken<Set<Cookie>>(){}.getType());
            for (Cookie loginCookie : loginCookies) {
                driver.manage().addCookie(loginCookie);
            }
            driver.navigate().refresh();
        }
    }
}
