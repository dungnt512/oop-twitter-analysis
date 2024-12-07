package twitter.navigators;

import lombok.Getter;
import lombok.Setter;
import org.openqa.selenium.*;
import twitter.controller.JsonFileManager;
import twitter.entity.LoginAccount;

import java.util.NoSuchElementException;

@Getter
@Setter
public class TwitterLogin implements SiteLogin {
    private final String DATA_ROOT_DIR = "data/";
    private final String TWITTER_LOGIN_URL = "https://x.com/i/flow/login";
    private final String USER_ACCOUNT_FILE = DATA_ROOT_DIR + "userAccount.json";
    private WebDriver driver;
    private LoginAccount loginAccount;


    public TwitterLogin(WebDriver driver) {
        this.driver = driver;
        this.loginAccount = (LoginAccount) JsonFileManager.fromJson(USER_ACCOUNT_FILE, false, LoginAccount.class);
    }
    public TwitterLogin(WebDriver driver, LoginAccount loginAccount) {
        this.driver = driver;
        this.loginAccount = loginAccount;
    }

    public void goToLoginPage() {
        driver.get(TWITTER_LOGIN_URL);
    }
    public boolean login() {
        System.out.println("Logging In...");
        goToLoginPage();
        try {
            System.err.println("Waiting for login page...");
            Thread.sleep(4000);

            inputUsername();
            Thread.sleep(8000);
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
            return false;
        }
        return true;
    }

    private void inputUsername() throws InterruptedException {
        int numberOfAttempt = 0;
        while (true) {
            try {
                WebElement username = driver.findElement(By.xpath("//input[@autocomplete='username']"));
                System.err.println("Typing username...");
                username.sendKeys(this.loginAccount.getUsername());
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
                unusualActivity.sendKeys(this.loginAccount.getMail());
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
                password.sendKeys(this.loginAccount.getPassword());
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
}
