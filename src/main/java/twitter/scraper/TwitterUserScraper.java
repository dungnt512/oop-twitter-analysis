package twitter.scraper;

import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.json.Json;
import twitter.controller.JsonFileManager;
import twitter.entity.ProgressPrinter;
import twitter.entity.Tweet;
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
    private final String USER_RETWEETS_SCRAPE_FILE = DATA_ROOT_DIR + "userRetweets.json";
    private boolean isGetUserSearches = false;

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
        long current = this.progressPrinter.getCurrent();

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
                                //noinspection deprecation
                                var userLink = link.getAttribute("href");
                                if (userLink == null || userLink.isEmpty()) {
                                    continue;
                                }
//                                System.err.println(userLink);
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
                            current++;
                            if (isGetUserSearches) {
                                printProgress(current, false);
                            }
                            siteScroller.scrollToElement(people);
                            int n = users.size();
                            progressPrinter.printProgress(n, false);
                            if (saveFile != null) {
                                JsonFileManager.toJson(saveFile, users, false);
                            }
                            Thread.sleep(200);
                            if (users.size() >= maxUsers) {
                                break;
                            }
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
        progressPrinter.printProgress(users.size(), true);
        return users;
    }

    public void normaliseUser(Map<String, User> users) {
        List<String> deleteIds = new ArrayList<>();
        List<User> addedUsers = new ArrayList<>();
        for (Map.Entry<String, User> entry : users.entrySet()) {
            String userId = entry.getKey();
            if (userId.charAt(0) == '@') {
                deleteIds.add(userId);
                addedUsers.add(entry.getValue());
            }
        }
        for (String id : deleteIds) {
            users.remove(id);
        }
        for (User user : addedUsers) {
            user.setUsername(User.removeAtSign(user.getUsername()));
            List<String> followers = new ArrayList<>();
            for (String id : user.getFollowers()) {
                followers.add(User.removeAtSign(id));
            }
            user.setFollowers(followers);
            List<String> following = new ArrayList<>();
            for (String id : user.getFollowing()) {
                following.add(User.removeAtSign(id));
            }
            user.setFollowing(following);

            users.put(user.getUsername(), user);
        }
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
        Map<String, User> users;
        Set<String> userIds;

        System.err.println("Preparing to get Followers...");
        userIds = JsonFileManager.fromJson(USER_IDS_SCRAPE_FILE, true, Set.class);
        users = JsonFileManager.fromJson(USER_FOLLOWERS_SCRAPE_FILE, true, new TypeToken<Map<String, User>>() {}.getType());
        if (users == null) users = new HashMap<>();
        normaliseUser(users);

        if (userIds == null) userIds = new HashSet<>();
//        List<String> deleteIds = new ArrayList<>();
//        List<User> addedUsers = new ArrayList<>();
//        for (Map.Entry<String, User> entry : users.entrySet()) {
//            String userId = entry.getKey();
//            if (userId.charAt(0) == '@') {
//                deleteIds.add(userId);
//                addedUsers.add(entry.getValue());
//            }
//        }
//        for (String id : deleteIds) {
//            users.remove(id);
//        }
//        for (User user : addedUsers) {
//            user.setUsername(User.removeAtSign(user.getUsername()));
//            List<String> followers = new ArrayList<>();
//            for (String id : user.getFollowers()) {
//                followers.add(User.removeAtSign(id));
//            }
//            user.setFollowers(followers);
//            users.put(user.getUsername(), user);
//        }
        JsonFileManager.toJson(USER_FOLLOWERS_SCRAPE_FILE, users, true);

        int counter = 0, numberOfUsers = userIds.size();
        if (limit == 0) {
            limit = numberOfUsers;
        }

        progressPrinter = new ProgressPrinter("Get followers", limit);
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
            printProgress(counter, false);
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

        JsonFileManager.toJson(USER_FOLLOWERS_SCRAPE_FILE, users, true);
        printProgress(counter, true);
    }

    public User getUserFollowing(User user) throws InterruptedException {
        String userId = user.getUsername();
        siteQuery.goToUser(userId, TwitterQuery.USER_FOLLOWING);
        Thread.sleep(3000);
        Set<String> userIds;
        userIds = getUsers(200, 600, -1, null);
        user.getFollowing().addAll(userIds);
        return user;
    }

    public void getUsersFollowing(int limit) throws InterruptedException {
        Map<String, User> users;
        Set<String> userIds;

        System.err.println("Preparing to get Following...");
        users = JsonFileManager.fromJson(USER_FOLLOWING_SCRAPE_FILE, true, new TypeToken<Map<String, User>>() {}.getType());
        if (users == null) users = new HashMap<>();
        userIds = JsonFileManager.fromJson(USER_IDS_SCRAPE_FILE, true, Set.class);
        if (userIds == null) userIds = new HashSet<>();

//        List<String> deleteIds = new ArrayList<>();
//        List<User> addedUsers = new ArrayList<>();
//        for (Map.Entry<String, User> entry : users.entrySet()) {
//            String userId = entry.getKey();
//            if (userId.charAt(0) == '@') {
//                deleteIds.add(userId);
//                addedUsers.add(entry.getValue());
//            }
//        }
//        for (String id : deleteIds) {
//            users.remove(id);
//        }
//        for (User user : addedUsers) {
//            user.setUsername(User.removeAtSign(user.getUsername()));
//            List<String> following = new ArrayList<>();
//            for (String id : user.getFollowing()) {
//                following.add(User.removeAtSign(id));
//            }
//            user.setFollowing(following);
//            users.put(user.getUsername(), user);
//        }
        normaliseUser(users);
        JsonFileManager.toJson(USER_FOLLOWING_SCRAPE_FILE, users, true);

        int counter = 0, numberOfUsers = userIds.size();
        if (limit == 0) {
            limit = numberOfUsers;
        }

        progressPrinter = new ProgressPrinter("Get following", limit);
        for (String userId : userIds) {
            if (users.containsKey(userId) && (users.get(userId) != null && !users.get(userId).getFollowing().isEmpty())) {
                System.err.println("Following of '" + userId + "' is already scraped!");
                counter++;
                continue;
            }

            User user = new User(userId);
            user = getUserFollowing(user);
            users.put(userId, user);
            counter++;
            JsonFileManager.toJson(USER_FOLLOWING_SCRAPE_FILE, users, true);
            printProgress(counter, false);
            if (counter >= limit) {
                break;
            }
        }

        JsonFileManager.toJson(USER_FOLLOWING_SCRAPE_FILE, users, true);
        printProgress(counter, true);
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
        if (temp == null) temp = new HashSet<>();
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
        progressPrinter = new ProgressPrinter("Get users", maxUsers);
        isGetUserSearches = true;
        JsonFileManager.toJson(USER_IDS_SCRAPE_FILE, new HashSet<>(), true);
        for (String query : queries) {
            Set<String> temp = JsonFileManager.fromJson(USER_IDS_SCRAPE_FILE, false, Set.class);
            if (temp == null) temp = new HashSet<>();
//            Set<String> temp = new HashSet<>();
//            progressPrinter.printProgress(Math.min(maxUsers, temp.size()), false);
//            printProgress(Math.min(maxUsers, temp.size()), false);
//            System.err.println(temp.size() + " " + query);
            if (temp.size() >= maxUsers) {
                break;
            }
            getUserSearch(query, maxUsers);
        }
        isGetUserSearches = true;
//        progressPrinter.printProgress(maxUsers, true);
        printProgress(maxUsers, true);
    }

    public void getUsersRetweets(int limit) throws InterruptedException {
        Map<String, User> users = JsonFileManager.fromJson(USER_RETWEETS_SCRAPE_FILE, true, new TypeToken<Map<String, User>>() {}.getType());
        if (users == null) users = new HashMap<>();
        if (limit == 0) {
            limit = users.size();
        }
        progressPrinter = new ProgressPrinter("Get retweets", limit);
        int counter = 0;
        for (User user : users.values()) {
            if (user.getTweets() == null) user.setTweets(new ArrayList<>());
            for (Tweet tweet : user.getTweets()) {
                siteQuery.goToTweet(user.getUsername(), tweet.getTweetId(), "retweets");
                Thread.sleep(3000);
                Set<String> userIds = getUsers(200, 600, -1, null);
//                tweet.getRetweets().addAll(userIds);
                tweet.setRetweets(userIds.stream().toList());
            }
            JsonFileManager.toJson(USER_RETWEETS_SCRAPE_FILE, users, true);
            counter++;
            printProgress(counter, false);
        }
        printProgress(limit, true);
    }
}
