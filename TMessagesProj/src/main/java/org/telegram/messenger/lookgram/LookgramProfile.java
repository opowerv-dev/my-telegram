package org.telegram.messenger.lookgram;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LookgramProfile {

    private static final String SERVER = "http://78.154.103.51:11563";

    public long id;
    public String username;
    public String name;
    public String bio;
    public String avatar;
    public boolean verified;
    public boolean premium;
    public long premiumUntil;
    public boolean frozen;
    public long freezeUntil;
    public boolean freezePermanent;

    public interface Callback {
        void onResult(LookgramProfile profile);
        void onError(Exception e);
    }

    private static LookgramProfile parseProfile(JSONObject json) throws Exception {
        LookgramProfile profile = new LookgramProfile();

        profile.id = json.getLong("id");
        profile.verified = json.optBoolean("verified", false);
        profile.premium = json.optBoolean("premium", false);
        profile.premiumUntil = json.optLong("premiumUntil", 0);
        profile.frozen = json.optBoolean("frozen", false);
        profile.freezeUntil = json.optLong("freezeUntil", 0);
        profile.freezePermanent = json.optBoolean("freezePermanent", false);

        JSONObject p = json.optJSONObject("profile");
        if (p != null) {
            profile.username = p.optString("username", "");
            profile.name = p.optString("name", "");
            profile.bio = p.optString("bio", "");
            profile.avatar = p.optString("avatar", "");
        }

        return profile;
    }

    public static void getAsync(long userId, Callback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(SERVER + "/users/by-telegram/" + userId);
                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );

                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                connection.disconnect();

                callback.onResult(parseProfile(
                        new JSONObject(result.toString())
                ));

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public static void getOrCreateAsync(long telegramId, Callback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(SERVER + "/users");
                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty(
                        "Content-Type", "application/json"
                );
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                JSONObject body = new JSONObject();
                body.put("telegramId", telegramId);

                OutputStream output = connection.getOutputStream();
                output.write(body.toString().getBytes("UTF-8"));
                output.close();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );

                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                connection.disconnect();

                callback.onResult(parseProfile(
                        new JSONObject(result.toString())
                ));

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public static void updateAsync(
            long telegramId,
            long userId,
            String username,
            String name,
            String bio,
            String avatar,
            Callback callback
    ) {
        new Thread(() -> {
            try {
                URL url = new URL(
                        SERVER + "/users/" + userId + "/profile"
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("PUT");
                connection.setRequestProperty(
                        "X-Telegram-ID", String.valueOf(telegramId)
                );
                connection.setRequestProperty(
                        "Content-Type", "application/json"
                );
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                JSONObject body = new JSONObject();
                body.put("username", username);
                body.put("name", name);
                body.put("bio", bio);
                body.put("avatar", avatar);

                OutputStream output = connection.getOutputStream();
                output.write(body.toString().getBytes("UTF-8"));
                output.close();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );

                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                connection.disconnect();

                callback.onResult(parseProfile(
                        new JSONObject(result.toString())
                ));

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
}
