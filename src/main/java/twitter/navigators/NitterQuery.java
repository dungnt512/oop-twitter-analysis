package twitter.navigators;

import org.openqa.selenium.WebDriver;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import twitter.entity.LoginAccount;

import java.util.Arrays;

public class NitterQuery implements SiteQuery {
    private final String NITTER_HOME_PAGE = "https://nitter.poast.org/";
    private WebDriver driver;

    @Override
    public void goToHome() {
        driver.get(NITTER_HOME_PAGE);
    }

    @Override
    public void goToSearch(String query, String tab, boolean isHashTag) {}

    @Override
    public void goToLink(String link) {
        driver.get(link);
    }

    @Override
    public void goToUser(String query, String tab) {
        if (query == null || query.isEmpty()) {
            System.err.println("Query is not set!");
            return ;
        }
        if (query.charAt(0) == '@') {
            query = query.substring(1);
        }
        String url = NITTER_HOME_PAGE + query + '/';
        if (!tab.isEmpty()) {
            url += tab;
        }
        System.out.println("Get users of '" + query + "' in tab '" + tab + "'...");
        driver.get(url);
    }

    @Override
    public void goToUserSearch(String query, String search) {
        if (query == null || query.isEmpty()) {
            System.err.println("Query is not set!");
            return ;
        }
        if (query.charAt(0) == '@') {
            query = query.substring(1);
        }
        String url = NITTER_HOME_PAGE + query + '/';
        if (!search.isEmpty()) {
            url += "search?f=tweets&q=" + search + "&since=&until=&near=";
        }
        System.out.println("Get users of '" + query + "' in search results of '" + search + "'...");
        driver.get(url);
    }

    @Override
    public void goToUserSearches(String query, String[] searches) {
        if (query == null || query.isEmpty() || searches == null || searches.length == 0) {
            System.err.println("Query is not set!");
            return ;
        }
        if (query.charAt(0) == '@') {
            query = query.substring(1);
        }
        StringBuilder url = new StringBuilder(NITTER_HOME_PAGE + query + '/');
        int m = searches.length;
        url.append("search?f=tweets&q=%28").append(searches[0]);
        for (int i = 1; i < m; i++) {
            url.append("%2C+OR+").append(searches[i]);
        }
        url.append("%29&since=&until=&near=");
        System.out.println("Get users of '" + query + "' in search results of '" + Arrays.toString(searches) + "'...");
        driver.get(url.toString());
    }

    @Override
    public void goToTweet(String userId, String tweetId, String tab) {

    }

    public LoginAccount getUserProfile() {
        return null;
    }

    public NitterQuery(WebDriver driver) {
        this.driver = driver;
    }
}
