package kaufisch.customchannel.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

/**
 * Reads the Bot Auth Token and language text from config.json.
 *
 * @author Kaufisch
 */

public class ConfigManager {

    private Gson gson;
    private BufferedReader br;
    private JsonObject english;
    private JsonObject german;

    /*
    Language IDs:
    English: 1
    German: 2
     */


    public ConfigManager() {
        gson = new Gson();
        getLanguages();
    }

    public JsonObject getEnglish() {
        return english;
    }

    public JsonObject getGerman() {
        return german;
    }

    public String getAuthToken() {
        try {
            br = new BufferedReader(new FileReader("./config.json"));
            JsonObject config = gson.fromJson(br, JsonObject.class);
            String TOKEN = config.getAsJsonObject("Discord").get("TOKEN").getAsString();
            if (TOKEN.equals("YourconfigBotToken")) {
                return "default values";
            } else {
                return TOKEN;
            }
        } catch (FileNotFoundException e) {
            return "not found";
        }
    }
  
    HashMap<String, String> getDatabaseCreds() {
        try {
            br = new BufferedReader(new FileReader("./config.json"));
            JsonObject config = gson.fromJson(br, JsonObject.class);
            HashMap<String, String> creds = new HashMap<>();
            creds.put("user", config.getAsJsonObject("Database").get("user").getAsString());
            creds.put("password", config.getAsJsonObject("Database").get("password").getAsString());
            creds.put("host", config.getAsJsonObject("Database").get("host").getAsString());
            creds.put("database", config.getAsJsonObject("Database").get("database").getAsString());
            return creds;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private void getLanguages() {
        try {
            br = new BufferedReader(new FileReader("./config.json"));
            JsonObject config = gson.fromJson(br, JsonObject.class);
            english = config.getAsJsonObject("Languages").getAsJsonObject("English");
            german = config.getAsJsonObject("Languages").getAsJsonObject("German");
        } catch (FileNotFoundException ignored) {
        }
    }

}
