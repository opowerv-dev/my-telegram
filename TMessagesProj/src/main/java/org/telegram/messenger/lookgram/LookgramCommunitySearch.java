package org.telegram.messenger.lookgram;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LookgramCommunitySearch {

    private static final String SERVER = "http://78.154.103.51:11563";

    public interface Callback {
        void onSuccess(JSONObject community);
        void onError(Exception e);
    }

    public static void searchAsync(String username, Callback callback) {
        String cleaned = username == null ? "" : username.trim();

        if (cleaned.startsWith("@")) {
            cleaned = cleaned.substring(1);
        }

        final String searchUsername = cleaned;

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                if (searchUsername.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Username cannot be empty"
                    );
                }

                String encoded = URLEncoder.encode(
                        searchUsername, StandardCharsets.UTF_8.name()
                );

                URL url = new URL(
                        SERVER + "/communities/by-username/" + encoded
                );

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int code = connection.getResponseCode();

                if (code < 200 || code >= 300) {
                    throw new Exception("HTTP " + code);
                }

                StringBuilder result = new StringBuilder();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream(),
                                StandardCharsets.UTF_8
                        ))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                }

                callback.onSuccess(new JSONObject(result.toString()));

            } catch (Exception e) {
                callback.onError(e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}
