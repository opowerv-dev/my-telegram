package org.telegram.ui.lookgram;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.telegram.messenger.AccountInstance;
import org.telegram.ui.ActionBar.BaseFragment;

public class LookgramChatsActivity extends BaseFragment {

    private LinearLayout chatsLayout;

    @Override
    public View createView(Context context) {
        actionBar.setTitle("Чаты");
        actionBar.setBackButtonImage(
                org.telegram.messenger.R.drawable.ic_ab_back
        );

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 30, 30, 30);

        android.widget.Button contactsButton =
                new android.widget.Button(context);
        contactsButton.setText("Контакты");
        contactsButton.setOnClickListener(v ->
                presentFragment(
                        new LookgramContactsActivity()
                )
        );

        root.addView(
                contactsButton,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        android.widget.Button notificationsButton =
                new android.widget.Button(context);
        notificationsButton.setText("Уведомления");
        notificationsButton.setOnClickListener(v ->
                presentFragment(
                        new LookgramNotificationsActivity()
                )
        );

        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(
                        "http://78.154.103.51:11563/notifications?userId=" + AccountInstance.getInstance(currentAccount).getUserConfig().clientUserId
                );

                java.net.HttpURLConnection connection =
                        (java.net.HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");

                java.util.Scanner scanner =
                        new java.util.Scanner(
                                connection.getInputStream()
                        ).useDelimiter("\\A");

                String response =
                        scanner.hasNext() ? scanner.next() : "";

                org.json.JSONObject json =
                        new org.json.JSONObject(response);

                org.json.JSONArray notifications =
                        json.optJSONArray("notifications");

                int count = notifications != null
                        ? notifications.length()
                        : 0;

                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() ->
                            notificationsButton.setText(
                                    count > 0
                                            ? "Уведомления [" + count + "]"
                                            : "Уведомления"
                            )
                    );
                }

            } catch (Exception ignored) {
            }
        }).start();

        root.addView(
                notificationsButton,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        android.widget.Button searchButton =
                new android.widget.Button(context);
        searchButton.setText("Поиск сообщений");
        searchButton.setOnClickListener(v ->
                presentFragment(
                        new LookgramChatSearchActivity()
                )
        );

        root.addView(
                searchButton,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );



        chatsLayout = new LinearLayout(context);
        chatsLayout.setOrientation(LinearLayout.VERTICAL);

        TextView empty = new TextView(context);
        empty.setText("Личных чатов пока нет");
        empty.setTextColor(Color.WHITE);
        empty.setTextSize(18);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(20, 60, 20, 60);

        chatsLayout.addView(empty);

        root.addView(
                chatsLayout,
                new LinearLayout.LayoutParams(
                        -1,
                        -1
                )
        );

        loadChats();
        startHeartbeat();

        return root;
    }

    private void startHeartbeat() {
        final long telegramUserId = AccountInstance
                .getInstance(currentAccount)
                .getUserConfig().clientUserId;
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    java.net.URL url = new java.net.URL(
                            "http://78.154.103.51:11563/users/" + telegramUserId + "/heartbeat"
                    );

                    java.net.HttpURLConnection connection =
                            (java.net.HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.getResponseCode();
                    connection.disconnect();

                    Thread.sleep(30000);

                } catch (Exception e) {
                    break;
                }
            }
        }).start();
    }

    private void loadChats() {
        final long telegramUserId = AccountInstance
                .getInstance(currentAccount)
                .getUserConfig()
                .clientUserId;

        new Thread(() -> {
            try {
                URL url = new URL("http://78.154.103.51:11563/chats/private");
                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty(
                        "X-Telegram-ID",
                        String.valueOf(telegramUserId)
                );

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );

                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                connection.disconnect();

                JSONArray chats = new JSONArray(response.toString());

                if (getParentActivity() == null) return;

                getParentActivity().runOnUiThread(() -> {
                    chatsLayout.removeAllViews();

                    if (chats.length() == 0) {
                        TextView empty = new TextView(
                                getParentActivity()
                        );
                        empty.setText("Личных чатов пока нет");
                        empty.setTextColor(Color.WHITE);
                        empty.setTextSize(18);
                        empty.setGravity(Gravity.CENTER);
                        chatsLayout.addView(empty);
                        return;
                    }

                    for (int i = 0; i < chats.length(); i++) {
                        try {
                            JSONObject chat =
                                    chats.getJSONObject(i);

                            JSONObject otherUser =
                                    chat.optJSONObject("otherUser");

                            JSONObject profile =
                                    otherUser != null
                                            ? otherUser.optJSONObject("profile")
                                            : null;

                            String name = profile != null
                                    ? profile.optString(
                                            "name",
                                            "Пользователь"
                                    )
                                    : "Пользователь";

                            LinearLayout item = new LinearLayout(
                                    getParentActivity()
                            );
                            item.setOrientation(LinearLayout.HORIZONTAL);
                            item.setGravity(android.view.Gravity.CENTER_VERTICAL);

                            int unreadCount =
                                    chat.optInt("unreadCount", 0);

                            JSONObject lastMessage =
                                    chat.optJSONObject("lastMessage");

                            String lastText = lastMessage != null
                                    ? lastMessage.optString("text", "")
                                    : "";

                            long createdAt = lastMessage != null
                                    ? lastMessage.optLong("createdAt", 0)
                                    : chat.optLong("createdAt", 0);

                            String time = "";
                            if (createdAt > 0) {
                                java.text.SimpleDateFormat format =
                                        new java.text.SimpleDateFormat(
                                                "HH:mm",
                                                java.util.Locale.getDefault()
                                        );
                                time = format.format(
                                        new java.util.Date(createdAt)
                                );
                            }

                            StringBuilder title =
                                    new StringBuilder(name);

                            if (!lastText.isEmpty()) {
                                title.append("\n")
                                        .append(lastText);
                            }

                            if (!time.isEmpty()) {
                                title.append("    ")
                                        .append(time);
                            }

                            if (unreadCount > 0) {
                                title.append("    [")
                                        .append(unreadCount)
                                        .append("]");
                            }

                            android.widget.ImageView avatar =
                                    new android.widget.ImageView(
                                            getParentActivity()
                                    );
                            avatar.setImageResource(
                                    android.R.drawable.sym_def_app_icon
                            );

                            try {
                                JSONObject otherUser =
                                        chat.optJSONObject("otherUser");
                                JSONObject profile =
                                        otherUser != null
                                                ? otherUser.optJSONObject("profile")
                                                : null;

                                String avatarData =
                                        profile != null
                                                ? profile.optString("avatar", "")
                                                : "";

                                if (!avatarData.isEmpty()) {
                                    byte[] bytes =
                                            android.util.Base64.decode(
                                                    avatarData,
                                                    android.util.Base64.DEFAULT
                                            );

                                    android.graphics.Bitmap bitmap =
                                            android.graphics.BitmapFactory
                                                    .decodeByteArray(
                                                            bytes,
                                                            0,
                                                            bytes.length
                                                    );

                                    if (bitmap != null) {
                                        avatar.setImageBitmap(bitmap);
                                    }
                                }
                            } catch (Exception ignored) {
                            }

                            item.addView(
                                    avatar,
                                    new LinearLayout.LayoutParams(
                                            72, 72
                                    )
                            );

                            TextView textView =
                                    new TextView(
                                            getParentActivity()
                                    );
                            textView.setText(title.toString());
                            textView.setTextColor(Color.WHITE);
                            textView.setTextSize(16);
                            textView.setPadding(20, 25, 20, 25);

                            item.addView(
                                    textView,
                                    new LinearLayout.LayoutParams(
                                            0,
                                            -2,
                                            1
                                    )
                            );
                            final int chatId =
                                    chat.optInt("id", 0);

                            item.setOnClickListener(v -> {
                                if (chatId != 0) {
                                    presentFragment(
                                            new LookgramChatActivity(chatId)
                                    );
                                }
                            });

                            chatsLayout.addView(item);

                        } catch (Exception ignored) {
                        }
                    }
                });

            } catch (Exception e) {
                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() ->
                            Toast.makeText(
                                    getParentActivity(),
                                    "Не удалось загрузить чаты",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
                }
            }
        }).start();
    }
}
