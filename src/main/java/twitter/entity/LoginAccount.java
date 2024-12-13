package twitter.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginAccount {
    private String mail;
    private String username;
    private String password;
    public LoginAccount(String mail, String username, String password) {
        this.mail = mail;
        this.username = username;
        this.password = password;
    }
    private String name;
}
