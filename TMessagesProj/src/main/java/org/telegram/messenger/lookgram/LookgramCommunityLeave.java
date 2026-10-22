package org.telegram.messenger.lookgram;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LookgramCommunityLeave {

    private static final String SERVER = "http://78.154.103.51:11563";

    public interface Callback {
        void onSuccess(JSONObject result);
        void onError(Exception e);
    }

    public static void leaveAsync(
            int communityId,
            long telegramId,
            Callback callback
    ) {
        new Thread(() -> {
            try {
                URL url = new URL(
                        SERVER + "/communities/" +
                        communityId + "/leave"
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty(
                        "X-Telegram-ID",
                        String.valueOf(telegramId)
                );
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );

                OutputStream output =
                        connection.getOutputStream();

                output.write("{}".getBytes("UTF-8"));
                output.close();

                int code = connection.getResponseCode();

                if (code < 200 || code >= 300) {
                    throw new Exception("HTTP " + code);
                }

                callback.onSuccess(
                        new JSONObject(
                                new java.io.BufferedReader(
                                        new java.io.InputStreamReader(
                                                connection.getInputStream()
                                        )
                                ).readLine()
                        )
                );

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
}
