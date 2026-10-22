package org.telegram.messenger.lookgram;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LookgramCommunityMemberList {

    private static final String SERVER = "http://78.154.103.51:11563";

    public interface Callback {
        void onSuccess(JSONArray members);
        void onError(Exception e);
    }

    public static void getAsync(
            int communityId,
            Callback callback
    ) {
        new Thread(() -> {
            try {
                URL url = new URL(
                        SERVER + "/communities/" +
                        communityId + "/members"
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

                JSONObject json =
                        new JSONObject(result.toString());

                JSONArray members =
                        json.optJSONArray("members");

                callback.onSuccess(
                        members != null
                                ? members
                                : new JSONArray()
                );

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
}
