package org.telegram.messenger.lookgram;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LookgramCommunityMemberCheck {

    private static final String SERVER = "http://78.154.103.51:11563";

    public interface Callback {
        void onSuccess(boolean member, boolean owner);
        void onError(Exception e);
    }

    public static void checkAsync(
            int communityId,
            long telegramId,
            Callback callback
    ) {
        new Thread(() -> {
            try {
                URL url = new URL(
                        SERVER + "/communities/" +
                        communityId + "/member/" +
                        telegramId
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");

                int code = connection.getResponseCode();

                if (code < 200 || code >= 300) {
                    throw new Exception("HTTP " + code);
                }

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
                        json.optBoolean("member"),
                        json.optBoolean("owner")
                );

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
}
