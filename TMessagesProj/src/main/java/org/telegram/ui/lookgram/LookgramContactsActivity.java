package org.telegram.ui.lookgram;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AccountInstance;
import org.telegram.ui.ActionBar.BaseFragment;

public class LookgramContactsActivity extends BaseFragment {

    private LinearLayout list;
    private long currentUserId;

    public LookgramContactsActivity() {
        super();
    }

    @Override
    public View createView(Context context) {
        actionBar.setTitle("Контакты");

        currentUserId = AccountInstance
                .getInstance(currentAccount)
                .getUserConfig()
                .clientUserId;

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);

        list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);

        root.addView(
                list,
                new LinearLayout.LayoutParams(
                        -1,
                        -1
                )
        );

        loadContacts();

        return root;
    }

    private void loadContacts() {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(
                        "http://78.154.103.51:11563/friends/" + currentUserId
                );

                java.net.HttpURLConnection connection =
                        (java.net.HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");

                java.io.BufferedReader reader =
                        new java.io.BufferedReader(
                                new java.io.InputStreamReader(
                                        connection.getInputStream()
                                )
                        );

                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                connection.disconnect();

                org.json.JSONArray contacts =
                        new org.json.JSONArray(response.toString());

                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() -> {
                        list.removeAllViews();

                        if (contacts.length() == 0) {
                            TextView empty = new TextView(
                                    getParentActivity()
                            );
                            empty.setText("Контактов пока нет");
                            empty.setGravity(Gravity.CENTER);
                            empty.setPadding(32, 32, 32, 32);

                            list.addView(
                                    empty,
                                    new LinearLayout.LayoutParams(
                                            -1,
                                            -2
                                    )
                            );
                            return;
                        }

                        for (int i = 0; i < contacts.length(); i++) {
                            org.json.JSONObject item =
                                    contacts.optJSONObject(i);

                            if (item == null) {
                                continue;
                            }

                            long userId = item.optLong("id", 0);
                            org.json.JSONObject profile =
                                    item.optJSONObject("profile");

                            String name = profile != null
                                    ? profile.optString("name", "")
                                    : "";

                            String username = profile != null
                                    ? profile.optString("username", "")
                                    : "";

                            TextView row = new TextView(
                                    getParentActivity()
                            );

                            String title = name.isEmpty()
                                    ? "Пользователь"
                                    : name;

                            if (!username.isEmpty()) {
                                title += "\n@" + username;
                            }

                            row.setText(title);
                            row.setTextSize(17);
                            row.setPadding(32, 24, 32, 24);

                            final long finalUserId = userId;

                            row.setOnClickListener(v ->
                                    presentFragment(
                                            new LookgramProfileActivity(
                                                    finalUserId
                                            )
                                    )
                            );

                            list.addView(
                                    row,
                                    new LinearLayout.LayoutParams(
                                            -1,
                                            -2
                                    )
                            );
                        }
                    });
                }
            } catch (Exception ignored) {
            }
        }).start();
    }
}
