package twitter.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User extends TweetObject {
    private String username;
    private String userLink;
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
