package org.telegram.messenger.lookgram;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LookgramCommunityModeration {

    private static final String SERVER = "http://78.154.103.51:11563";

    public interface Callback {
        void onResult(boolean success, String error);
    }

    private static void request(
            int communityId,
            long actorTelegramId,
            long targetTelegramId,
            String action,
            Callback callback
    ) {
        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(
                        SERVER + "/communities/" + communityId + "/" + action
                );

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );
                connection.setRequestProperty(
                        "x-telegram-id",
                        String.valueOf(actorTelegramId)
                );

                JSONObject body = new JSONObject();
                body.put(
                        "telegramId",
                        String.valueOf(targetTelegramId)
                );

                byte[] data = body.toString().getBytes("UTF-8");

                OutputStream output = connection.getOutputStream();
                output.write(data);
                output.flush();
                output.close();

                int code = connection.getResponseCode();

                if (code >= 200 && code < 300) {
                    callback.onResult(true, null);
                } else {
                    callback.onResult(
                            false,
                            "HTTP " + code
                    );
                }

            } catch (Exception e) {
                callback.onResult(false, e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    public static void mute(
            int communityId,
            long actorTelegramId,
            long targetTelegramId,
            Callback callback
    ) {
        request(
                communityId,
                actorTelegramId,
                targetTelegramId,
                "mute",
                callback
        );
    }

    public static void unmute(
            int communityId,
            long actorTelegramId,
            long targetTelegramId,
            Callback callback
    ) {
        request(
                communityId,
                actorTelegramId,
                targetTelegramId,
                "unmute",
                callback
        );
    }

    public static void ban(
            int communityId,
            long actorTelegramId,
            long targetTelegramId,
            Callback callback
    ) {
        request(
                communityId,
                actorTelegramId,
                targetTelegramId,
                "ban",
                callback
        );
    }

    public static void unban(
            int communityId,
            long actorTelegramId,
            long targetTelegramId,
            Callback callback
    ) {
        request(
                communityId,
                actorTelegramId,
                targetTelegramId,
                "unban",
                callback
        );
    }
}
