package org.example;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String username;
    private List<String> followers = new ArrayList<>();
    private List<String> following = new ArrayList<>();

    User(String username) {
        this.username = username;
    }
}
