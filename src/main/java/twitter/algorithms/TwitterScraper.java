package twitter.algorithms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openqa.selenium.*;
import twitter.controller.DriverManager;
import twitter.entity.LoginAccount;
import twitter.entity.ProgressPrinter;
import twitter.navigators.*;
import twitter.entity.User;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TwitterScraper extends Scraper {
    private List<Map<String, String>> scrapedTweets = new ArrayList<>();
    private TwitterLogin siteLogin;
    private TwitterUserScraper twitterUserScraper;

    public TwitterScraper(LoginAccount loginAccount, String proxy, boolean headless) {
        super(proxy, headless);
        siteLogin = new TwitterLogin(driver, loginAccount);
        if (!login())
            return ;
    }
    public TwitterScraper(String proxy, boolean headless) {
        super(proxy, headless);
        siteLogin = new TwitterLogin(driver);
    }
    public TwitterScraper(WebDriver driver) {
        super(driver);
        siteLogin = new TwitterLogin(driver);
    }
    public boolean login() {
        return siteLogin.login();
    }
}
