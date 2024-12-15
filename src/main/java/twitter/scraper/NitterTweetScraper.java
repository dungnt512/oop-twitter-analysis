package twitter.scraper;

import com.google.gson.reflect.TypeToken;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import twitter.entity.ProgressPrinter;
import twitter.entity.Tweet;
import twitter.entity.User;
import org.openqa.selenium.WebDriver;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import twitter.controller.JsonFileManager;
import twitter.navigators.SiteQuery;
import twitter.navigators.SiteScroller;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
public class NitterTweetScraper extends Scraper {
    private final String USERS_SCRAPE_FILE = DATA_ROOT_DIR + "users.json";
    private final String USER_TWEETS_SCRAPE_FILE = DATA_ROOT_DIR + "userTweets.json";
    private final int MINIMUM_NUMBER_OF_FOLLOWERS = 1000;

    public NitterTweetScraper(WebDriver driver, SiteScroller siteScroller, SiteQuery siteQuery) {
        super(driver, siteScroller, siteQuery);
    }

    @SuppressWarnings("BusyWait")
    public User getTweetsOfUser(User user, int base) throws InterruptedException {
        final int tweetsLimit = (int)(base * Math.pow(Math.log10(user.getFollowersCount()), 3) / 100);
        final int tweetsGetLimit = (int)(tweetsLimit * 8L / 7);
        final int tweetsMaxLimit = (int)(tweetsGetLimit * 5L / 4);
        List<Tweet> tweets = new ArrayList<>();
        if (user.getUsername().charAt(0) == '@') {
            user.setUsername(user.getUsername().substring(1));
        }
        ProgressPrinter progressPrinter = new ProgressPrinter("get tweets of '" + user.getUsername() + "'", tweetsGetLimit, 20);
        String userId = user.getUsername();
        Thread.sleep(2500);
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
                    //noinspection deprecation
                    tweetLink = tweetCard.findElement(By.xpath(".//a[contains(@href, '" + user.getUsername() + "/status/')]")).getAttribute("href");
//                    tweetLink = tweetCard.findElement(By.xpath(".//a[contains(@href, '/status/')]")).getAttribute("href");
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
                int retryCount = 2;
                while (retryCount > 0) {
                    try {
                        WebElement loadMoreBtn = driver.findElement(By.xpath("//div[@class='show-more']//a"));
                        loadMoreBtn.click();
                        Thread.sleep(1500);
                        break;
                    } catch (Exception e) {
                        driver.navigate().refresh();
                        Thread.sleep(2000);
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

    public void getTweetsOfUsers(int limit, int base, String... searchStrings) throws InterruptedException {
        Map<String, User> userIds;
        System.err.println("Preparing to get Tweets...");

        userIds = JsonFileManager.fromJson(USERS_SCRAPE_FILE, true, new TypeToken<Map<String, User>>() {}.getType());
        Map<String, User> users = JsonFileManager.fromJson(USER_TWEETS_SCRAPE_FILE, true, new TypeToken<Map<String, User>>() {}.getType());
        if (users == null) users = new HashMap<>();
        else {
            List<String> deleteIds = new ArrayList<>();
            List<User> addIds = new ArrayList<>();
            for (Map.Entry<String, User> user: users.entrySet()) {
                String id = user.getKey();
                if (id.charAt(0) == '@') {
                    id = id.substring(1);
                    if (users.containsKey(id)) {
                        deleteIds.add(user.getKey());
                    }
                    else {
                        deleteIds.add(user.getKey());
                        addIds.add(user.getValue());
                    }
                }
            }
            for (String id : deleteIds) {
                users.remove(id);
            }
            for (User user : addIds) {
                users.put(user.getUsername(), user);
            }
        }

        if (userIds == null) {
            userIds = new HashMap<>();
        }
        int counter = 0, numberOfUsers = userIds.size();
        if (limit == 0) {
            limit = numberOfUsers;
        }
        int numSearchString = searchStrings.length;
        progressPrinter = new ProgressPrinter("Get tweets", limit);

        for (User user : userIds.values()) {
            if (counter >= limit) {
                break;
            }
//            if (users.containsKey(user.getUsername()) && (users.get(user.getUsername()) != null && !users.get(user.getUsername()).getTweets().isEmpty())) {
            if (users.containsKey(user.getUsername())) {
                System.err.println("Tweets of " + user.getUsername() + " already scraped!");
                progressPrinter.update(counter);
                counter++;
                continue;
            }
            if (numSearchString > 0) {
                siteQuery.goToUserSearches(user.getUsername(), searchStrings);
            }
            else {
                siteQuery.goToUser(user.getUsername(), "");
            }
            user = getTweetsOfUser(user, base);
            users.put(user.getUsername(), user);

            counter++;
//            JsonFileManager.toJsonFromMap(USER_TWEETS_SCRAPE_FILE, users, false);
            JsonFileManager.toJson(USER_TWEETS_SCRAPE_FILE, users, false);
//            progressPrinter.printProgress(counter, false);
            printProgress(counter, false);
        }
        printProgress(counter, true);
    }
}
