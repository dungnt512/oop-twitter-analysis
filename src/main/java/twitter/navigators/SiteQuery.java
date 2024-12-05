package twitter.navigators;

public interface SiteQuery {
    void goToHome();
    void goToSearch(String query, String tab, boolean isHashTag);
    void goToUser(String query, String tab);
    void goToUserSearch(String query, String search);
}
