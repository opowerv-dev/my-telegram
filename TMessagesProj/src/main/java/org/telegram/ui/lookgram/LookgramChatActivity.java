package org.telegram.ui.lookgram;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
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

public class LookgramChatActivity extends BaseFragment {

    private final int chatId;
    private LinearLayout messagesLayout;
    private android.os.Handler chatHandler;
    private Runnable chatUpdater;
    private int replyToMessageId = 0;

    public LookgramChatActivity(int chatId) {
        this.chatId = chatId;
    }

    @Override
    public View createView(Context context) {
        actionBar.setTitle("Чат");
        actionBar.setBackButtonImage(
                org.telegram.messenger.R.drawable.ic_ab_back
        );

        actionBar.createMenu().addItem(
                1001,
                "Удалить чат"
        ).setOnClickListener(v ->
                new android.app.AlertDialog.Builder(getParentActivity())
                        .setTitle("Удалить чат?")
                        .setMessage("Удалить этот чат и все сообщения?")
                        .setNegativeButton("Отмена", null)
                        .setPositiveButton("Удалить", (d, w) -> deleteChat())
                        .show()
        );

        actionBar.createMenu().addItem(
                1002,
                "Поиск"
        ).setOnClickListener(v -> {
            final EditText searchInput = new EditText(getParentActivity());
            searchInput.setHint("Поиск сообщений");

            new android.app.AlertDialog.Builder(getParentActivity())
                    .setTitle("Поиск")
                    .setView(searchInput)
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Найти", (d, w) ->
                            searchMessages(
                                    searchInput.getText().toString().trim()
                            )
                    )
                    .show();
        });

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 20);

        messagesLayout = new LinearLayout(context);
        messagesLayout.setOrientation(LinearLayout.VERTICAL);

        root.addView(
                messagesLayout,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        TextView info = new TextView(context);
        info.setText("Chat ID: " + chatId);
        info.setTextColor(Color.WHITE);
        info.setTextSize(18);
        info.setGravity(Gravity.CENTER);

        root.addView(
                info,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        LinearLayout bottom = new LinearLayout(context);
        bottom.setOrientation(LinearLayout.HORIZONTAL);

        EditText input = new EditText(context);
        input.setHint("Сообщение");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);

        bottom.addView(
                input,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        TextView send = new TextView(context);
        send.setText("Отправить");
        send.setTextColor(Color.WHITE);
        send.setTextSize(16);
        send.setPadding(20, 20, 20, 20);

        bottom.addView(send);

        send.setOnClickListener(v -> {
            String text = input.getText().toString().trim();

            if (!text.isEmpty()) {
                sendMessage(text);
                input.setText("");
            }
        });

        root.addView(bottom);

        loadMessages();
        markChatAsRead();

        chatHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        chatUpdater = new Runnable() {
            @Override
            public void run() {
                loadMessages();
                markChatAsRead();
                if (chatHandler != null) {
                    chatHandler.postDelayed(this, 2000);
                }
            }
        };
        chatHandler.postDelayed(chatUpdater, 2000);

        return root;
    }

    @Override
    public void onFragmentDestroy() {
        if (chatHandler != null && chatUpdater != null) {
            chatHandler.removeCallbacks(chatUpdater);
        }
        chatHandler = null;
        chatUpdater = null;
        super.onFragmentDestroy();
    }

    private void loadMessages() {
        final long telegramUserId = AccountInstance
                .getInstance(currentAccount)
                .getUserConfig()
                .clientUserId;

        new Thread(() -> {
            try {
                URL url = new URL(
                        "http://78.154.103.51:11563/chats/"
                                + chatId + "/messages"
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty(
                        "X-Telegram-ID",
                        String.valueOf(telegramUserId)
                );

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
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

                JSONObject result =
                        new JSONObject(response.toString());

                JSONArray messages =
                        result.optJSONArray("messages");

                JSONObject chatObject =
                        result.optJSONObject("chat");

                int pinnedMessageId =
                        chatObject != null
                                ? chatObject.optInt("pinnedMessageId", 0)
                                : 0;

                if (messages == null) return;

                if (getParentActivity() == null) return;

                getParentActivity().runOnUiThread(() -> {
                    messagesLayout.removeAllViews();

                    if (pinnedMessageId > 0) {
                        for (int p = 0; p < messages.length(); p++) {
                            JSONObject pinned =
                                    messages.optJSONObject(p);

                            if (pinned != null &&
                                    pinned.optInt("id", 0) == pinnedMessageId) {

                                TextView pinnedView =
                                        new TextView(getParentActivity());

                                pinnedView.setText(
                                        "📌 Закреплено: " +
                                        pinned.optString("text", "")
                                );
                                pinnedView.setTextColor(Color.WHITE);
                                pinnedView.setTextSize(15);
                                pinnedView.setPadding(15, 12, 15, 12);

                                messagesLayout.addView(pinnedView);
                                break;
                            }
                        }
                    }

                    for (int i = 0; i < messages.length(); i++) {
                        try {
                            JSONObject message =
                                    messages.getJSONObject(i);

                            TextView item = new TextView(
                                    getParentActivity()
                            );

                            long createdAt = message.optLong("createdAt", 0);
                            String time = createdAt > 0
                                    ? new java.text.SimpleDateFormat(
                                            "HH:mm",
                                            java.util.Locale.getDefault()
                                      ).format(new java.util.Date(createdAt))
                                    : "";

                            String messageText =
                                    message.optString("text", "");

                            int replyId =
                                    message.optInt("replyTo", 0);

                            if (replyId > 0) {
                                for (int r = 0; r < messages.length(); r++) {
                                    JSONObject replied =
                                            messages.optJSONObject(r);

                                    if (replied != null &&
                                            replied.optInt("id", 0) == replyId) {
                                        messageText =
                                                "Ответ: " +
                                                replied.optString("text", "") +
                                                "\n\n" +
                                                messageText;
                                        break;
                                    }
                                }
                            }

                            item.setText(
                                    (message.optInt("id", 0) == pinnedMessageId
                                            ? "📌 "
                                            : "") +
                                    messageText +
                                    (time.isEmpty() ? "" : "  " + time)
                            );

                            item.setTextColor(Color.WHITE);
                            item.setTextSize(16);
                            item.setPadding(15, 12, 15, 12);

                            item.setOnLongClickListener(v -> {
                                long senderId = message.optLong("senderId", 0);
                                long myId = AccountInstance
                                        .getInstance(currentAccount)
                                        .getUserConfig()
                                        .clientUserId;

                                if (senderId == myId) {
                                    new android.app.AlertDialog.Builder(
                                            getParentActivity()
                                    )
                                            .setItems(
                                                    new String[]{"Ответить", "Редактировать", "Удалить", "Закрепить", "Открепить", "Переслать"},
                                                    (dialog, which) -> {
                                                        int messageId =
                                                                message.optInt("id", 0);

                                                        if (which == 3) {
                                                            pinMessage(messageId);
                                                            return;
                                                        }

                                                        if (which == 4) {
                                                            unpinMessage(messageId);
                                                            return;
                                                        }

                                                        if (which == 5) {
                                                            showForwardChatPicker(messageId);
                                                            return;
                                                        }

                                                        if (which == 0) {
                                                            replyToMessageId =
                                                                    message.optInt("id", 0);

                                                            Toast.makeText(
                                                                    getParentActivity(),
                                                                    "Ответ на сообщение выбран",
                                                                    Toast.LENGTH_SHORT
                                                            ).show();
                                                        } else if (which == 1) {
                                                            final EditText edit =
                                                                    new EditText(getParentActivity());
                                                            edit.setText(
                                                                    message.optString("text", "")
                                                            );

                                                            new android.app.AlertDialog.Builder(
                                                                    getParentActivity()
                                                            )
                                                                    .setTitle("Редактировать")
                                                                    .setView(edit)
                                                                    .setNegativeButton("Отмена", null)
                                                                    .setPositiveButton(
                                                                            "Сохранить",
                                                                            (d, w) ->
                                                                                    editMessage(
                                                                                            message.optInt("id", 0),
                                                                                            edit.getText().toString().trim()
                                                                                    )
                                                                    )
                                                                    .show();
                                                        } else {
                                                            new android.app.AlertDialog.Builder(
                                                                    getParentActivity()
                                                            )
                                                                    .setTitle("Удалить сообщение?")
                                                                    .setMessage("Удалить это сообщение?")
                                                                    .setNegativeButton("Отмена", null)
                                                                    .setPositiveButton(
                                                                            "Удалить",
                                                                            (d, w) ->
                                                                                    deleteMessage(
                                                                                            message.optInt("id", 0)
                                                                                    )
                                                                    )
                                                                    .show();
                                                        }
                                                    }
                                            )
                                            .show();
                                } else {
                                    replyToMessageId = message.optInt("id", 0);

                                    Toast.makeText(
                                            getParentActivity(),
                                            "Ответ на сообщение выбран",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }

                                return true;
                            });

                            messagesLayout.addView(item);

                            TextView reaction = new TextView(
                                    getParentActivity()
                            );
                            org.json.JSONArray reactions =
                                    message.optJSONArray("reactions");
                            int reactionCount =
                                    reactions != null ? reactions.length() : 0;

                            reaction.setText(
                                    reactionCount > 0
                                            ? "👍 " + reactionCount
                                            : "👍"
                            );
                            reaction.setTextSize(18);
                            reaction.setPadding(15, 2, 15, 8);
                            long myId =
                                    AccountInstance
                                            .getInstance(currentAccount)
                                            .getUserConfig()
                                            .clientUserId;

                            boolean reacted = false;
                            org.json.JSONArray reactionArray =
                                    message.optJSONArray("reactions");

                            if (reactionArray != null) {
                                for (int r = 0; r < reactionArray.length(); r++) {
                                    JSONObject reactionObject =
                                            reactionArray.optJSONObject(r);

                                    if (reactionObject != null &&
                                            reactionObject.optLong("userId", 0) == myId) {
                                        reacted = true;
                                        break;
                                    }
                                }
                            }

                            if (reacted) {
                                reaction.setTag("liked");
                            }

                            reaction.setOnClickListener(v -> {
                                int messageId = message.optInt("id", 0);

                                new android.app.AlertDialog.Builder(getParentActivity())
                                        .setTitle("Реакция")
                                        .setItems(
                                                new String[]{"👍", "❤️", "😂", "😡", "🔥", "Убрать реакцию"},
                                                (dialog, which) -> {
                                                    if (which == 5) {
                                                        removeReaction(messageId);
                                                        return;
                                                    }

                                                    String[] emojis =
                                                            {"👍", "❤️", "😂", "😡", "🔥"};

                                                    reactToMessage(
                                                            messageId,
                                                            emojis[which]
                                                    );
                                                }
                                        )
                                        .show());
                            messagesLayout.addView(reaction);

                        } catch (Exception ignored) {
                        }
                    }
                });

            } catch (Exception e) {
                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() ->
                            Toast.makeText(
                                    getParentActivity(),
                                    "Не удалось загрузить сообщения",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
                }
            }
        }).start();
    }


    private void searchMessages(String query) {
        final long telegramUserId = AccountInstance
                .getInstance(currentAccount)
                .getUserConfig()
                .clientUserId;

        new Thread(() -> {
            try {
                URL url = new URL(
                        "http://78.154.103.51:11563/chats/"
                                + chatId + "/search?q="
                                + java.net.URLEncoder.encode(query, "UTF-8")
                );

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

                JSONObject result =
                        new JSONObject(response.toString());

                JSONArray messages =
                        result.optJSONArray("messages");

                if (messages == null || getParentActivity() == null) {
                    return;
                }

                getParentActivity().runOnUiThread(() -> {
                    messagesLayout.removeAllViews();

                    for (int i = 0; i < messages.length(); i++) {
                        try {
                            JSONObject message =
                                    messages.getJSONObject(i);

                            TextView item = new TextView(
                                    getParentActivity()
                            );

                            long createdAt =
                                    message.optLong("createdAt", 0);

                            String time = createdAt > 0
                                    ? new java.text.SimpleDateFormat(
                                            "HH:mm",
                                            java.util.Locale.getDefault()
                                    ).format(
                                            new java.util.Date(createdAt)
                                    )
                                    : "";

                            item.setText(
                                    message.optString("text", "") +
                                            (time.isEmpty()
                                                    ? ""
                                                    : "  " + time)
                            );

                            item.setTextColor(Color.WHITE);
                            item.setTextSize(16);
                            item.setPadding(15, 12, 15, 12);

                            messagesLayout.addView(item);

                        } catch (Exception ignored) {
                        }
                    }
                });

            } catch (Exception ignored) {
            }
        }).start();
    }

    private void deleteMessage(int messageId) {
        final long telegramUserId = AccountInstance
                .getInstance(currentAccount)
                .getUserConfig()
                .clientUserId;

        new Thread(() -> {
            try {
                URL url = new URL(
                        "http://78.154.103.51:11563/chats/"
                                + chatId + "/messages/"
                                + messageId
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("DELETE");
                connection.setRequestProperty(
                        "X-Telegram-ID",
                        String.valueOf(telegramUserId)
                );

                int code = connection.getResponseCode();
                connection.disconnect();

                if (code >= 200 && code < 300
                        && getParentActivity() != null) {
                    getParentActivity().runOnUiThread(
                            this::loadMessages
                    );
                }

            } catch (Exception ignored) {
            }
        }).start();
    }

    private void editMessage(int messageId, String newText) {
        final long telegramUserId = AccountInstance
                .getInstance(currentAccount)
                .getUserConfig()
                .clientUserId;

        new Thread(() -> {
            try {
                URL url = new URL(
                        "http://78.154.103.51:11563/chats/"
                                + chatId + "/messages/"
                                + messageId
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("PUT");
                connection.setDoOutput(true);
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );
                connection.setRequestProperty(
                        "X-Telegram-ID",
                        String.valueOf(telegramUserId)
                );

                JSONObject body = new JSONObject();
                body.put("text", newText);

                connection.getOutputStream().write(
                        body.toString().getBytes("UTF-8")
                );

                connection.getResponseCode();
                connection.disconnect();

                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(
                            this::loadMessages
                    );
                }

            } catch (Exception ignored) {
            }
        }).start();
    }

    private void pinMessage(int messageId) {
        sendPinRequest(messageId, false);
    }

    private void unpinMessage(int messageId) {
        sendPinRequest(messageId, true);
    }

    private void sendPinRequest(int messageId, boolean unpin) {
        final long telegramUserId = AccountInstance
                .getInstance(currentAccount)
                .getUserConfig()
                .clientUserId;

        new Thread(() -> {
            try {
                String urlString;

                String urlString = "http://78.154.103.51:11563/chats/"
                        + chatId
                        + "/messages/"
                        + messageId
                        + "/pin";

                URL url = new URL(urlString);

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod(
                        unpin ? "DELETE" : "POST"
                );

                connection.setRequestProperty(
                        "X-Telegram-ID",
                        String.valueOf(telegramUserId)
                );

                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int code = connection.getResponseCode();

                connection.disconnect();

                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() -> {
                        if (code >= 200 && code < 300) {
                            loadMessages();
                        } else {
                            android.widget.Toast.makeText(
                                    getParentActivity(),
                                    unpin
                                            ? "Не удалось открепить сообщение"
                                            : "Не удалось закрепить сообщение",
                                    android.widget.Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
                }

            } catch (Exception e) {
                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() ->
                            android.widget.Toast.makeText(
                                    getParentActivity(),
                                    "Ошибка соединения с Lookgram",
                                    android.widget.Toast.LENGTH_SHORT
                            ).show()
                    );
                }
            }
        }).start();
    }

    private void showForwardChatPicker(int messageId) {
        final long telegramUserId = AccountInstance
                .getInstance(currentAccount)
                .getUserConfig()
                .clientUserId;

        new Thread(() -> {
            try {
                URL url = new URL(
                        "http://78.154.103.51:11563/chats/private"
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty(
                        "X-Telegram-ID",
                        String.valueOf(telegramUserId)
                );

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
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

                JSONArray chats =
                        new JSONArray(response.toString());

                if (getParentActivity() == null) return;

                getParentActivity().runOnUiThread(() -> {
                    String[] names = new String[chats.length()];
                    int[] ids = new int[chats.length()];

                    for (int i = 0; i < chats.length(); i++) {
                        JSONObject chat =
                                chats.optJSONObject(i);

                        JSONObject otherUser =
                                chat != null
                                        ? chat.optJSONObject("otherUser")
                                        : null;

                        JSONObject profile =
                                otherUser != null
                                        ? otherUser.optJSONObject("profile")
                                        : null;

                        names[i] = profile != null
                                ? profile.optString(
                                        "name",
                                        "Пользователь"
                                )
                                : "Пользователь";

                        ids[i] = chat != null
                                ? chat.optInt("id", 0)
                                : 0;
                    }

                    new android.app.AlertDialog.Builder(
                            getParentActivity()
                    )
                            .setTitle("Переслать в чат")
                            .setItems(
                                    names,
                                    (dialog, which) ->
                                            forwardMessage(
                                                    messageId,
                                                    ids[which]
                                            )
                            )
                            .setNegativeButton(
                                    "Отмена",
                                    null
                            )
                            .show();
                });
            } catch (Exception ignored) {
            }
        }).start();
    }

    private void forwardMessage(int messageId, int targetChatId) {
        final long telegramUserId = AccountInstance
                .getInstance(currentAccount)
                .getUserConfig()
                .clientUserId;

        new Thread(() -> {
            try {
                URL url = new URL(
                        "http://78.154.103.51:11563/chats/" + chatId
                                + "/messages/" + messageId + "/forward"
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );
                connection.setRequestProperty(
                        "X-Telegram-ID",
                        String.valueOf(telegramUserId)
                );
                connection.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("targetChatId", targetChatId);

                connection.getOutputStream().write(
                        body.toString().getBytes("UTF-8")
                );

                connection.getResponseCode();
                connection.disconnect();

                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(this::loadMessages);
                }
            } catch (Exception ignored) {
            }
        }).start();
    }

    private void removeReaction(int messageId) {
        final long telegramUserId = AccountInstance
                .getInstance(currentAccount)
                .getUserConfig()
                .clientUserId;

        new Thread(() -> {
            try {
                URL url = new URL(
                        "http://78.154.103.51:11563/chats/" + chatId
                                + "/messages/" + messageId + "/reaction"
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("DELETE");
                connection.setRequestProperty(
                        "X-Telegram-ID",
                        String.valueOf(telegramUserId)
                );

                connection.getResponseCode();
                connection.disconnect();

                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(this::loadMessages);
                }
            } catch (Exception ignored) {
            }
        }).start();
    }

    private void reactToMessage(int messageId, String emoji) {
        final long telegramUserId = AccountInstance
                .getInstance(currentAccount)
                .getUserConfig()
                .clientUserId;

        new Thread(() -> {
            try {
                URL url = new URL(
                        "http://78.154.103.51:11563/chats/"
                                + chatId + "/messages/"
                                + messageId + "/reaction"
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );
                connection.setRequestProperty(
                        "X-Telegram-ID",
                        String.valueOf(telegramUserId)
                );

                JSONObject body = new JSONObject();
                body.put("emoji", emoji);

                connection.getOutputStream().write(
                        body.toString().getBytes("UTF-8")
                );

                connection.getResponseCode();
                connection.disconnect();

                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(this::loadMessages);
                }

            } catch (Exception ignored) {
            }
        }).start();
    }

    private void markChatAsRead() {
        final long telegramUserId = AccountInstance
                .getInstance(currentAccount)
                .getUserConfig()
                .clientUserId;

        new Thread(() -> {
            try {
                URL url = new URL(
                        "http://78.154.103.51:11563/chats/"
                                + chatId + "/read"
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );
                connection.setRequestProperty(
                        "X-Telegram-ID",
                        String.valueOf(telegramUserId)
                );

                connection.getOutputStream().write(
                        "{}".getBytes("UTF-8")
                );

                connection.getInputStream().close();
                connection.disconnect();

            } catch (Exception ignored) {
            }
        }).start();
    }

    private void deleteChat() {
        final long telegramUserId = AccountInstance
                .getInstance(currentAccount)
                .getUserConfig()
                .clientUserId;

        new Thread(() -> {
            try {
                URL url = new URL(
                        "http://78.154.103.51:11563/chats/" + chatId
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("DELETE");
                connection.setRequestProperty(
                        "X-Telegram-ID",
                        String.valueOf(telegramUserId)
                );

                int code = connection.getResponseCode();
                connection.disconnect();

                if (code >= 200 && code < 300 && getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() -> {
                        finishFragment();
                    });
                }

            } catch (Exception e) {
                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() ->
                            Toast.makeText(
                                    getParentActivity(),
                                    "Не удалось удалить чат",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
                }
            }
        }).start();
    }

    private void sendMessage(String text) {
        final long telegramUserId = AccountInstance
                .getInstance(currentAccount)
                .getUserConfig()
                .clientUserId;

        new Thread(() -> {
            try {
                URL url = new URL(
                        "http://78.154.103.51:11563/chats/"
                                + chatId + "/messages"
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );
                connection.setRequestProperty(
                        "X-Telegram-ID",
                        String.valueOf(telegramUserId)
                );

                JSONObject body = new JSONObject();
                body.put("text", text);

                if (replyToMessageId > 0) {
                    body.put("replyTo", replyToMessageId);
                }

                connection.getOutputStream().write(
                        body.toString().getBytes("UTF-8")
                );

                connection.getInputStream().close();
                connection.disconnect();

                replyToMessageId = 0;

                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(
                            this::loadMessages
                    );
                }

            } catch (Exception e) {
                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() ->
                            Toast.makeText(
                                    getParentActivity(),
                                    "Не удалось отправить сообщение",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
                }
            }
        }).start();
    }


}
