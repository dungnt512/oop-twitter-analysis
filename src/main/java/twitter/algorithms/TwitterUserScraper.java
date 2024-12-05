package twitter.algorithms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import twitter.controller.JsonFileManager;
import twitter.entity.ProgressPrinter;
import twitter.entity.User;
import twitter.navigators.TwitterQuery;

import java.io.FileReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
public class TwitterUserScraper extends Scraper {
    private final String DATA_ROOT_DIR = "data/";
    private final String USERS_SCRAPE_FILE = DATA_ROOT_DIR + "users.json";
    private final String USER_FOLLOWERS_SCRAPE_FILE = DATA_ROOT_DIR + "userFollowers.json";
    private final String USER_FOLLOWING_SCRAPE_FILE = DATA_ROOT_DIR + "userFollowing.json";

    @SuppressWarnings("BusyWait")
    private Set<String> getUsers(int maxUsers, int delayMillis, int retryLimit) throws InterruptedException {
        Set<String> users = new HashSet<>();

        int countRetry = 0, countEmpty = 0, countRefresh = 0;
//        for (int iter = 0; iter < numberOfScroll; iter++)
        if (maxUsers == 0) {
            maxUsers = 3000;
        }
        if (retryLimit == 0) {
            retryLimit = 20;
        }
        ProgressPrinter progressPrinter = new ProgressPrinter("get username", maxUsers);

        while (users.size() < maxUsers) {
            try {
                List<WebElement> local = driver.findElements(By.xpath("//div[@class='css-175oi2r']//a//div//div//span[contains(text(),'@')]"));
                int m = local.size(), added = 0;
                for (int i = Math.max(0, m - 40); i < m; i++) {
                    try {
                        WebElement people = local.get(i);
                        String user;
                        try {
                            user = people.getText();
//                            user = user.substring(user.indexOf('@'));
                        }
                        catch (Exception e) {
                            continue;
                        }
                        if (!user.isEmpty() && !users.contains(user)) {
                            added++;
                            users.add(user);
                            siteScroller.scrollToElement(people);
                            int n = users.size();
                            progressPrinter.update(n);
                            Thread.sleep(200);
                        }
                    }
                    catch (Exception e) {
                        continue;
                    }
                }

                if (added == 0) {
                    try {
                        while (countRetry < retryLimit) {
                            WebElement retryButton = driver.findElement(By.xpath("//span[text()='Retry']/../../.."));
                            System.err.println("Trying to Retry in " + (retryLimit - countRetry) + " minutes...");
                            Thread.sleep(57000);
                            retryButton.click();
                            countRetry++;
                            Thread.sleep(3000);
                        }
                    }
                    catch (Exception e) {
                        countRetry = 0;
                        siteScroller.scrollToBottom();
                    }

                    if (countEmpty > 5) {
                        if (countRefresh > 3) {
                            System.err.println("No more users to scrape...");
                            break;
                        }
                        countRefresh++;
                    }
                    countEmpty++;
                    Thread.sleep(delayMillis);
                }
                else {
                    countEmpty = 0;
                    countRefresh = 0;
                }
            }
            catch (StaleElementReferenceException e) {
                Thread.sleep(delayMillis * 2L);
                continue;
            }
            catch (Exception e) {
                System.err.println("Error scraping users!");
                break;
            }
        }
        return users;
    }

    public User getUserFollowers(User user) throws InterruptedException {
        String userId = user.getUsername();
        siteQuery.goToUser(userId, TwitterQuery.USER_VERIFIED_FOLLOWERS);
        Thread.sleep(3000);
        Set<String> userIds;
        userIds = getUsers(200, 600, -1);
        user.getFollowers().addAll(userIds);
        siteQuery.goToUser(userId, TwitterQuery.USER_FOLLOWERS);
        Thread.sleep(3000);
        userIds = getUsers(200, 600, -1);
        user.getFollowers().addAll(userIds);
        return user;
    }
    public void getUsersFollowers(int limit) throws InterruptedException {
        Map<String, User> users = new HashMap<>();
        Set<String> userIds;

        System.err.println("Preparing to get Followers...");
        userIds = JsonFileManager.fromJson(USERS_SCRAPE_FILE, true, Set.class);
        users = JsonFileManager.fromJsonToMap(USER_FOLLOWERS_SCRAPE_FILE, true);
        int counter = 0, numberOfUsers = userIds.size();
        if (limit == 0) {
            limit = numberOfUsers;
        }

        ProgressPrinter progressPrinter = new ProgressPrinter("get followers", limit);
        for (String userId : userIds) {
            if (users.containsKey(userId) || (users.get(userId) != null && !users.get(userId).getFollowers().isEmpty())) {
                System.err.println("Followers of '" + userId + "' is already scraped!");
                counter++;
                continue;
            }

            User user = new User(userId);
            user = getUserFollowers(user);
            users.put(userId, user);
            counter++;
            JsonFileManager.toJson(USER_FOLLOWERS_SCRAPE_FILE, users, true);
            progressPrinter.printProgress(counter, false);
            if (counter >= limit) {
                break;
            }
        }
//        counter = 0;
//        for (String user : users) {
//            goToFollowers(user, "following");
//            users = getUsers(0, 700, -1);
//            for (String temp : users) {
//                if (!allUsers.containsKey(temp)) {
//                    continue;
//                }
//                User user1 = allUsers.get(temp);
//                user1.getFollowers().add(user);
//            }
//            counter++;
//            if (counter >= limit) {
//                break;
//            }
//        }

        JsonFileManager.toJson(USER_FOLLOWERS_SCRAPE_FILE, userIds, true);
        progressPrinter.printProgress(counter, true);
    }

    @SuppressWarnings({"CallToPrintStackTrace", "unchecked"})
    public void getUserSearch(int maxUsers, boolean hasScraped) throws InterruptedException {
        System.out.println("Get users...");
        Set<String> users = new HashSet<>();
        if (hasScraped) {
            users = (Set<String>) JsonFileManager.fromJson(USERS_SCRAPE_FILE, true, Set.class);
        }
        users = getUsers(maxUsers, 1000, 0);
        JsonFileManager.toJson(USERS_SCRAPE_FILE, users, true);

    }
}
