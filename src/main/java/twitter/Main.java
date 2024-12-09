package twitter;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import twitter.algorithms.*;
import twitter.algorithms.NitterScraper;
import twitter.controller.DriverManager;
import twitter.entity.LoginAccount;
import twitter.entity.ProgressPrinter;
import twitter.entity.TimePrinter;

import java.io.FileReader;
import java.util.*;

@Getter
@Setter
@ToString
public class Main {
    public static void main(String[] args) throws InterruptedException {
        WebDriver driver = null;
        while (true) {
            try {
                List<String> proxyList = new ArrayList<>();
                proxyList.add("");
//                proxyList.add("148.72.165.185:10501");
//                proxyList.add("35.161.172.205:1080");
//                proxyList.add("52.35.240.119:1080");
//                proxyList.add("148.72.165.184:10501");

                for (String proxy : proxyList) {
//                    TwitterScraper scraper = new TwitterScraper(proxy, false, true);
//                    Thread.sleep(3000);
//                    scraper.getTwitterUserScraper().getUserSearches(5000, "hyperledger fabric", "ethereum", "corda", "quorum", "#blockchain", "crypto", "#crypto");
//                    scraper.getTwitterUserScraper().getUsersFollowers(0);
//                    scraper.quitDriver();
//                    TwitterScraper twitterScraper = new TwitterScraper("", false);
//                    twitterScraper.login();
                    NitterScraper scraper = new NitterScraper(proxy, false);
                    driver = scraper.getDriver();
//                    scraper.getNitterUserScraper().getInfoOfUsers(0);
//                    scraper.getNitterTweetScraper().getTweetsOfUsers(0, 50, "blockchain", "ethereum", "crypto", "hyperledger fabric");
                    scraper.getNitterTweetScraper().getTweetsOfUsers(0, 50);
                    scraper.quitDriver();
                }
                break;
            } catch (Exception e) {
                assert driver != null;
                driver.quit();
            }
        }
    }
}