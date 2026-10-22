package org.telegram.messenger.lookgram;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LookgramStickers {

    private static final String SERVER =
            "http://78.154.103.51:11563";

    public static class Sticker {
        public long id;
        public String name;
        public String emoji;
        public String image;
        public boolean animated;
    }

    public interface Callback {
        void onResult(List<Sticker> stickers);
        void onError(Exception e);
    }

    public static void getAsync(Callback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(SERVER + "/stickers");
                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(
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
                connection.disconnect();

                JSONArray array = new JSONArray(result.toString());
                List<Sticker> stickers = new ArrayList<>();

                for (int i = 0; i < array.length(); i++) {
                    JSONObject json = array.getJSONObject(i);

                    Sticker sticker = new Sticker();
                    sticker.id = json.getLong("id");
                    sticker.name = json.optString("name", "");
                    sticker.emoji = json.optString("emoji", "");
                    sticker.image = json.optString("image", "");
                    sticker.animated =
                            json.optBoolean("animated", false);

                    stickers.add(sticker);
                }

                callback.onResult(stickers);

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
}
