package twitter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import twitter.application.App;

@Getter
@Setter
@ToString
public class Main {
    public static void main(String[] args) throws InterruptedException {
        App.main(args);
//        WebDriver driver = null;
//        while (true) {
//            try {
//                List<String> proxyList = new ArrayList<>();
//                proxyList.add("");
////                proxyList.add("148.72.165.185:10501");
////                proxyList.add("35.161.172.205:1080");
////                proxyList.add("52.35.240.119:1080");
////                proxyList.add("148.72.165.184:10501");
//
//                for (String proxy : proxyList) {
//                    TwitterScraper scraper = new TwitterScraper(proxy, false, true);
//                    driver = scraper.getDriver();
//                    //noinspection BusyWait
//                    Thread.sleep(3000);
////                    scraper.getTwitterUserScraper().getUserSearches(5000, "hyperledger fabric", "ethereum", "corda", "quorum", "#blockchain", "crypto", "#crypto");
//                    scraper.getTwitterUserScraper().getUsersFollowers(0);
////                    scraper.quitDriver();
////                    TwitterScraper twitterScraper = new TwitterScraper("", false);
////                    twitterScraper.login();
////                    NitterScraper scraper = new NitterScraper(proxy, false);
////                    scraper.getNitterUserScraper().getInfoOfUsers(0);
////                    scraper.getNitterTweetScraper().getTweetsOfUsers(0, 50, "blockchain", "ethereum", "crypto", "hyperledger fabric");
////                    scraper.getNitterTweetScraper().getTweetsOfUsers(0, 50);
//                    scraper.quitDriver();
//                }
//                break;
//            } catch (Exception e) {
//                assert driver != null;
//                System.err.println(e.getMessage() + ". Try again!");
//                driver.quit();
//            }
//        }
    }
}