package org.telegram.messenger.lookgram;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LookgramCommunityMessages {

    private static final String SERVER = "http://78.154.103.51:11563";

    public interface Callback {
        void onSuccess(JSONArray messages);
        void onError(Exception e);
    }

    public static void getAsync(
            int communityId,
            long telegramId,
            Callback callback
    ) {
        new Thread(() -> {
            try {
                URL url = new URL(
                        SERVER + "/communities/" +
                        communityId + "/messages"
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty(
                        "X-Telegram-ID",
                        String.valueOf(telegramId)
                );

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

                JSONObject json =
                        new JSONObject(result.toString());

                callback.onSuccess(
                        json.optJSONArray("messages")
                                != null
                                ? json.optJSONArray("messages")
                                : new JSONArray()
                );

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public static void sendAsync(
            int communityId,
            long telegramId,
            String text,
            Callback callback
    ) {
        sendAsync(communityId, telegramId, text, null, callback);
    }

    public static void sendAsync(
            int communityId,
            long telegramId,
            String text,
            Integer replyTo,
            Callback callback
    ) {
        new Thread(() -> {
            try {
                URL url = new URL(
                        SERVER + "/communities/" +
                        communityId + "/messages"
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );
                connection.setRequestProperty(
                        "X-Telegram-ID",
                        String.valueOf(telegramId)
                );

                JSONObject body = new JSONObject();
                body.put("text", text);
                if (replyTo != null) {
                    body.put("replyTo", replyTo);
                }

                OutputStream output =
                        connection.getOutputStream();

                output.write(
                        body.toString().getBytes("UTF-8")
                );
                output.close();

                if (connection.getResponseCode() < 200 ||
                        connection.getResponseCode() >= 300) {
                    throw new Exception(
                            "HTTP " +
                            connection.getResponseCode()
                    );
                }

                callback.onSuccess(new JSONArray());

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
    public static void editAsync(
            int communityId,
            long telegramId,
            int messageId,
            String text,
            Callback callback
    ) {
        new Thread(() -> {
            try {
                URL url = new URL(
                        SERVER + "/communities/" +
                        communityId + "/messages/" +
                        messageId
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("PUT");
                connection.setDoOutput(true);
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );
                connection.setRequestProperty(
                        "X-Telegram-ID",
                        String.valueOf(telegramId)
                );

                JSONObject body = new JSONObject();
                body.put("text", text);

                OutputStream output =
                        connection.getOutputStream();

                output.write(
                        body.toString().getBytes("UTF-8")
                );
                output.close();

                int code = connection.getResponseCode();

                if (code < 200 || code >= 300) {
                    throw new Exception("HTTP " + code);
                }

                callback.onSuccess(new JSONArray());

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

}
