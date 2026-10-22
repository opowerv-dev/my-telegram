package org.telegram.messenger.lookgram;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LookgramCommunityDetails {

    private static final String SERVER =
            "http://78.154.103.51:11563";

    public interface Callback {
        void onSuccess(JSONObject community);
        void onError(Exception e);
    }

    public static void getAsync(
            int communityId,
            Callback callback
    ) {
        new Thread(() -> {
            try {
                URL url = new URL(
                        SERVER + "/communities/" + communityId
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");

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
