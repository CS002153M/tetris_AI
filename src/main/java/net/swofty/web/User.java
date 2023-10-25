package net.swofty.web;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.UUID;

@Getter
@Setter
public class User {
    public static ArrayList<User> users = new ArrayList<>();

    public String identifier;
    public String authenticationToken;

    public User(String identifier) {
        this.identifier = identifier;

        this.authenticationToken = generateAuthenticationToken();

            users.add(this);
    }

    public String generateAuthenticationToken() {
        return UUID.randomUUID().toString();
    }

    public static User getUser(String identifier) {
        for (User user : users) {
            if (user.identifier.equals(identifier)) {
                return user;
            }
        }

        return null;
    }

    public static User getFromAuthenticationToken(String token) {
        for (User user : users) {
            if (user.authenticationToken.equals(token)) {
                return user;
            }
        }

        return null;
    }

}
