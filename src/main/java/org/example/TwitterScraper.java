package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.NoSuchElementException;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TwitterScraper {
    private static final String TWITTER_LOGIN_URL = "https://twitter.com/i/flow/login";

    private WebDriver driver;
    private String mail, username, password;
    private List<Map<String, String>> scrapedTweets = new ArrayList<>();
    private Scroller scroller;
    private Actions actions;

    TwitterScraper(String mail, String username, String password, String proxy, boolean headless) throws InterruptedException {
        this.mail = mail;
        this.username = username;
        this.password = password;
        driver = initializeDriver(proxy, headless);
        scroller = new Scroller(driver);
        actions = new Actions(driver);
    }

    private WebDriver initializeDriver(String proxyAddress, boolean headless) {
        System.out.println("Initialize WebDriver...");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-dev-shm-usage", "--ignore-certificate-error",
                "--disable-gpu", "--log-level-3", "--disable-notifications", "--disable-popup-blocking",
                "--no-sandbox");

        if (!proxyAddress.isEmpty()) {
            Proxy proxy = new Proxy();
            proxy.setHttpProxy(proxyAddress);
            proxy.setSslProxy(proxyAddress);
            options.setProxy(proxy);
        }
        if (headless) {
            options.addArguments("--headless");
        }
        try {
            System.out.println("Initialize ChromeDriver...");
            driver = new ChromeDriver(options);
            System.out.println("Setup Complete");
            return driver;
        }
        catch (WebDriverException e) {
            System.out.println("Initialize ChromeDriver failed");
        }
        return driver;
    }

    public void login() throws InterruptedException {
        System.out.println("Logging In...");
        try {
            driver.manage().window().maximize();
            driver.get(TWITTER_LOGIN_URL);
            System.err.println("Waiting for login page...");
            Thread.sleep(4000);

            inputUsername();
            inputUnusualActivity();
            System.err.println("Preparing password... ");
            inputPassword();

//            Set<Cookie> cookies = driver.manage().getCookies();
//            String authToken = null;
//            for (Cookie cookie : cookies) {
//                if (cookie.getName().equals("auth_token")) {
//                    authToken = cookie.getValue();
//                    break;
//                }
//            }
//            if (authToken == null) {
//                System.out.println("Authentication error!");
//            }

            System.out.println("\nLogin Successful!\n");
        }
        catch (Exception e) {
            System.out.println("\nLogin failed!\n");
        }
    }

    private void inputUsername() throws InterruptedException {
        int numberOfAttempt = 0;
        while (true) {
            try {
                WebElement username = driver.findElement(By.xpath("//input[@autocomplete='username']"));
                System.err.println("Typing username...");
                username.sendKeys(this.username);
                username.sendKeys(Keys.RETURN);
                //noinspection BusyWait
                Thread.sleep(3000);
                break;
            }
            catch (Exception e) {
                numberOfAttempt++;
                if (numberOfAttempt > 2) {
                    System.out.println("Input username ERROR!");
                    driver.quit();
                    System.exit(1);
                }
                else {
                    System.out.println("Re-attempting to input username...");
                    //noinspection BusyWait
                    Thread.sleep(2000);
                }
            }
        }
    }

    private void inputUnusualActivity() throws InterruptedException {
        Thread.sleep(1000);
        int numberOfAttempt = 0;
        WebElement unusualActivity;
        while (true) {
            try {
                unusualActivity = driver.findElement(By.xpath("//input[@data-testid='ocfEnterTextTextInput']"));
                System.err.println("Typing email...");
                unusualActivity.sendKeys(this.mail);
                unusualActivity.sendKeys(Keys.RETURN);
                //noinspection BusyWait
                Thread.sleep(2000);
                break;
            }
            catch (Exception e) {
//                System.err.println("No unusual activity found!...");
                numberOfAttempt++;
                if (numberOfAttempt > 1) {
                    break;
                }

            }
        }

    }

    private void inputPassword() throws InterruptedException {
        int numberOfAttempt = 0;
        while (true) {
            try {
                WebElement password = driver.findElement(By.xpath("//input[@autocomplete='current-password']"));
                System.err.println("Typing password...");
                password.sendKeys(this.password);
                password.sendKeys(Keys.RETURN);
                //noinspection BusyWait
                Thread.sleep(3000);
                break;
            }
            catch (NoSuchElementException e) {
                numberOfAttempt++;
                if (numberOfAttempt > 2) {
                    System.out.println("Input password ERROR!");
                    driver.quit();
                    System.exit(1);
                }
                else {
                    System.out.println("Re-attempting to input password...");
                    //noinspection BusyWait
                    Thread.sleep(2000);
                }
            }
        }
    }

    private void goToHome() throws InterruptedException {
        driver.get("https://x.com/home");
        Thread.sleep(3000);
    }

    public void goToSearch(String query, String tab) throws InterruptedException {
        if (query == null || query.isEmpty()) {
            System.err.println("Query is not set!");
            return ;
        }
        System.out.println("Query '" + query + "' in tab '" + tab + "'...");
        String url = "https://x.com/search?q=" + query + "&src=typed_query";
        if (tab.equals("people")) {
            url += "&f=user";
        }
        driver.get(url);
    }

    public void goToFollowers(String query, String tab) throws InterruptedException {
        if (query == null || query.isEmpty()) {
            System.err.println("Query is not set!");
            return ;
        }
        System.out.println("Get followers of '" + query + "' in tab '" + tab + "'...");
        String url = "https://x.com/" + query + '/' + tab;
        driver.get(url);
    }

    private Set<String> getUsers(int maxUsers, int delayMillis) throws InterruptedException {
        Set<String> users = new HashSet<>();

        int countRetry = 0, countEmpty = 0, countRefresh = 0;
//        for (int iter = 0; iter < numberOfScroll; iter++)
        if (maxUsers == 0) {
            maxUsers = 3000;
        }

        Progress progress = new Progress("get username", maxUsers);
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
                            scroller.scrollToElement(people);
                            int n = users.size();
                            progress.update(n);
                            Thread.sleep(150);
                        }
                    }
                    catch (Exception e) {
                        continue;
                    }
                }

                if (added == 0) {
                    try {
                        while (countRetry < 20) {
                            WebElement retryButton = driver.findElement(By.xpath("//span[text()='Retry']/../../.."));
                            System.err.println("Trying to Retry in " + (20 - countRetry) + " minutes...");
                            Thread.sleep(57000);
                            retryButton.click();
                            countRetry++;
                            Thread.sleep(3000);
                        }
                    }
                    catch (Exception e) {
                        countRetry = 0;
                        scroller.scrollToBottom();
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

    public void getFollowers(String query, int limit) throws InterruptedException {
        Map<String, User> allUsers = new HashMap<>();
        Set<String> users;
        if (query == null || query.isEmpty()) {
            System.err.println("Query is not set!");
            return ;
        }

        Gson gson = new Gson();
        System.err.println("Preparing to get Followers...");

        final String crawlUsersFile = "users.json";
        if (query.equals("all")) {
            try {
                System.out.println("Finding 'users.json'...");
                JsonReader reader = new JsonReader(new FileReader(crawlUsersFile));
                users = gson.fromJson(reader, Set.class);
            }
            catch (Exception e) {
                System.out.println("'users.json' not found.");
                return ;
            }
        }
        else {
            users = new HashSet<>();
            users.add(query);
        }


        final String crawlUserFollowersFile = "userFollowers.json";

        try {
            Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            JsonReader reader = new JsonReader(new FileReader(crawlUserFollowersFile));
            allUsers = gson.fromJson(reader, mapType);
        }
        catch (Exception e) {
            System.err.println("Import '" + crawlUserFollowersFile + "' error!");
        }
        int counter = 0, numberOfUsers = users.size();
        if (limit == 0) {
            limit = numberOfUsers;
        }

        Progress progress = new Progress("get followers", limit);
        for (String user : users) {
            if (allUsers.containsKey(user)) {
                System.err.println("Followers of " + user + " already crawled!");
                counter++;
                continue;
            }
            User user1 = new User(user);
            goToFollowers(user, "verified_followers");
            Thread.sleep(3000);
            users = getUsers(200, 600);
            user1.getFollowers().addAll(users);
            goToFollowers(user, "followers");
            Thread.sleep(3000);
            users = getUsers(200, 600);
            user1.getFollowers().addAll(users);
            allUsers.put(user, user1);

            counter++;
            try {
                PrintWriter out = new PrintWriter(crawlUserFollowersFile);
                out.print(gson.toJson(allUsers));
                out.close();
            }
            catch (Exception e) {
                System.err.println("Cannot access '" + crawlUserFollowersFile + "'!");
                continue;
            }
            progress.printProgress(counter);
//            if (progress.printProgress(counter)) {
//                out.println(gson.toJson(allUsers));
//            }
            if (counter >= limit) {
                break;
            }
        }
        counter = 0;
        for (String user : users) {
            goToFollowers(user, "following");
            users = getUsers(0, 700);
            for (String temp : users) {
                if (!allUsers.containsKey(temp)) {
                    continue;
                }
                User user1 = allUsers.get(temp);
                user1.getFollowers().add(user);
            }
            counter++;
            if (counter >= limit) {
                break;
            }
        }

        try {
            PrintWriter out = new PrintWriter(crawlUserFollowersFile);
            out.print(gson.toJson(allUsers));
            out.close();
        }
        catch (Exception e) {
            System.err.println("Creating '" + crawlUserFollowersFile + "' error!");
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }

        System.out.println();
    }

    @SuppressWarnings({"CallToPrintStackTrace"})
    public void getUserSearch(int maxUsers, boolean hasCrawl) throws InterruptedException {
        System.out.println("Get users...");
        Set<String> users = new HashSet<>();
        Gson gson = new Gson();
        final String crawlUsersFile = "users.json";
        if (hasCrawl) {
            try {
                System.out.println("Finding 'users.json'...");
                JsonReader reader = new JsonReader(new FileReader(crawlUsersFile));
                users = gson.fromJson(reader, Set.class);
                System.out.println("'users.json' found. Done!!!\n");
                return ;
            }
            catch (FileNotFoundException e) {
                System.out.println("'users.json' not found. Prepare to crawling users...");
                hasCrawl = false;
            }
        }
        System.out.println();

        users = getUsers(maxUsers, 1000);

        try {
            PrintWriter out = new PrintWriter(crawlUsersFile);
            gson.toJson(users);
//            System.out.println(gson.toJson(users));
            System.out.println(users.size());
            out.print(gson.toJson(users));
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println(users.size());
//        for (String user: users) {
//            System.out.println(user);
//        }
    }
}
