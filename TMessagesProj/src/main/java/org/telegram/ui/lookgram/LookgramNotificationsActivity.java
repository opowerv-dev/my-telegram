package org.telegram.ui.lookgram;

import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.ui.ActionBar.BaseFragment;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class LookgramNotificationsActivity extends BaseFragment {


    private LinearLayout list;

    @Override
    public View createView(android.content.Context context) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);

        TextView title = new TextView(context);
        title.setText("Уведомления");
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        title.setPadding(20, 25, 20, 25);
        root.addView(title);

        android.widget.Button refreshButton =
                new android.widget.Button(context);
        refreshButton.setText("Обновить");
        refreshButton.setOnClickListener(v ->
                loadNotifications()
        );
        root.addView(
                refreshButton,
                new LinearLayout.LayoutParams(-1, -2)
        );

        list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        root.addView(list, new LinearLayout.LayoutParams(-1, -1));

        loadNotifications();

        return root;
    }

    private void loadNotifications() {
        final long currentUserId = org.telegram.messenger.AccountInstance
                .getInstance(currentAccount)
                .getUserConfig().clientUserId;

        new Thread(() -> {
            try {
                URL url = new URL(
                        "http://78.154.103.51:11563/notifications?userId="
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

                JSONArray notifications =
                        json.optJSONArray("notifications");

                if (notifications == null) {
                    notifications = new JSONArray();
                }

                JSONArray finalNotifications = notifications;

                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() -> {
                        list.removeAllViews();

                        if (finalNotifications.length() == 0) {
                            TextView empty = new TextView(
                                    getParentActivity()
                            );
                            empty.setText("Новых уведомлений нет");
                            empty.setTextColor(Color.LTGRAY);
                            empty.setTextSize(17);
                            empty.setPadding(20, 30, 20, 30);
                            list.addView(empty);
                            return;
                        }

                        for (int i = 0;
                             i < finalNotifications.length();
                             i++) {
                            try {
                                JSONObject notification =
                                        finalNotifications.getJSONObject(i);

                                TextView item = new TextView(
                                        getParentActivity()
                                );

                                String text =
                                        notification.optString(
                                                "text",
                                                "Новое уведомление"
                                        );

                                item.setText(text);
                                item.setTextColor(Color.WHITE);
                                item.setTextSize(16);
                                item.setPadding(20, 25, 20, 25);

                                list.addView(item);

                            } catch (Exception ignored) {
                            }
                        }
                    });
                }

            } catch (Exception e) {
                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() ->
                            Toast.makeText(
                                    getParentActivity(),
                                    "Не удалось загрузить уведомления",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
                }
            }
        }).start();
    }
}
