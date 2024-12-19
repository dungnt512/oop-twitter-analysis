package twitter.navigators;

import twitter.entity.LoginAccount;

public interface SiteQuery {
    void goToHome();
    void goToLink(String link);
    void goToSearch(String query, String tab, boolean isHashTag);
    void goToTweet(String userId, String tweetId, String tab);
    void goToUser(String query, String tab);
    void goToUserSearch(String query, String search);
    void goToUserSearches(String username, String[] searchStrings);
    LoginAccount getUserProfile();
}
