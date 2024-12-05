package twitter.algorithms;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import twitter.entity.ProgressPrinter;
import twitter.entity.Tweet;
import twitter.entity.User;
import org.openqa.selenium.WebDriver;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import twitter.controller.DriverManager;
import twitter.controller.JsonFileManager;
import twitter.navigators.NitterQuery;
import twitter.navigators.SiteQuery;
import twitter.navigators.SiteScroller;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
public class NitterTweetScraper extends Scraper {
    private final String DATA_ROOT_DIR = "data/";
    private final String USERS_SCRAPE_FILE = DATA_ROOT_DIR + "users.json";
    private final String USER_TWEETS_SCRAPE_FILE = DATA_ROOT_DIR + "userTweets.json";

    public NitterTweetScraper(WebDriver driver, SiteScroller siteScroller, SiteQuery siteQuery) {
        this.driver = driver;
        this.siteQuery = siteQuery;
        this.siteScroller = siteScroller;
    }

    @SuppressWarnings("BusyWait")
    public User getTweetsOfUser(User user, String searchString, int tweetsLimit) throws InterruptedException {
        final int tweetsGetLimit = tweetsLimit * 3;
        final int tweetsMaxLimit = tweetsLimit * 6;
        String userId = user.getUsername();
        siteQuery.goToUserSearch(userId, searchString);
        List<Tweet> tweets = new ArrayList<>();
        ProgressPrinter progressPrinter = new ProgressPrinter("get tweets of '" + user.getUsername() + "'", tweetsGetLimit, 5);
        if (user.getUsername().charAt(0) == '@') {
            user.setUsername(user.getUsername().substring(1));
        }
        Thread.sleep(3000);
        int counter = 0;
        while (tweets.size() < tweetsGetLimit) {
            if (counter >= tweetsMaxLimit) {
                break;
            }
            List<WebElement> tweetCards = driver.findElements(By.xpath("//div[@class='timeline']//div[@class='timeline-item ']"));
//            System.out.println(tweetCards.size());
            for (WebElement tweetCard : tweetCards) {
                String tweetLink;
                counter++;
                if (counter >= tweetsMaxLimit) {
                    break;
                }
                try {
                    tweetLink = tweetCard.findElement(By.xpath(".//a[contains(@href, '" + user.getUsername() + "/status/')]")).getAttribute("href");
                    if (tweetLink == null) {
                        continue;
                    }
                }
                catch (Exception _) {
                    continue;
                }
                Tweet tweet = new Tweet(tweetLink);
                List<WebElement> tweetStats = tweetCard.findElements(By.xpath(".//span[@class='tweet-stat']"));
//                System.out.println(tweetStats.size());
                for (WebElement tweetStat : tweetStats) {
//                    System.out.print(tweetStat.getText() + " ");
                    try {
                        tweetStat.findElement(By.xpath(".//span[@class='icon-comment']")).getText();
                        tweet.setNumberOfComments(Integer.parseInt(tweetStat.getText()));
                    }
                    catch (Exception _) { }
                    try {
                        tweetStat.findElement(By.xpath(".//span[@class='icon-retweet']")).getText();
                        tweet.setNumberOfRetweets(Integer.parseInt(tweetStat.getText()));
                    }
                    catch (Exception _) { }
                    try {
                        tweetStat.findElement(By.xpath(".//span[@class='icon-quote']")).getText();
                        tweet.setNumberOfQuotes(Integer.parseInt(tweetStat.getText()));
                    }
                    catch (Exception _) { }
                    try {
                        tweetStat.findElement(By.xpath(".//span[@class='icon-heart']")).getText();
                        tweet.setNumberOfHearts(Integer.parseInt(tweetStat.getText()));
                    }
                    catch (Exception _) { }
                }

                tweets.add(tweet);
//                scroller.scrollToElement(tweetCard);
//                System.err.println(tweets.size());
                progressPrinter.printProgress(tweets.size(), false);
                if (tweets.size() >= tweetsGetLimit) {
                    break;
                }
            }

            if (tweets.size() < tweetsGetLimit) {
                int retryCount = 3;
                while (retryCount > 0) {
                    try {
                        WebElement loadMoreBtn = driver.findElement(By.xpath("//div[@class='show-more']//a"));
                        loadMoreBtn.click();
                        Thread.sleep(1500);
                        break;
                    } catch (Exception e) {
                        driver.navigate().refresh();
                        Thread.sleep(3000);
                        retryCount--;
                    }
                }
                if (retryCount == 0) {
                    System.err.println("No more tweets of '" + user.getUsername() + "'!");
                    break;
                }
            }
        }

        progressPrinter.printProgress(tweets.size(), true);
        tweets.sort(new Tweet.SortTweets());
//        tweets = tweets.subList(0, Math.min(tweets.size(), tweetsLimit));
        user.setTweets(tweets);

        return user;
    }

    @SuppressWarnings("unchecked")
    public void getTweetsOfUsers(int limit, int tweetsLimit, String searchString) throws InterruptedException {
        Set<String> userIds;
        System.err.println("Preparing to get Tweets...");

        userIds = (Set<String>) JsonFileManager.fromJson(USERS_SCRAPE_FILE, true, Set.class);
        Map<String, User> users = JsonFileManager.fromJsonToMap(USER_TWEETS_SCRAPE_FILE, true);

        int counter = 0, numberOfUsers = userIds.size();
        if (limit == 0) {
            limit = numberOfUsers;
        }
        ProgressPrinter progressPrinter = new ProgressPrinter("get tweets", limit);
        for (String userId : userIds) {
            if (counter >= limit) {
                break;
            }
            if (users.containsKey(userId) || (users.get(userId) != null && !users.get(userId).getTweets().isEmpty())) {
                System.err.println("Tweets of " + userId + " already crawled!");
                counter++;
                continue;
            }
            User user = new User(userId);
            user = getTweetsOfUser(user, searchString, tweetsLimit);
            users.put(userId, user);

            counter++;
            JsonFileManager.toJsonFromMap(USER_TWEETS_SCRAPE_FILE, users, true);
            progressPrinter.printProgress(counter, false);
        }
    }

    public void quitDriver() {
        DriverManager.quitDriver(driver);
    }
}
