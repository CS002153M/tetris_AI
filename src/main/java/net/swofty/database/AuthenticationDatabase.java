package net.swofty.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.swofty.Resources;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class AuthenticationDatabase {

    private final String id;

    public static MongoClient client;
    public static MongoDatabase database;
    public static MongoCollection<Document> collection;

    public AuthenticationDatabase(String queryKey) {
        this.id = queryKey;
    }

    public AuthenticationDatabase connect() {
        ConnectionString cs = new ConnectionString((Resources.get("mongodb")));
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(cs).build();
        client = MongoClients.create(settings);

        database = client.getDatabase("Auth");
        collection = database.getCollection("auth");
        return this;
    }

    @Override
    public String toString() {
        return "AuthenticationDatabase{" +
                "name='" + id + '\'' +
                '}';
    }

    public void set(String key, Object value) {
        Document query = new Document("id", id);
        Document found = collection.find(query).first();

        if (found == null) {
            Document doc = new Document("id", id);
            doc.append(key, value);
            collection.insertOne(doc);
            return;
        }

        collection.updateOne(Filters.eq("id", id), Updates.set(key, value));
    }

    public Object get(String key, Object def) {
        Document query = new Document("id", id);
        Document found = collection.find(query).first();

        if (found == null) {
            return def;
        }

        Object res = found.get(key);
        if (res == null)
            return def;

        return res;
    }

    public String getString(String key, String def) {
        return get(key, def).toString();
    }

    public int getInt(String key, int def) {
        return Integer.parseInt(get(key, def).toString());
    }

    public long getLong(String key, long def) {
        return Long.parseLong(getString(key, def + ""));
    }

    public boolean getBoolean(String key, boolean def) {
        return Boolean.parseBoolean(get(key, def).toString());
    }

    public <T> List<T> getList(String key, Class<T> t) {
        Document query = new Document("id", id);
        Document found = collection.find(query).first();

        if (found == null) {
            return new ArrayList<>();
        }

        return found.getList(key, t);
    }

    public boolean remove(String id) {
        Document query = new Document("id", id);
        Document found = collection.find(query).first();

        if (found == null) {
            return false;
        }

        collection.deleteOne(query);
        return true;
    }

    public Double getDouble(String key, Object def) {
        Object o = get(key, def);
        if (o == null)
            return null;

        return Double.parseDouble(get(key, def).toString());
    }

    public boolean exists() {
        Document query = new Document("id", id);
        Document found = collection.find(query).first();
        return found != null;
    }

    public static Document getDocumentWithId(String id) {
        Document query = new Document("id", id);
        return collection.find(query).first();
    }
}
