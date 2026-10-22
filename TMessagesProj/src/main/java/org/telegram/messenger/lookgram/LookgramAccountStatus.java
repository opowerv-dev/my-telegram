package org.telegram.messenger.lookgram;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LookgramAccountStatus {

    private static final String SERVER =
            "http://78.154.103.51:11563";

    public interface Callback {
        void onActive();
        void onFrozen(long until, boolean permanent);
        void onError(Exception e);
    }

    public static void checkAsync(long telegramId, Callback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(
                        SERVER + "/users/by-telegram/" + telegramId
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty(
                        "X-Telegram-ID",
                        String.valueOf(telegramId)
                );
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int code = connection.getResponseCode();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                code >= 200 && code < 300
                                        ? connection.getInputStream()
                                        : connection.getErrorStream()
                        )
                );

                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                connection.disconnect();

                if (code == 423) {
                    JSONObject json =
                            new JSONObject(result.toString());

                    callback.onFrozen(
                            json.optLong("freezeUntil", 0),
                            json.optBoolean("permanent", false)
                    );
                    return;
                }

                if (code < 200 || code >= 300) {
                    throw new Exception(
                            "HTTP " + code + ": " + result
                    );
                }

                callback.onActive();

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
}
