package org.example;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import lombok.Getter;
import lombok.Setter;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Main {
    public static void main(String[] args) throws InterruptedException {
        List<String> proxyList = new ArrayList<>();
        proxyList.add("");
//        proxyList.add("148.72.165.185:10501");
//        proxyList.add("35.161.172.205:1080");
//        proxyList.add("52.35.240.119:1080");
//
//        proxyList.add("148.72.165.184:10501");

        Gson gson = new Gson();
        final String userAccountFile = "userAccount.json";
        Account account;
        try {
            JsonReader jsonReader = new JsonReader(new FileReader(userAccountFile));
            account = gson.fromJson(jsonReader, Account.class);
        }
        catch (Exception e) {
            System.err.println("Cannot access to '" + userAccountFile + "'");
            return ;
        }

        for (String proxy : proxyList) {
            TwitterScraper scraper = new TwitterScraper(account.getMail(),
                    account.getUsername(), account.getPassword(), proxy, true);
            if (!scraper.login()) {
                continue;
            }

//            Thread.sleep(3000);
//            scraper.goToSearch("blockchain", "people");
//            Thread.sleep(3000);
//            scraper.getUserSearch(5000, true);

            Thread.sleep(3000);
            scraper.getFollowers("all", 0);
            final String crawlUserFollowersFile = "userFollowers.json";
//
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
        }

    }
}