package twitter.algorithms;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import twitter.controller.JsonFileManager;
import twitter.entity.ProgressPrinter;
import twitter.entity.User;
import twitter.navigators.SiteQuery;
import twitter.navigators.SiteScroller;
import twitter.navigators.TwitterQuery;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
public class TwitterUserScraper extends Scraper {
    private final String USER_IDS_SCRAPE_FILE = DATA_ROOT_DIR + "userIds.json";
    private final String USER_IDS_SCRAPE_BACKUP_FILE = DATA_ROOT_DIR + "userIds_tmp.json";
    private final String USERS_SCRAPE_FILE = DATA_ROOT_DIR + "users.json";
    private final String USER_FOLLOWERS_SCRAPE_FILE = DATA_ROOT_DIR + "userFollowers.json";
    private final String USER_FOLLOWING_SCRAPE_FILE = DATA_ROOT_DIR + "userFollowing.json";

    TwitterUserScraper(WebDriver driver, SiteScroller siteScroller, SiteQuery siteQuery) {
        super(driver, siteScroller, siteQuery);
    }


    @SuppressWarnings({"BusyWait"})
    private Set<String> getUsers(int maxUsers, int delayMillis, int retryLimit, String saveFile) throws InterruptedException {
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
                List<WebElement> local = driver.findElements(By.xpath("//button[@data-testid='UserCell']"));
                int m = local.size(), added = 0;
//                System.err.print(m + " ");
//                if (m > 1 && local.get(0) != local.get(1)) System.err.print("? ");
                for (int i = Math.max(0, m - 40); i < m; i++) {
                    try {
                        WebElement people = local.get(i);
                        String user = "";
                        try {
                            List<WebElement> links = people.findElements(By.xpath(".//a[@role='link']"));
//                            System.err.print(links.size() + " ");
                            for (WebElement link : links) {
                                String userLink = link.getAttribute("href");
                                if (userLink == null || userLink.isEmpty()) {
                                    continue;
                                }
                                System.err.println(userLink);
                                String[] splits = userLink.split("/");
                                user = splits[splits.length - 1];
                                break;
                            }

                        } catch (Exception e) {
                            //noinspection CallToPrintStackTrace
                            e.printStackTrace();
                        }
                        if (!user.isEmpty() && !users.contains(user)) {
                            added++;
                            users.add(user);
                            siteScroller.scrollToElement(people);
                            int n = users.size();
                            progressPrinter.printProgress(n, false);
                            if (saveFile != null) {
                                JsonFileManager.toJson(saveFile, users, false);
                            }
                            Thread.sleep(200);
                        }
                    }
                    catch (Exception e) {
                        Thread.sleep(300);
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
//                        siteScroller.scrollToBottom();
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
//        progressPrinter.printProgress(users.size(), true);
        return users;
    }

    public User getUserFollowers(User user) throws InterruptedException {
        String userId = user.getUsername();
        siteQuery.goToUser(userId, TwitterQuery.USER_VERIFIED_FOLLOWERS);
        Thread.sleep(3000);
        Set<String> userIds;
        userIds = getUsers(200, 600, -1, null);
        user.getFollowers().addAll(userIds);
        siteQuery.goToUser(userId, TwitterQuery.USER_FOLLOWERS);
        Thread.sleep(3000);
        userIds = getUsers(200, 600, -1, null);
        user.getFollowers().addAll(userIds);
        return user;
    }
    public void getUsersFollowers(int limit) throws InterruptedException {
        Map<String, User> users = new HashMap<>();
        Set<String> userIds;

        System.err.println("Preparing to get Followers...");
        userIds = JsonFileManager.fromJson(USER_IDS_SCRAPE_FILE, true, Set.class);
        users = JsonFileManager.fromJsonToMap(USER_FOLLOWERS_SCRAPE_FILE, true);
        int counter = 0, numberOfUsers = userIds.size();
        if (limit == 0) {
            limit = numberOfUsers;
        }

        ProgressPrinter progressPrinter = new ProgressPrinter("get followers", limit);
        for (String userId : userIds) {
            if (users.containsKey(userId) && (users.get(userId) != null && !users.get(userId).getFollowers().isEmpty())) {
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

    public void getUserSearch(String query, int maxUsers) throws InterruptedException {
        Set<String> users;
        if (query == null || query.isEmpty()) {
            System.err.println("Query is not set!");
            return ;
        }
        if (query.charAt(0) == '#') {
            query = query.substring(1);
            siteQuery.goToSearch(query, TwitterQuery.SEARCH_PEOPLE, true);
        }
        else {
            siteQuery.goToSearch(query, TwitterQuery.SEARCH_PEOPLE, false);
        }
        System.out.println("Get users...");
        Set<String> temp = JsonFileManager.fromJson(USER_IDS_SCRAPE_FILE, false, Set.class);
//        users = JsonFileManager.fromJson(USER_IDS_SCRAPE_FILE, true, Set.class);
        JsonFileManager.toJson(USER_IDS_SCRAPE_BACKUP_FILE, temp, false);
        users = getUsers(maxUsers, 1000, 0, USER_IDS_SCRAPE_FILE);
        for (String user : temp) {
            if (user.charAt(0) == '@') {
                user = user.substring(1);
            }
            users.add(user);
        }
        JsonFileManager.toJson(USER_IDS_SCRAPE_FILE, users, true);
    }

    public void getUserSearches(int maxUsers, String... queries) throws InterruptedException {
        int m = queries.length;
        ProgressPrinter progressPrinter = new ProgressPrinter("Get users", maxUsers);
        for (String query : queries) {
            Set<String> temp = JsonFileManager.fromJson(USER_IDS_SCRAPE_FILE, false, Set.class);
            progressPrinter.printProgress(Math.min(maxUsers, temp.size()), false);
            if (temp.size() >= maxUsers) {
                break;
            }
            getUserSearch(query, maxUsers);
        }
        progressPrinter.printProgress(maxUsers, true);
    }
}
