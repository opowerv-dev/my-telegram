package org.telegram.ui.lookgram;

import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.ui.ActionBar.BaseFragment;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class LookgramChatSearchActivity extends BaseFragment {


    private EditText query;
    private LinearLayout results;

    @Override
    public View createView(android.content.Context context) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);

        query = new EditText(context);
        query.setHint("Поиск сообщений");
        query.setTextColor(Color.WHITE);
        query.setHintTextColor(Color.GRAY);
        root.addView(query, new LinearLayout.LayoutParams(-1, -2));

        android.widget.Button search =
                new android.widget.Button(context);
        search.setText("Найти");
        search.setOnClickListener(v -> searchChats());
        root.addView(search, new LinearLayout.LayoutParams(-1, -2));

        results = new LinearLayout(context);
        results.setOrientation(LinearLayout.VERTICAL);
        root.addView(results,
                new LinearLayout.LayoutParams(-1, -1));

        return root;
    }

    private void searchChats() {
        final long currentUserId = org.telegram.messenger.AccountInstance
                .getInstance(currentAccount)
                .getUserConfig().clientUserId;

        String q = query.getText().toString().trim();

        if (q.isEmpty()) {
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL(
                        "http://78.154.103.51:11563/chats/private/search?q="
                                + URLEncoder.encode(q, "UTF-8")
                                + "&userId="
                                + currentUserId
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");

                Scanner scanner =
                        new Scanner(connection.getInputStream())
                                .useDelimiter("\\A");

                String response =
                        scanner.hasNext() ? scanner.next() : "";

                JSONObject json =
                        new JSONObject(response);

                JSONArray messages =
                        json.optJSONArray("messages");

                if (messages == null) {
                    messages = new JSONArray();
                }

                JSONArray finalMessages = messages;

                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() -> {
                        results.removeAllViews();

                        for (int i = 0;
                             i < finalMessages.length();
                             i++) {
                            try {
                                JSONObject message =
                                        finalMessages.getJSONObject(i);

                                TextView item =
                                        new TextView(
                                                getParentActivity()
                                        );

                                item.setText(
                                        message.optString(
                                                "text",
                                                ""
                                        )
                                );

                                item.setTextColor(Color.WHITE);
                                item.setTextSize(16);
                                item.setPadding(
                                        20, 25, 20, 25
                                );

                                final int chatId =
                                        message.optInt(
                                                "chatId", 0
                                        );

                                item.setOnClickListener(v -> {
                                    if (chatId != 0) {
                                        presentFragment(
                                                new LookgramChatActivity(
                                                        chatId
                                                )
                                        );
                                    }
                                });

                                results.addView(item);

                            } catch (Exception ignored) {
                            }
                        }
                    });
                }

            } catch (Exception ignored) {
            }
        }).start();
    }
}
