package org.telegram.messenger.lookgram;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LookgramCommunitySearch {

    private static final String SERVER = "http://78.154.103.51:11563";

    public interface Callback {
        void onSuccess(JSONObject community);
        void onError(Exception e);
    }

    public static void searchAsync(
            String username,
            Callback callback
    ) {
        new Thread(() -> {
            try {
                username = username.trim();

                if (username.startsWith("@")) {
                    username = username.substring(1);
                }

                URL url = new URL(
                        SERVER + "/communities/by-username/" +
                        java.net.URLEncoder.encode(username, "UTF-8")
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");

                int code = connection.getResponseCode();

                if (code < 200 || code >= 300) {
                    throw new Exception("HTTP " + code);
                }

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        connection.getInputStream()
                                )
                        );

                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();

                callback.onSuccess(
                        new JSONObject(result.toString())
                );

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
}
