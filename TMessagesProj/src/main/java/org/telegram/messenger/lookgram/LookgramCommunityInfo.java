package org.telegram.messenger.lookgram;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LookgramCommunityInfo {
    private static final String SERVER = "http://78.154.103.51:11563";

    public interface Callback {
        void onSuccess(JSONObject community);
        void onError(Exception e);
    }

    public static void updateAsync(
            int communityId,
            long telegramId,
            String name,
            String username,
            String description,
            String avatar,
            Callback callback
    ) {
        new Thread(() -> {
            try {
                URL url = new URL(
                        SERVER + "/communities/" + communityId + "/info"
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );
                connection.setRequestProperty(
                        "X-Telegram-ID",
                        String.valueOf(telegramId)
                );
                connection.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("name", name);
                body.put("username", username);
                body.put("description", description);
                body.put("avatar", avatar);

                byte[] data = body.toString()
                        .getBytes(StandardCharsets.UTF_8);

                try (OutputStream output = connection.getOutputStream()) {
                    output.write(data);
                }

                int code = connection.getResponseCode();

                if (code < 200 || code >= 300) {
                    throw new Exception("HTTP " + code);
                }

                java.io.BufferedReader reader =
                        new java.io.BufferedReader(
                                new java.io.InputStreamReader(
                                        connection.getInputStream()
                                )
                        );

                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();

                JSONObject json =
                        new JSONObject(result.toString());

                callback.onSuccess(
                        json.optJSONObject("community")
                );

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
}
