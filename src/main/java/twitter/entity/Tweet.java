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


public class Tweet extends TweetObject {
    public static class SortTweets implements Comparator<Tweet> {
        public int compare(Tweet a, Tweet b) {
            long A = a.getNumberOfHearts() + a.getNumberOfComments() * 3L + a.getNumberOfQuotes() * 2L + a.getNumberOfRetweets() * 3L;
            long B = b.getNumberOfHearts() + b.getNumberOfComments() * 3L + b.getNumberOfQuotes() * 2L + b.getNumberOfRetweets() * 3L;
            return Long.compare(B, A);
        }
    }
    public static String removeSharpEnd(String id) {
        String[] split = id.split("#");
        return split[0];
    }
    public static String getTweetId(String tweetLink) {
        String[] split = tweetLink.split("/");
        int j = split.length - 1;
        while (split[j].charAt(0) < '0' || split[j].charAt(0) > '9') {
            j--;
        }
        return removeSharpEnd(split[j]);
    }

    private String tweetId;
    private String tweetLink;
    private int numberOfComments = 0, numberOfRetweets = 0, numberOfQuotes = 0, numberOfHearts = 0;
    private List<String> comments = new ArrayList<>();
    private List<String> retweets = new ArrayList<>();

    public Tweet(String tweetLink) {
        this.typeName = "tweet";
        this.tweetLink = tweetLink;
        String[] split = tweetLink.split("/");
        this.tweetId = split[split.length - 1];
    }

    public String getTweetId() {
        return tweetId;
    }

    public void setTweetId(String tweetId) {
        this.tweetId = tweetId;
    }

    public String getTweetLink() {
        return tweetLink;
    }

    public void setTweetLink(String tweetLink) {
        this.tweetLink = tweetLink;
    }

    public int getNumberOfComments() {
        return numberOfComments;
    }

    public void setNumberOfComments(int numberOfComments) {
        this.numberOfComments = numberOfComments;
    }

    public int getNumberOfRetweets() {
        return numberOfRetweets;
    }

    public void setNumberOfRetweets(int numberOfRetweets) {
        this.numberOfRetweets = numberOfRetweets;
    }

    public int getNumberOfQuotes() {
        return numberOfQuotes;
    }

    public void setNumberOfQuotes(int numberOfQuotes) {
        this.numberOfQuotes = numberOfQuotes;
    }

    public int getNumberOfHearts() {
        return numberOfHearts;
    }

    public void setNumberOfHearts(int numberOfHearts) {
        this.numberOfHearts = numberOfHearts;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public List<String> getRetweets() {
        return retweets;
    }

    public void setRetweets(List<String> retweets) {
        this.retweets = retweets;
    }
}
