package org.telegram.messenger.lookgram;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LookgramCommunity {

    private static final String SERVER =
            "http://78.154.103.51:11563";

    public interface Callback {
        void onSuccess(JSONObject community);
        void onError(Exception e);
    }

    public static void createAsync(
            long telegramId,
            String type,
            String name,
            Callback callback
    ) {
        new Thread(() -> {
            try {
                URL url = new URL(
                        SERVER + "/communities/create"
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

                JSONObject json = new JSONObject();
                json.put("type", type);
                json.put("name", name);
                json.put(
                        "ownerTelegramId",
                        String.valueOf(telegramId)
                );

                OutputStream output =
                        connection.getOutputStream();

                output.write(
                        json.toString().getBytes("UTF-8")
                );
                output.close();

                int code = connection.getResponseCode();

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        code >= 200 && code < 300
                                                ? connection.getInputStream()
                                                : connection.getErrorStream()
                                )
                        );

                StringBuilder result =
                        new StringBuilder();

                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                connection.disconnect();

                if (code < 200 || code >= 300) {
                    throw new Exception(
                            "HTTP " + code + ": " + result
                    );
                }

                callback.onSuccess(
                        new JSONObject(result.toString())
                );

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
}
