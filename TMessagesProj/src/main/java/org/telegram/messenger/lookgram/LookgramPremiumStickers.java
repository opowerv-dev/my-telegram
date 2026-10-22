package org.telegram.messenger.lookgram;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LookgramPremiumStickers {

    private static final String SERVER =
            "http://78.154.103.51:11563";

    public static class Sticker {
        public long id;
        public String name;
        public String emoji;
        public String image;
    }

    public interface Callback {
        void onResult(List<Sticker> stickers);
        void onError(Exception e);
    }

    public static void getAsync(Callback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(
                        SERVER + "/stickers/premium"
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        connection.getInputStream()
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

                JSONArray array =
                        new JSONArray(result.toString());

                List<Sticker> stickers =
                        new ArrayList<>();

                for (int i = 0; i < array.length(); i++) {
                    JSONObject json =
                            array.getJSONObject(i);

                    Sticker sticker = new Sticker();

                    sticker.id =
                            json.getLong("id");

                    sticker.name =
                            json.optString("name", "");

                    sticker.emoji =
                            json.optString("emoji", "");

                    sticker.image =
                            json.optString("image", "");

                    stickers.add(sticker);
                }

                callback.onResult(stickers);

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }


    public static void sendAsync(
            long fromId,
            long toId,
            long stickerId,
            Callback callback
    ) {
        new Thread(() -> {
            try {
                URL url = new URL(
                        SERVER + "/stickers/send"
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );
                connection.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("fromId", fromId);
                body.put("toId", toId);
                body.put("stickerId", stickerId);

                OutputStream output =
                        connection.getOutputStream();

                output.write(
                        body.toString().getBytes("UTF-8")
                );

                output.close();

                int code =
                        connection.getResponseCode();

                if (code != 200) {
                    throw new Exception("HTTP " + code);
                }

                connection.disconnect();

                callback.onResult(
                        new ArrayList<>()
                );

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public static void addToUserAsync(
            long userId,
            long stickerId,
            Callback callback
    ) {
        new Thread(() -> {
            try {
                URL url = new URL(
                        SERVER +
                        "/stickers/premium/add-to-user"
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );
                connection.setDoOutput(true);

                JSONObject body = new JSONObject();

                body.put("userId", userId);
                body.put("stickerId", stickerId);

                OutputStream output =
                        connection.getOutputStream();

                output.write(
                        body.toString().getBytes("UTF-8")
                );

                output.close();

                int code =
                        connection.getResponseCode();

                connection.disconnect();

                if (code != 200) {
                    throw new Exception(
                            "HTTP " + code
                    );
                }

                callback.onResult(
                        new ArrayList<>()
                );

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
}
