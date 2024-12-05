package twitter.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Tweet extends TweetObject {
    public static class SortTweets implements Comparator<Tweet> {
        public int compare(Tweet a, Tweet b) {
            long A = a.getNumberOfHearts() + a.getNumberOfComments() * 3L + a.getNumberOfQuotes() * 2L + a.getNumberOfRetweets() * 3L;
            long B = b.getNumberOfHearts() + b.getNumberOfComments() * 3L + b.getNumberOfQuotes() * 2L + b.getNumberOfRetweets() * 3L;
            return Long.compare(B, A);
        }
    }

    private String tweetId;
    private String tweetLink;
    private int numberOfComments = 0, numberOfRetweets = 0, numberOfQuotes = 0, numberOfHearts = 0;
    private List<String> comments;
    private List<String> retweets;

    public Tweet(String tweetLink) {
        this.typeName = "tweet";
        this.tweetLink = tweetLink;
        String[] split = tweetLink.split("/");
        this.tweetId = split[split.length - 1];
    }
}
