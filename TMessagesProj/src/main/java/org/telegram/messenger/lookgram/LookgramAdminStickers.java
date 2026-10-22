package org.telegram.messenger.lookgram;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LookgramAdminStickers {

    private static final String SERVER =
            "http://78.154.103.51:11563";

    public interface Callback {
        void onResult(String result);
        void onError(Exception e);
    }

    public static void addAsync(
            long adminTelegramId,
            String name,
            String emoji,
            String image,
            boolean animated,
            Callback callback
    ) {
        new Thread(() -> {
            try {
                URL url = new URL(SERVER + "/admin/stickers/add");

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );
                connection.setRequestProperty(
                        "X-Telegram-ID",
                        String.valueOf(adminTelegramId)
                );

                JSONObject json = new JSONObject();
                json.put("name", name);
                json.put("emoji", emoji);
                json.put("image", image);
                json.put("animated", animated);

                OutputStream output = connection.getOutputStream();
                output.write(json.toString().getBytes("UTF-8"));
                output.close();

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

                if (code < 200 || code >= 300) {
                    throw new Exception(
                            "HTTP " + code + ": " + result
                    );
                }

                callback.onResult(result.toString());

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
}
