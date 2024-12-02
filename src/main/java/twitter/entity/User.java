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
public class User {
    private String username;
    private List<String> followers = new ArrayList<>();
    private List<String> following = new ArrayList<>();

    public User(String username) {
        this.username = username;
    }
}
