package net.swofty.web;
import lombok.SneakyThrows;
import net.swofty.database.AuthenticationDatabase;
import net.swofty.tetris.Field;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class AuthenticationEndpoints {

    public static HashMap<User, Field> users = new HashMap<>();

    public static void handle() {
        get("/receive-auth", (request, response) -> {
            String identifier = request.headers("identifier");
            String password = request.headers("password");

            AuthenticationDatabase database = new AuthenticationDatabase(identifier);
            if (!database.exists()) {
                return new JSONObject().append("status", "error");
            }

            String salt = database.getString("salt","");
            String hash = database.getString("hash", "");

            String hashedPassword = hashWithPreExistingSalt(salt, password);
            if (!hash.equals(hashedPassword)) {
                return new JSONObject().append("status", "error");
            }

            User user = User.getUser(identifier);
            user.setAuthenticationToken(user.generateAuthenticationToken());
            users.put(user, new Field());

            return new JSONObject().append("status", user.getAuthenticationToken());
        });

        get("/register", (request, response) -> {
            String identifier = request.headers("identifier");
            String password = request.headers("password");

            AuthenticationDatabase database = new AuthenticationDatabase(identifier);
            if (database.exists()) {
                return new JSONObject().append("status", "error");
            }

            Map.Entry<String, String> hashAndSalt = hashWithRandomSalt(password);
            String salt = hashAndSalt.getKey();
            String hash = hashAndSalt.getValue();

            database.set("id", identifier);
            database.set("hash", hash);
            database.set("salt", salt);

            User.users.add(new User(identifier));

            return new JSONObject().append("status", "success");
        });

        before("/protected/*", (request, response) -> {
            if (User.getFromAuthenticationToken(request.headers("token")) == null) {
                halt(401, "Go Away!");
            }
        });
    }

    @SneakyThrows
    public static String hashWithPreExistingSalt(String salt, String password) {
        StringBuilder sb = new StringBuilder();
        MessageDigest md = MessageDigest.getInstance("SHA-512");

        byte[] saltBytes = Base64.getDecoder().decode(salt);

        md.update(saltBytes);
        byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
        // Loop through the digest
        for (byte b : digest) {
            // Append the byte to the StringBuilder
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @SneakyThrows
    public static Map.Entry<String, String> hashWithRandomSalt(String password) {
        // Create a new StringBuilder object
        StringBuilder sb = new StringBuilder();

        // Initialize a new SecureRandom object
        SecureRandom random = new SecureRandom();

        // Initialize a new byte array of size 16
        byte[] salt = new byte[16];

        // Fill the byte array with random bytes
        random.nextBytes(salt);

        // Initialize a new MessageDigest object with the SHA-512 algorithm
        MessageDigest md = MessageDigest.getInstance("SHA-512");

        // Update the digest with the salt
        md.update(salt);
        String saltString = Base64.getEncoder().encodeToString(salt);

        // Digest the password
        byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));

        // Loop through the hashed password
        for (byte b : hashedPassword) {
            // Append the byte to the StringBuilder
            sb.append(String.format("%02x", b));
        }

        // Return the salt and hashed password
        return Map.entry(saltString, sb.toString());
    }
}
