package org.telegram.messenger.lookgram;

import android.content.Context;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LookgramStars {

    private static final String SERVER = "http://78.154.103.51:11563";
    private final long userId;

    public LookgramStars(Context context, long userId) {
        this.userId = userId;
    }

    public interface BalanceCallback {
        void onResult(long balance);
        void onError(Exception e);
    }

    public void getBalanceAsync(BalanceCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(SERVER + "/stars/" + userId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );

                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                connection.disconnect();

                JSONObject json = new JSONObject(result.toString());
                long balance = json.getLong("stars");

                callback.onResult(balance);

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
}
