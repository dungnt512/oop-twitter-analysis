package twitter.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    public LoginAccount() {}

    public String getMail() {
        return mail;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
