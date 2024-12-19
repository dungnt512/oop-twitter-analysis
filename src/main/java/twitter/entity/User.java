package twitter.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class User extends TweetObject {
    public static class SortUsers implements Comparator<User> {
        public int compare(User a, User b) {
            long A = a.getLikesCount() * 3L + a.getFollowingCount() + a.getTweetsCount() * 2L + a.getFollowersCount() * 3L;
            long B = b.getLikesCount() * 3L + b.getFollowingCount() + b.getTweetsCount() * 2L + b.getFollowersCount() * 3L;
            return Long.compare(B, A);
        }
    }

    private String username;
    private String userLink;
    private String location;
    private String joinDate;
    private int tweetsCount = 0;
    private int followersCount = 0;
    private int followingCount = 0;
    private int likesCount = 0;
    private List<String> followers = new ArrayList<>();
    private List<String> following = new ArrayList<>();
    private List<Tweet> tweets = new ArrayList<>();

    public User(String username) {
        this.typeName = "user";
        removeAtSign(username);
        this.username = username;
    }
    public User(String userLink, boolean isLink) {
        this.typeName = "user";
        if (!isLink) {
            this.username = userLink;
        }
        else {
            this.userLink = userLink;
            String[] splits = userLink.split("/");
            this.username = splits[splits.length - 1];
        }
    }

    public static String removeAtSign(String userId) {
        if (userId.charAt(0) == '@') {
            userId = userId.substring(1);
        }
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserLink() {
        return userLink;
    }

    public void setUserLink(String userLink) {
        this.userLink = userLink;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(String joinDate) {
        this.joinDate = joinDate;
    }

    public int getTweetsCount() {
        return tweetsCount;
    }

    public void setTweetsCount(int tweetsCount) {
        this.tweetsCount = tweetsCount;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }

    public List<String> getFollowing() {
        return following;
    }

    public void setFollowing(List<String> following) {
        this.following = following;
    }

    public List<Tweet> getTweets() {
        return tweets;
    }

    public void setTweets(List<Tweet> tweets) {
        this.tweets = tweets;
    }
}
