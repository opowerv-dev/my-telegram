package org.telegram.ui.lookgram;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.telegram.messenger.lookgram.LookgramCommunityDetails;
import org.telegram.ui.ActionBar.BaseFragment;

public class LookgramCommunityActivity extends BaseFragment {

    private int communityId;

    public LookgramCommunityActivity(int communityId) {
        this.communityId = communityId;
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(
                org.telegram.messenger.R.drawable.ic_ab_back
        );

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        android.widget.ImageView avatarView =
                new android.widget.ImageView(context);
        avatarView.setLayoutParams(
                new LinearLayout.LayoutParams(180, 180)
        );
        avatarView.setScaleType(
                android.widget.ImageView.ScaleType.CENTER_CROP
        );
        layout.addView(avatarView);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(32, 40, 32, 40);

        TextView name = new TextView(context);
        name.setTextColor(Color.WHITE);
        name.setTextSize(22);
        name.setGravity(Gravity.CENTER);
        layout.addView(name);

        TextView info = new TextView(context);
        info.setTextColor(Color.GRAY);
        info.setTextSize(15);
        info.setGravity(Gravity.CENTER);
        info.setPadding(0, 12, 0, 0);
        layout.addView(info);

        android.widget.Button shareButton =
                new android.widget.Button(context);
        shareButton.setText("Поделиться ссылкой");
        shareButton.setVisibility(View.GONE);

        layout.addView(
                shareButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        android.widget.Button editButton =
                new android.widget.Button(context);
        editButton.setText("Изменить информацию");
        editButton.setVisibility(View.GONE);

        layout.addView(
                editButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        info.setOnLongClickListener(v -> {
            String text = info.getText().toString();

            android.content.ClipboardManager clipboard =
                    (android.content.ClipboardManager)
                            context.getSystemService(Context.CLIPBOARD_SERVICE);

            if (clipboard != null) {
                clipboard.setPrimaryClip(
                        android.content.ClipData.newPlainText(
                                "Lookgram link",
                                text
                        )
                );

                Toast.makeText(
                        context,
                        "Ссылка скопирована",
                        Toast.LENGTH_SHORT
                ).show();
            }

            return true;
        });

        android.widget.Button joinButton =
                new android.widget.Button(context);

        joinButton.setText("Вступить");

        joinButton.setOnClickListener(v -> {
            long telegramId =
                    org.telegram.messenger.AccountInstance
                            .getInstance(
                                    org.telegram.messenger.UserConfig.selectedAccount
                            )
                            .getUserConfig()
                            .clientUserId;

            org.telegram.messenger.lookgram.LookgramCommunityMembers
                    .joinAsync(
                            communityId,
                            telegramId,
                            new org.telegram.messenger.lookgram.LookgramCommunityMembers.Callback() {
                                @Override
                                public void onSuccess(org.json.JSONObject result) {
                                    if (getParentActivity() == null) {
                                        return;
                                    }

                                    getParentActivity().runOnUiThread(() -> {
                                        joinButton.setText("Вы участник");

                                        Toast.makeText(
                                                context,
                                                "Вы вступили",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    });
                                }

                                @Override
                                public void onError(Exception e) {
                                    if (getParentActivity() == null) {
                                        return;
                                    }

                                    getParentActivity().runOnUiThread(() ->
                                            Toast.makeText(
                                                    context,
                                                    "Не удалось вступить",
                                                    Toast.LENGTH_SHORT
                                            ).show()
                                    );
                                }
                            }
                    );
        });

        layout.addView(
                joinButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        android.widget.Button leaveButton =
                new android.widget.Button(context);

        leaveButton.setText("Выйти");
        leaveButton.setVisibility(View.GONE);

        leaveButton.setOnClickListener(v -> {
            long telegramId =
                    org.telegram.messenger.AccountInstance
                            .getInstance(
                                    org.telegram.messenger.UserConfig.selectedAccount
                            )
                            .getUserConfig()
                            .clientUserId;

            org.telegram.messenger.lookgram.LookgramCommunityLeave
                    .leaveAsync(
                            communityId,
                            telegramId,
                            new org.telegram.messenger.lookgram.LookgramCommunityLeave.Callback() {
                                @Override
                                public void onSuccess(org.json.JSONObject result) {
                                    if (getParentActivity() != null) {
                                        getParentActivity().runOnUiThread(() -> {
                                            leaveButton.setVisibility(View.GONE);
                                            joinButton.setText("Вступить");
                                            joinButton.setVisibility(View.VISIBLE);

                                            Toast.makeText(
                                                    context,
                                                    "Вы вышли",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        });
                                    }
                                }

                                @Override
                                public void onError(Exception e) {
                                    if (getParentActivity() != null) {
                                        getParentActivity().runOnUiThread(() ->
                                                Toast.makeText(
                                                        context,
                                                        "Не удалось выйти",
                                                        Toast.LENGTH_SHORT
                                                ).show()
                                        );
                                    }
                                }
                            }
                    );
        });

        layout.addView(
                leaveButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        joinButton.setVisibility(View.GONE);

        long memberCheckTelegramId =
                org.telegram.messenger.AccountInstance
                        .getInstance(
                                org.telegram.messenger.UserConfig.selectedAccount
                        )
                        .getUserConfig()
                        .clientUserId;

        org.telegram.messenger.lookgram.LookgramCommunityMemberList
                .getAsync(
                        communityId,
                        new org.telegram.messenger.lookgram.LookgramCommunityMemberList.Callback() {
                            @Override
                            public void onSuccess(org.json.JSONArray members) {
                                boolean joined = false;

                                for (int i = 0; i < members.length(); i++) {
                                    if (String.valueOf(memberCheckTelegramId)
                                            .equals(members.optString(i))) {
                                        joined = true;
                                        break;
                                    }
                                }

                                boolean finalJoined = joined;

                                if (getParentActivity() != null) {
                                    getParentActivity().runOnUiThread(() -> {
                                        if (finalJoined) {
                                            joinButton.setVisibility(View.GONE);
                                            leaveButton.setVisibility(View.VISIBLE);
                                        } else {
                                            joinButton.setVisibility(View.VISIBLE);
                                            leaveButton.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onError(Exception e) {
                                if (getParentActivity() != null) {
                                    getParentActivity().runOnUiThread(() ->
                                            joinButton.setVisibility(View.VISIBLE)
                                    );
                                }
                            }
                        }
                );

        LinearLayout messagesLayout = new LinearLayout(context);
        messagesLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout messageBar = new LinearLayout(context);
        messageBar.setOrientation(LinearLayout.HORIZONTAL);
        messageBar.setPadding(0, 24, 0, 0);

        android.widget.EditText messageInput =
                new android.widget.EditText(context);
        messageInput.setHint("Сообщение");
        messageInput.setTextColor(Color.WHITE);
        messageInput.setHintTextColor(Color.GRAY);
        messageInput.setSingleLine(true);

        messageBar.addView(
                messageInput,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        android.widget.Button sendButton =
                new android.widget.Button(context);
        sendButton.setText("Отправить");

        messageBar.addView(
                sendButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        layout.addView(messageBar);

        sendButton.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();

            if (text.isEmpty()) {
                return;
            }

            long telegramId =
                    org.telegram.messenger.AccountInstance
                            .getInstance(
                                    org.telegram.messenger.UserConfig.selectedAccount
                            )
                            .getUserConfig()
                            .clientUserId;

            org.telegram.messenger.lookgram.LookgramCommunityMessages
                    .sendAsync(
                            communityId,
                            telegramId,
                            text,
                            new org.telegram.messenger.lookgram.LookgramCommunityMessages.Callback() {
                                @Override
                                public void onSuccess(org.json.JSONArray messages) {
                                    if (getParentActivity() != null) {
                                        getParentActivity().runOnUiThread(() -> {
                                            messageInput.setText("");

                                            LookgramCommunityMessages.getAsync(
                                                    communityId,
                                                    telegramId,
                                                    new LookgramCommunityMessages.Callback() {
                                                        @Override
                                                        public void onSuccess(org.json.JSONArray messages) {
                                                            if (getParentActivity() == null) {
                                                                return;
                                                            }

                                                            getParentActivity().runOnUiThread(() -> {
                                                                messagesLayout.removeAllViews();

                                                                for (int i = 0; i < messages.length(); i++) {
                                                                    org.json.JSONObject message =
                                                                            messages.optJSONObject(i);

                                                                    if (message == null) {
                                                                        continue;
                                                                    }

                                                                    TextView messageView =
                                                                            new TextView(context);

                                                                    messageView.setTextColor(Color.WHITE);
                                                                    messageView.setTextSize(16);
                                                                    messageView.setPadding(12, 10, 12, 10);
                                                                    messageView.setText(
                                                                            message.optString("text", "")
                                                                    );

                                                                    final int pinnedMessageId = message.optInt("id", 0);
                        final boolean isPinned = message.optBoolean("pinned", false);

                        messageView.setOnLongClickListener(v -> {
                            String[] items = isPinned
                                    ? new String[]{"Открепить", "Изменить", "Удалить"}
                                    : new String[]{"Закрепить", "Изменить", "Удалить"};

                            new android.app.AlertDialog.Builder(getParentActivity())
                                    .setItems(items, (dialog, which) -> {
                                        if (isPinned && which == 0) {
                                            unpinCommunityMessage(pinnedMessageId);
                                        } else if (!isPinned && which == 0) {
                                            pinCommunityMessage(pinnedMessageId);
                                        } else if (which == 1) {
                                            editCommunityMessage(pinnedMessageId, messageView);
                                        } else {
                                            deleteCommunityMessage(pinnedMessageId, messageView);
                                        }
                                    })
                                    .show();

                            return true;
                        });

                        messagesLayout.addView(messageView);
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onError(Exception e) {
                                                        }
                                                    }
                                            );

                                            Toast.makeText(
                                                    context,
                                                    "Отправлено",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        });
                                    }
                                }

                                @Override
                                public void onError(Exception e) {
                                    if (getParentActivity() != null) {
                                        getParentActivity().runOnUiThread(() ->
                                                Toast.makeText(
                                                        context,
                                                        "Ошибка отправки",
                                                        Toast.LENGTH_SHORT
                                                ).show()
                                        );
                                    }
                                }
                            }
                    );
        });

        long currentTelegramId =
                org.telegram.messenger.AccountInstance
                        .getInstance(
                                org.telegram.messenger.UserConfig.selectedAccount
                        )
                        .getUserConfig()
                        .clientUserId;

        TextView messagesTitle = new TextView(context);
        messagesTitle.setText("Сообщения");
        messagesTitle.setTextColor(Color.WHITE);
        messagesTitle.setTextSize(18);
        messagesTitle.setPadding(0, 24, 0, 8);
        layout.addView(messagesTitle);

        layout.addView(
                messagesLayout,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        org.telegram.messenger.lookgram.LookgramCommunityMessages
                .getAsync(
                        communityId,
                        currentTelegramId,
                        new org.telegram.messenger.lookgram.LookgramCommunityMessages.Callback() {
                            @Override
                            public void onSuccess(org.json.JSONArray messages) {
                                if (getParentActivity() == null) {
                                    return;
                                }

                                getParentActivity().runOnUiThread(() -> {
                                    messagesLayout.removeAllViews();

                                    for (int i = 0; i < messages.length(); i++) {
                                        org.json.JSONObject message =
                                                messages.optJSONObject(i);

                                        if (message == null) {
                                            continue;
                                        }

                                        TextView messageView =
                                                new TextView(context);

                                        messageView.setTextColor(Color.WHITE);
                                        messageView.setTextSize(16);
                                        messageView.setPadding(
                                                12, 10, 12, 10
                                        );

                                        messageView.setText(
                                                message.optString(
                                                        "text",
                                                        ""
                                                )
                                        );

                                        final int pinnedMessageId = message.optInt("id", 0);
                        final boolean isPinned = message.optBoolean("pinned", false);

                        messageView.setOnLongClickListener(v -> {
                            String[] items = isPinned
                                    ? new String[]{"Открепить", "Изменить", "Удалить"}
                                    : new String[]{"Закрепить", "Изменить", "Удалить"};

                            new android.app.AlertDialog.Builder(getParentActivity())
                                    .setItems(items, (dialog, which) -> {
                                        if (isPinned && which == 0) {
                                            unpinCommunityMessage(pinnedMessageId);
                                        } else if (!isPinned && which == 0) {
                                            pinCommunityMessage(pinnedMessageId);
                                        } else if (which == 1) {
                                            editCommunityMessage(pinnedMessageId, messageView);
                                        } else {
                                            deleteCommunityMessage(pinnedMessageId, messageView);
                                        }
                                    })
                                    .show();

                            return true;
                        });

                        messagesLayout.addView(messageView);
                                    }
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                // Сообщения пока просто не показываем
                            }
                        }
                );

        LookgramCommunityDetails.getAsync(
                communityId,
                new LookgramCommunityDetails.Callback() {
                    @Override
                    public void onSuccess(JSONObject community) {
                        if (getParentActivity() == null) {
                            return;
                        }

                        getParentActivity().runOnUiThread(() -> {
                            String title =
                                    community.optString(
                                            "name",
                                            "Без названия"
                                    );

                            boolean channel =
                                    "channel".equals(
                                            community.optString("type")
                                    );

                            boolean verified =
                                    community.optBoolean(
                                            "verified",
                                            false
                                    );

                            String avatarUrl = community.optString("avatar", "");
                            actionBar.setTitle(title);

                            avatarView.setImageDrawable(null);
                            avatarView.setBackgroundColor(Color.DKGRAY);
                            avatarView.setContentDescription(
                                    channel ? "Аватар канала" : "Аватар группы"
                            );

                            if (!avatarUrl.isEmpty()) {
                                new Thread(() -> {
                                    try {
                                        java.net.URL url =
                                                new java.net.URL(avatarUrl);
                                        java.net.HttpURLConnection connection =
                                                (java.net.HttpURLConnection) url.openConnection();
                                        connection.setConnectTimeout(5000);
                                        connection.setReadTimeout(5000);

                                        android.graphics.Bitmap bitmap =
                                                android.graphics.BitmapFactory.decodeStream(
                                                        connection.getInputStream()
                                                );

                                        connection.disconnect();

                                        if (bitmap != null &&
                                                getParentActivity() != null) {
                                            getParentActivity().runOnUiThread(
                                                    () -> avatarView.setImageBitmap(bitmap)
                                            );
                                        }
                                    } catch (Exception ignored) {
                                    }
                                }).start();
                            }

                            long currentTelegramId =
                                    org.telegram.messenger.UserConfig
                                            .getInstance(
                                                    org.telegram.messenger.UserConfig.selectedAccount
                                            )
                                            .getClientUserId();

                            boolean isOwner =
                                    String.valueOf(currentTelegramId).equals(
                                            community.optString("ownerTelegramId", "")
                                    );

                            if (isOwner) {
                                editButton.setVisibility(View.VISIBLE);
                                editButton.setOnClickListener(v ->
                                        presentFragment(
                                                new LookgramCommunityEditActivity(communityId)
                                        )
                                );
                            } else {
                                editButton.setVisibility(View.GONE);

                                org.telegram.messenger.lookgram.LookgramCommunityAdmins.getAsync(
                                        communityId,
                                        new org.telegram.messenger.lookgram.LookgramCommunityAdmins.Callback() {
                                            @Override
                                            public void onSuccess(org.json.JSONArray admins) {
                                                boolean canChangeInfo = false;

                                                for (int i = 0; i < admins.length(); i++) {
                                                    org.json.JSONObject admin =
                                                            admins.optJSONObject(i);

                                                    if (admin == null) {
                                                        continue;
                                                    }

                                                    if (!String.valueOf(currentTelegramId).equals(
                                                            admin.optString("telegramId", "")
                                                    )) {
                                                        continue;
                                                    }

                                                    org.json.JSONObject rights =
                                                            admin.optJSONObject("rights");

                                                    canChangeInfo =
                                                            rights != null &&
                                                            rights.optBoolean(
                                                                    "changeInfo",
                                                                    false
                                                            );
                                                    break;
                                                }

                                                boolean finalCanChangeInfo = canChangeInfo;

                                                if (getParentActivity() != null) {
                                                    getParentActivity().runOnUiThread(() -> {
                                                        if (finalCanChangeInfo) {
                                                            editButton.setVisibility(View.VISIBLE);
                                                            editButton.setOnClickListener(v ->
                                                                    presentFragment(
                                                                            new LookgramCommunityEditActivity(communityId)
                                                                    )
                                                            );
                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onError(Exception e) {
                                            }
                                        }
                                );
                            }

                            name.setText(
                                    (channel ? "📢 " : "👥 ") +
                                            title +
                                            (verified ? " ✓" : "")
                            );

                            String username =
                                    community.optString("username", "");

                            info.setText(
                                    (channel ? "Канал" : "Группа") +
                                    (username.isEmpty()
                                            ? ""
                                            : "\n\nlookgram://c/" + username)
                            );
                        if (!username.isEmpty()) {
    shareButton.setVisibility(View.VISIBLE);
    shareButton.setOnClickListener(v -> {
        android.content.Intent sendIntent =
                new android.content.Intent(android.content.Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(
                android.content.Intent.EXTRA_TEXT,
                "lookgram://c/" + username
        );

        context.startActivity(
                android.content.Intent.createChooser(
                        sendIntent,
                        "Поделиться ссылкой"
                )
        );
    });

    info.setOnClickListener(v -> {
        android.content.Intent intent = new android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse("lookgram://c/" + username)
        );
        context.startActivity(intent);
    });
}

});
                    }

                    @Override
                    public void onError(Exception e) {
                        if (getParentActivity() != null) {
                            getParentActivity().runOnUiThread(() ->
                                    Toast.makeText(
                                            context,
                                            "Ошибка: " + e.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show()
                            );
                        }
                    }
                }
        );

        return fragmentView = layout;
    }

    private void deleteCommunityMessage(int messageId, TextView messageView) {
        final long telegramId =
                org.telegram.messenger.AccountInstance
                        .getInstance(
                                org.telegram.messenger.UserConfig.selectedAccount
                        )
                        .getUserConfig()
                        .clientUserId;

        new Thread(() -> {
            try {
                URL url = new URL(
                        "http://78.154.103.51:11563/communities/"
                                + communityId
                                + "/messages/delete"
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
                        String.valueOf(telegramId)
                );

                String body =
                        "{\"messageId\":" + messageId + "}";

                connection.getOutputStream().write(
                        body.getBytes("UTF-8")
                );

                int code = connection.getResponseCode();
                connection.disconnect();

                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() -> {
                        if (code >= 200 && code < 300) {
    Toast.makeText(
            getParentActivity(),
            "Сообщение удалено",
            Toast.LENGTH_SHORT
    ).show();
    messagesLayout.removeView(messageView);
} else {
                            Toast.makeText(
                                    getParentActivity(),
                                    "Недостаточно прав",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
                }

            } catch (Exception e) {
                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() ->
                            Toast.makeText(
                                    getParentActivity(),
                                    "Ошибка удаления",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
                }
            }
        }).start();
    }


    private void editCommunityMessage(int messageId, TextView messageView) {
        final android.widget.EditText input = new android.widget.EditText(getParentActivity());
        input.setText(messageView.getText().toString());
        input.setSingleLine(false);

        new android.app.AlertDialog.Builder(getParentActivity())
                .setTitle("Изменить сообщение")
                .setView(input)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String newText = input.getText().toString().trim();

                    if (newText.isEmpty()) {
                        return;
                    }

                    long telegramId =
                            org.telegram.messenger.AccountInstance
                                    .getInstance(
                                            org.telegram.messenger.UserConfig.selectedAccount
                                    )
                                    .getUserConfig()
                                    .clientUserId;

                    org.telegram.messenger.lookgram.LookgramCommunityMessages
                            .editAsync(
                                    communityId,
                                    telegramId,
                                    messageId,
                                    newText,
                                    new org.telegram.messenger.lookgram.LookgramCommunityMessages.Callback() {
                                        @Override
                                        public void onSuccess(org.json.JSONArray result) {
                                            if (getParentActivity() != null) {
                                                getParentActivity().runOnUiThread(() -> {
                                                    messageView.setText(newText);

                                                    Toast.makeText(
                                                            getParentActivity(),
                                                            "Сообщение изменено",
                                                            Toast.LENGTH_SHORT
                                                    ).show();
                                                });
                                            }
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            if (getParentActivity() != null) {
                                                getParentActivity().runOnUiThread(() ->
                                                        Toast.makeText(
                                                                getParentActivity(),
                                                                "Ошибка изменения",
                                                                Toast.LENGTH_SHORT
                                                        ).show()
                                                );
                                            }
                                        }
                                    }
                            );
                })
                .show();
    }


    private void pinCommunityMessage(int messageId) {
        sendCommunityPinRequest(messageId, false);
    }

    private void unpinCommunityMessage(int messageId) {
        sendCommunityPinRequest(messageId, true);
    }

    private void sendCommunityPinRequest(int messageId, boolean unpin) {
        final long telegramId =
                org.telegram.messenger.AccountInstance
                        .getInstance(
                                org.telegram.messenger.UserConfig.selectedAccount
                        )
                        .getUserConfig()
                        .clientUserId;

        new Thread(() -> {
            try {
                URL url = new URL(
                        "http://78.154.103.51:11563/communities/"
                                + communityId
                                + "/messages/"
                                + messageId
                                + "/pin"
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod(
                        unpin ? "DELETE" : "POST"
                );

                connection.setRequestProperty(
                        "X-Telegram-ID",
                        String.valueOf(telegramId)
                );

                int code = connection.getResponseCode();
                connection.disconnect();

                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() -> {
                        if (code >= 200 && code < 300) {
                            Toast.makeText(
                                    getParentActivity(),
                                    unpin
                                            ? "Сообщение откреплено"
                                            : "Сообщение закреплено",
                                    Toast.LENGTH_SHORT
                            ).show();
                        } else {
                            Toast.makeText(
                                    getParentActivity(),
                                    "Недостаточно прав или ошибка",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
                }

            } catch (Exception e) {
                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() ->
                            Toast.makeText(
                                    getParentActivity(),
                                    "Ошибка соединения",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
                }
            }
        }).start();
    }

}
