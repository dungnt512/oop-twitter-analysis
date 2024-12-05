package twitter.navigators;

import org.openqa.selenium.WebDriver;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NitterQuery implements SiteQuery {
    private final String NITTER_HOME_PAGE = "https://nitter.poast.org/";
    private WebDriver driver;

    @Override
    public void goToHome() {
        driver.get(NITTER_HOME_PAGE);
    }

    @Override
    public void goToSearch(String query, String tab, boolean isHashTag) {

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
            url += "/" + tab;
        }
        System.out.println("Get users of '" + query + "' in tab '" + tab + "'...");
        driver.get(url);
    }

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
}
