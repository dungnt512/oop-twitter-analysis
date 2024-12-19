package twitter.scraper;
import com.google.gson.reflect.TypeToken;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import twitter.entity.ProgressPrinter;

import twitter.entity.Tweet;
import twitter.entity.User;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.JavascriptExecutor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import twitter.controller.JsonFileManager;
import twitter.navigators.SiteQuery;
import twitter.navigators.SiteScroller;

import java.util.*;

public class NitterUserScraper extends Scraper {
    private final String USER_IDS_SCRAPE_FILE = DATA_ROOT_DIR + "userIds.json";
    private final String USERS_SCRAPE_FILE = DATA_ROOT_DIR + "users.json";
    private final String USER_COMMENTS_SCRAPE_FILE = DATA_ROOT_DIR + "userComments.json";
    private final String USER_EXCLUDE_FILE = DATA_ROOT_DIR + "userExclude.json";
    private int MINIMUM_FOLLOWERS_COUNT = 150;

    public NitterUserScraper(WebDriver driver, SiteScroller siteScroller, SiteQuery siteQuery) {
        super(driver, siteScroller, siteQuery);
    }

    private int convertNitterNumber(String number) {
        StringBuilder num = new StringBuilder();
        for (char c : number.toCharArray()) {
            if (c >= '0' && c <= '9') {
                num.append(c);
            }
        }
        return Integer.parseInt(num.toString());
    }

    public User getInfoOfUser(User user) throws InterruptedException {
        if (user.getUsername().charAt(0) == '@') {
            user.setUsername(user.getUsername().substring(1));
        }
        String userId = user.getUsername();
        ProgressPrinter progressPrinter = new ProgressPrinter("get information of '" + user.getUsername() + "'", 1, 1);
        Thread.sleep(2000);
        final int retryLimit = 5;
        int retryCount = 0;
        while (retryCount < retryLimit) {
            try {
                WebElement profileCard = driver.findElement(By.xpath("//div[@class='profile-card']"));
                try {
                    user.setLocation(profileCard.findElement(By.xpath(".//div[@class='profile-location']")).getText());
                    user.setJoinDate(profileCard.findElement(By.xpath(".//div[@class='profile-joindate']")).getText());
                }
                catch (Exception _) {}
                user.setTweetsCount(convertNitterNumber(profileCard.findElement(By.xpath(".//li[@class='posts']//span[@class='profile-stat-num']")).getText()));
                user.setFollowingCount(convertNitterNumber(profileCard.findElement(By.xpath(".//li[@class='following']//span[@class='profile-stat-num']")).getText()));
                user.setFollowersCount(convertNitterNumber(profileCard.findElement(By.xpath(".//li[@class='followers']//span[@class='profile-stat-num']")).getText()));
                user.setLikesCount(convertNitterNumber(profileCard.findElement(By.xpath(".//li[@class='likes']//span[@class='profile-stat-num']")).getText()));
                break;
            } catch (Exception e) {
////            noinspection CallToPrintStackTrace
//            e.printStackTrace();
                driver.navigate().refresh();
                Thread.sleep(3000);
                ((JavascriptExecutor)driver).executeScript("window.stop();");
                retryCount++;
            }
        }

        progressPrinter.printProgress(1, false);
        return user;
    }

