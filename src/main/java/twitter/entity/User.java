package twitter.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
}
