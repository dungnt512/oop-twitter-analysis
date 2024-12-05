package twitter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import twitter.algorithms.*;
import twitter.algorithms.NitterScraper;
import twitter.controller.DriverManager;

@Getter
@Setter
@ToString
public class Main {
    public static void main(String[] args) throws InterruptedException {
//        List<String> proxyList = new ArrayList<>();
//        proxyList.add("");
////        proxyList.add("148.72.165.185:10501");
////        proxyList.add("35.161.172.205:1080");
////        proxyList.add("52.35.240.119:1080");
////        proxyList.add("148.72.165.184:10501");
//
//        Gson gson = new Gson();
//        final String userAccountFile = "data/userAccount.json";
//        LoginAccount loginAccount;
//        try {
//            JsonReader jsonReader = new JsonReader(new FileReader(userAccountFile));
//            loginAccount = gson.fromJson(jsonReader, LoginAccount.class);
//        }
//        catch (Exception e) {
//            System.err.println("Cannot access to '" + userAccountFile + "'");
//            return ;
//        }
//
//        for (String proxy : proxyList) {
//            TwitterScraper scraper = new TwitterScraper(loginAccount, proxy, false);
//            if (!scraper.getSiteLogin().login()) {
//                continue;
//            }
//
////            Thread.sleep(3000);
////            scraper.getSiteQuery().goToSearch("blockchain", TwitterQuery.SEARCH_PEOPLE, false);
////            Thread.sleep(3000);
////            scraper.getUserSearch(5000, true);
//
//            Thread.sleep(3000);
//            scraper.getFollowers("all", 0);
//            final String crawlUserFollowersFile = "userFollowers.json";
////
//            Gson gson = new Gson();
//            try {
//                Type mapType = new TypeToken<Map<String, User>>() {
//                }.getType();
//                JsonReader reader = new JsonReader(new FileReader(crawlUserFollowersFile));
//                Map<String, User> allUsers = gson.fromJson(reader, mapType);
//                System.out.println(allUsers.size());
//            } catch (Exception e) {
//                System.err.println("Import '" + crawlUserFollowersFile + "' error!");
//            }
//        }

//        TwitterScraper twitterScraper = new TwitterScraper("", false);
//        twitterScraper.login();
        NitterScraper scraper = new NitterScraper("", false);
        scraper.getNitterTweetScraper().getTweetsOfUsers(0, 50, "blockchain");
//        scraper.quitDriver();
        scraper.getDriver().quit();
    }
}