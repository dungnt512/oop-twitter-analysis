package twitter.navigators;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.openqa.selenium.WebElement;
import java.util.*;
import twitter.entity.LoginAccount;

@Getter
@Setter
@AllArgsConstructor
public class TwitterQuery implements SiteQuery {
    private final String TWITTER_HOME_PAGE = "https://x.com/";
    private WebDriver driver;

    @Override
    public void goToHome() {
        driver.get(TWITTER_HOME_PAGE);
    }
    @Override
    public void goToLink(String link) {
        driver.get(TWITTER_HOME_PAGE + link);
    }
    public static final String SEARCH_LATEST    = "live";
    public static final String SEARCH_PEOPLE    = "user";
    public static final String SEARCH_MEDIA     = "media";
    public static final String SEARCH_LISTS     = "list";

    @Override
    public void goToSearch(String query, String tab, boolean isHashTag) {
        if (query == null || query.isEmpty()) {
            System.err.println("Query is not set!");
            return ;
        }
        System.out.println("Query '" + (isHashTag ? "#" : "") + query + "' in tab '" + tab + "'...");
        String url = !isHashTag ?
                TWITTER_HOME_PAGE + "search?q=" + query + "&src=typed_query" :
                TWITTER_HOME_PAGE + "hashtag/" + query  ;
        if (!tab.isEmpty()) {
            url += (isHashTag ? "?f=" : "&f=") + tab;
        }
        driver.get(url);
    }

    public static final String USER_VERIFIED_FOLLOWERS  = "verified_followers";
    public static final String USER_FOLLOWERS           = "followers";
    public static final String USER_FOLLOWING           = "following";
    public static final String USER_REPLIES             = "with_replies";
    public static final String USER_AFFILIATES          = "affiliates";

    @Override
    public void goToUser(String query, String tab) {
        if (query == null || query.isEmpty()) {
            System.err.println("Query is not set!");
            return ;
        }
        System.out.println("Get users of '" + query + "' in tab '" + tab + "'...");
        String url = TWITTER_HOME_PAGE + query + '/' + tab;
        driver.get(url);
    }

    @Override
    public void goToUserSearch(String query, String search) {}
    @Override
    public void goToUserSearches(String query, String[] searches) {}

    public LoginAccount getUserProfile() {
        WebElement accountSwitcher = driver.findElement(By.xpath("//button[@data-testid='SideNav_AccountSwitcher_Button']"));
        List<WebElement> profile = accountSwitcher.findElements(By.xpath(".//span[@class='css-1jxf684 r-bcqeeo r-1ttztb7 r-qvutc0 r-poiln3']"));
        LoginAccount account = new LoginAccount();
        account.setName(profile.getFirst().getText());
        account.setUsername(profile.get(1).getText());
        return account;
    }
}