    public void getInfoOfUsers(int limit) throws InterruptedException {
        Set<String> userIds;
        System.err.println("Preparing to get Users...");

        userIds = JsonFileManager.fromJson(USER_IDS_SCRAPE_FILE, true, new TypeToken<Set<String>>() {}.getType());
        if (userIds == null) userIds = new HashSet<>();
        Map<String, User> users = JsonFileManager.fromJson(USERS_SCRAPE_FILE, true, new TypeToken<Map<String, User>>() {}.getType());
        if (users == null) users = new HashMap<>();

        int numberOfUsers = userIds.size();
        if (limit == 0) {
            limit = numberOfUsers;
        }
        progressPrinter = new ProgressPrinter("Get user profile", limit);
        Set<String> excludeUser = JsonFileManager.fromJson(USER_EXCLUDE_FILE, true, new TypeToken<Set<String>>() {}.getType());
        if (excludeUser == null) excludeUser = new HashSet<>();
        int counter = 0;
        for (String userId : userIds) {
            if (users.size() >= limit) {
                break;
            }
            counter++;
            if (excludeUser.contains(userId)) {
                System.err.println("Followers of " + userId + " is too low! Remove from list");
                progressPrinter.update(counter);
                continue;
            }
            if (users.containsKey(userId)) {
                User user = users.get(userId);
                if (user.getFollowersCount() < MINIMUM_FOLLOWERS_COUNT) {
                    System.err.println("Followers of " + userId + " is too low! Remove from list");
                    excludeUser.add(userId);
                    JsonFileManager.toJson(USER_EXCLUDE_FILE, excludeUser, false);
                    users.remove(userId);
                    JsonFileManager.toJson(USERS_SCRAPE_FILE, users, false);
                    continue;
                }
                System.err.println("Information of " + userId + " already scraped!");
                progressPrinter.update(counter);
                continue;
            }
            User user = new User(userId);
            siteQuery.goToUser(userId, "search");
            user = getInfoOfUser(user);
            if (user.getFollowersCount() >= MINIMUM_FOLLOWERS_COUNT) {
                users.put(userId, user);
//                JsonFileManager.toJsonFromMap(USERS_SCRAPE_FILE, users, false);
                JsonFileManager.toJson(USER_EXCLUDE_FILE, users, false);
//                progressPrinter.printProgress(counter, false);
                printProgress(counter, false);
            }
            else {
                System.err.println("Followers of " + userId + " is too low! Remove from list");
                excludeUser.add(userId);
                JsonFileManager.toJson(USER_EXCLUDE_FILE, excludeUser, false);
            }
        }

        List<User> userList = new ArrayList<>(users.values());
        userList.sort(new User.SortUsers());
        userIds.removeAll(excludeUser);
        printProgress(counter, true);
        JsonFileManager.toJson(USER_EXCLUDE_FILE, excludeUser, true);
        JsonFileManager.toJson(USER_IDS_SCRAPE_FILE, userIds, true);
        JsonFileManager.toJson(USERS_SCRAPE_FILE, users, true);
    }

    private Set<String> getUsers() {
        Set<String> commenters = new HashSet<>();

        try {
            for (int i = 0; i < 5; i++) {
                List<WebElement> titles = driver.findElements(By.xpath("//a[@class='username']"));
                for (WebElement title : titles) {
                    String username = title.getText().trim();
                    if (!username.isEmpty()) {
                        commenters.add(username);
                    }
                }
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
                    System.err.println("No more comments!");
                    break;
                }
            }
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace(); // Log lỗi nếu cần
        }

        return commenters;
    }

    public void getUserComments(int limit) throws Exception {
        Map<String, User> users = JsonFileManager.fromJson(USER_COMMENTS_SCRAPE_FILE, true, new TypeToken<Map<String, User>>() {}.getType());
        if (users == null) users = new HashMap<>();
        if (limit == 0) {
            limit = users.size();
        }

        progressPrinter = new ProgressPrinter("Get user comments", limit);
        int counter = 0;
        for (User user : users.values()) {
            if (user.getTweets() == null) user.setTweets(new ArrayList<>());
            for (Tweet tweet : user.getTweets()) {
                siteQuery.goToLink(tweet.getTweetLink());
                Thread.sleep(3000);
                Set<String> userIds = getUsers();
                tweet.setComments(userIds.stream().toList());
            }
            JsonFileManager.toJson(USER_COMMENTS_SCRAPE_FILE, users, true);
            counter++;
            printProgress(counter, false);
        }
        printProgress(counter, true);
    }

    public void setMINIMUM_FOLLOWERS_COUNT(int MINIMUM_FOLLOWERS_COUNT) {
        this.MINIMUM_FOLLOWERS_COUNT = MINIMUM_FOLLOWERS_COUNT;
    }
}
