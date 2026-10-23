package org.telegram.ui.lookgram;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.lookgram.LookgramProfile;
import org.telegram.messenger.lookgram.LookgramAuthApi;
import org.telegram.messenger.lookgram.LookgramAuthSession;
import org.telegram.ui.ActionBar.BaseFragment;

public class LookgramProfileActivity extends BaseFragment {
    private android.widget.ImageView avatarView;

    private final long profileTelegramUserId;

    public LookgramProfileActivity() {
        this(0);
    }

    public LookgramProfileActivity(long telegramUserId) {
        this.profileTelegramUserId = telegramUserId;
    }

    private TextView usernameView;
    private TextView nameView;
    private TextView bioView;
    private TextView statusView;
    private volatile boolean authenticatedProfileLoaded = false;


    private void refreshAuthenticatedProfile(Context context, long telegramUserId) {
        if (context == null ||
                (profileTelegramUserId != 0 &&
                 profileTelegramUserId != telegramUserId)) {
            return;
        }

        String token = LookgramAuthSession.getToken(context);
        if (token == null || token.isEmpty()) {
            return;
        }

        new Thread(() -> {
            try {
                org.json.JSONObject data = new org.json.JSONObject(
                        LookgramAuthApi.get(
                                "https://lookgram.wisp.uno",
                                "/auth/me",
                                token
                        )
                );

                org.json.JSONObject profile = data.optJSONObject("profile");
                if (profile == null) {
                    profile = new org.json.JSONObject();
                }

                String name = profile.optString("name", "");
                String username = profile.optString("username", "");
                String bio = profile.optString("bio", "");
                String avatar = profile.optString("avatar", "");
                boolean premium = data.optBoolean("premium", false);
                boolean verified = data.optBoolean("verified", false);

                final String finalName = name;
                final String finalUsername = username;
                final String finalBio = bio;
                final String finalAvatar = avatar;
                final boolean finalPremium = premium;
                final boolean finalVerified = verified;

                if (getParentActivity() == null) {
                    return;
                }

                getParentActivity().runOnUiThread(() -> {
                    if (getParentActivity() == null) {
                        return;
                    }

                    authenticatedProfileLoaded = true;

                    if (!finalUsername.isEmpty()) {
                        usernameView.setText("@" + finalUsername);
                    }
                    if (!finalName.isEmpty()) {
                        nameView.setText(finalName);
                    } else if (!finalUsername.isEmpty()) {
                        nameView.setText(finalUsername);
                    }
                    bioView.setText(finalBio);

                    StringBuilder badges = new StringBuilder();
                    if (finalPremium) {
                        badges.append("★ Premium");
                    }
                    if (finalVerified) {
                        if (badges.length() > 0) {
                            badges.append("   ");
                        }
                        badges.append("✓");
                    }

                    if (badges.length() > 0) {
                        statusView.setText(badges.toString());
                        statusView.setVisibility(View.VISIBLE);
                    } else {
                        statusView.setVisibility(View.GONE);
                    }

                    if (!finalAvatar.isEmpty()) {
                        new Thread(() -> {
                            try {
                                String encoded = finalAvatar;
                                int comma = encoded.indexOf(',');
                                if (encoded.startsWith("data:") && comma >= 0) {
                                    encoded = encoded.substring(comma + 1);
                                }

                                byte[] bytes = android.util.Base64.decode(
                                        encoded, android.util.Base64.DEFAULT
                                );
                                Bitmap bitmap = BitmapFactory.decodeByteArray(
                                        bytes, 0, bytes.length
                                );

                                if (bitmap != null && getParentActivity() != null) {
                                    getParentActivity().runOnUiThread(() -> {
                                        if (getParentActivity() != null) {
                                            avatarView.setImageBitmap(bitmap);
                                        }
                                    });
                                }
                            } catch (Exception ignored) {
                            }
                        }).start();
                    }
                });
            } catch (Exception ignored) {
                // При сетевой ошибке оставляем старый профиль.
            }
        }).start();
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        return true;
    }

    private void refreshOnlineStatus() {
        if (profileTelegramUserId == 0 ||
                profileTelegramUserId == 8043596187L) {
            return;
        }

        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(
                        "http://78.154.103.51:11563/users/"
                                + profileTelegramUserId
                                + "/status"
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

                boolean online =
                        json.optBoolean("online", false);

                long lastSeen =
                        json.optLong("lastSeen", 0);

                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() -> {
                        if (online) {
                            statusView.setText("● онлайн");
                        } else if (lastSeen > 0) {
                            java.text.SimpleDateFormat format =
                                    new java.text.SimpleDateFormat(
                                            "dd.MM.yyyy HH:mm",
                                            java.util.Locale.getDefault()
                                    );

                            statusView.setText(
                                    "был(а) " +
                                    format.format(
                                            new java.util.Date(lastSeen)
                                    )
                            );
                        }

                        statusView.setVisibility(
                                android.view.View.VISIBLE
                        );
                    });
                }

                connection.disconnect();

            } catch (Exception ignored) {
            }
        }).start();
    }

    public android.view.View createView(Context context) {
        actionBar.setTitle("Lookgram Profile");
        actionBar.setBackButtonImage(
                org.telegram.messenger.R.drawable.ic_ab_back
        );

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        avatarView = new android.widget.ImageView(context);
        avatarView.setLayoutParams(
                new LinearLayout.LayoutParams(180, 180)
        );
        avatarView.setScaleType(
                android.widget.ImageView.ScaleType.CENTER_CROP
        );
        avatarView.setBackgroundColor(Color.DKGRAY);
        layout.addView(avatarView);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(40, 40, 40, 40);

        usernameView = new TextView(context);
        nameView = new TextView(context);
        bioView = new TextView(context);
        statusView = new TextView(context);

        usernameView.setTextSize(24);
        nameView.setTextSize(20);
        bioView.setTextSize(16);
        statusView.setTextSize(15);
        statusView.setTextColor(Color.LTGRAY);
        statusView.setVisibility(View.GONE);

        statusView.setOnClickListener(v ->
                android.widget.Toast.makeText(
                        context,
                        "Официальная галочка Lookgram",
                        android.widget.Toast.LENGTH_SHORT
                ).show()
        );

        usernameView.setTextColor(Color.WHITE);
        nameView.setTextColor(Color.WHITE);
        bioView.setTextColor(Color.LTGRAY);

        usernameView.setText("@...");
        nameView.setText("Загрузка...");
        bioView.setText("");

        layout.addView(usernameView);
        layout.addView(statusView);
        layout.addView(nameView);
        layout.addView(bioView);
        android.widget.Button edit = new android.widget.Button(context);
        edit.setText("Редактировать профиль");
        layout.addView(edit);

        long currentUserId = AccountInstance
                .getInstance(currentAccount)
                .getUserConfig()
                .clientUserId;

        edit.setVisibility(
                profileTelegramUserId == 0 || profileTelegramUserId == currentUserId
                        ? android.view.View.VISIBLE
                        : android.view.View.GONE
        );

        edit.setOnClickListener(v ->
                presentFragment(new LookgramEditProfileActivity())
        );

        android.widget.Button openTelegramProfileButton =
                new android.widget.Button(context);
        openTelegramProfileButton.setText("Открыть профиль Telegram");
        layout.addView(openTelegramProfileButton);
        openTelegramProfileButton.setOnClickListener(v -> {
            long targetUserId = profileTelegramUserId != 0
                    ? profileTelegramUserId : currentUserId;
            if (targetUserId == 0) {
                android.widget.Toast.makeText(
                        context, "Не удалось определить пользователя",
                        android.widget.Toast.LENGTH_SHORT
                ).show();
                return;
            }
            android.os.Bundle profileArgs = new android.os.Bundle();
            profileArgs.putLong("user_id", targetUserId);
            presentFragment(new org.telegram.ui.ProfileActivity(profileArgs));
        });

        android.widget.Button friendButton =
                new android.widget.Button(context);
        friendButton.setText("Добавить в контакты");
        layout.addView(friendButton);

        android.widget.Button blockButton =
                new android.widget.Button(context);
        blockButton.setText("Заблокировать");
        layout.addView(blockButton);

        boolean isForeignProfile =
                profileTelegramUserId != 0 &&
                profileTelegramUserId != currentUserId;

        friendButton.setVisibility(
                isForeignProfile
                        ? android.view.View.VISIBLE
                        : android.view.View.GONE
        );

        blockButton.setVisibility(
                isForeignProfile
                        ? android.view.View.VISIBLE
                        : android.view.View.GONE
        );

        android.widget.Button messageButton =
                new android.widget.Button(context);
        messageButton.setText("Написать");
        layout.addView(messageButton);

        messageButton.setVisibility(
                isForeignProfile
                        ? android.view.View.VISIBLE
                        : android.view.View.GONE
        );

        messageButton.setOnClickListener(v -> {
            new Thread(() -> {
                try {
                    long myId = AccountInstance
                            .getInstance(currentAccount)
                            .getUserConfig()
                            .clientUserId;

                    java.net.URL url = new java.net.URL(
                            "http://78.154.103.51:11563/chats/private"
                    );

                    java.net.HttpURLConnection connection =
                            (java.net.HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("POST");
                    connection.setRequestProperty(
                            "Content-Type",
                            "application/json"
                    );
                    connection.setDoOutput(true);

                    org.json.JSONObject body = new org.json.JSONObject();
                    body.put("user1", myId);
                    body.put("user2", profileTelegramUserId);

                    connection.getOutputStream().write(
                            body.toString().getBytes("UTF-8")
                    );

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

                    org.json.JSONObject result =
                            new org.json.JSONObject(response.toString());

                    long chatId = result.optLong("id", 0);

                    if (chatId != 0 && getParentActivity() != null) {
                        getParentActivity().runOnUiThread(() ->
                                presentFragment(
                                        new LookgramChatActivity(chatId)
                                )
                        );
                    }
                } catch (Exception ignored) {
                }
            }).start();
        });

        friendButton.setOnClickListener(v -> {
            final long myId = AccountInstance
                    .getInstance(currentAccount)
                    .getUserConfig()
                    .clientUserId;

            final boolean remove = friendButton.getText().toString()
                    .equals("Удалить из контактов");

            new Thread(() -> {
                try {
                    String path = remove ? "/friends/remove" : "/friends/add";
                    String method = remove ? "DELETE" : "POST";

                    java.net.URL url = new java.net.URL(
                            "http://78.154.103.51:11563" + path
                    );
                    java.net.HttpURLConnection connection =
                            (java.net.HttpURLConnection) url.openConnection();

                    connection.setRequestMethod(method);
                    connection.setRequestProperty(
                            "Content-Type",
                            "application/json"
                    );
                    connection.setDoOutput(true);

                    org.json.JSONObject body = new org.json.JSONObject();
                    body.put("userId", myId);
                    body.put("friendId", profileTelegramUserId);

                    connection.getOutputStream().write(
                            body.toString().getBytes("UTF-8")
                    );

                    connection.getResponseCode();
                    connection.disconnect();

                    if (getParentActivity() != null) {
                        getParentActivity().runOnUiThread(() -> {
                            friendButton.setText(
                                    remove
                                            ? "Добавить в контакты"
                                            : "Удалить из контактов"
                            );
                            android.widget.Toast.makeText(
                                    getParentActivity(),
                                    remove
                                            ? "Удалено из контактов"
                                            : "Добавлено в контакты",
                                    android.widget.Toast.LENGTH_SHORT
                            ).show();
                        });
                    }
                } catch (Exception ignored) {
                }
            }).start();
        });

        blockButton.setOnClickListener(v -> {
            final long myId = AccountInstance
                    .getInstance(currentAccount)
                    .getUserConfig()
                    .clientUserId;

            final boolean unblock = blockButton.getText().toString()
                    .equals("Разблокировать");

            new Thread(() -> {
                try {
                    String path = unblock ? "/blocks/remove" : "/blocks/add";

                    java.net.URL url = new java.net.URL(
                            "http://78.154.103.51:11563" + path
                    );
                    java.net.HttpURLConnection connection =
                            (java.net.HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("POST");
                    connection.setRequestProperty(
                            "Content-Type",
                            "application/json"
                    );
                    connection.setDoOutput(true);

                    org.json.JSONObject body = new org.json.JSONObject();
                    body.put("userId", myId);
                    body.put("blockedId", profileTelegramUserId);

                    connection.getOutputStream().write(
                            body.toString().getBytes("UTF-8")
                    );

                    connection.getResponseCode();
                    connection.disconnect();

                    if (getParentActivity() != null) {
                        getParentActivity().runOnUiThread(() -> {
                            blockButton.setText(
                                    unblock
                                            ? "Заблокировать"
                                            : "Разблокировать"
                            );
                            android.widget.Toast.makeText(
                                    getParentActivity(),
                                    unblock
                                            ? "Пользователь разблокирован"
                                            : "Пользователь заблокирован",
                                    android.widget.Toast.LENGTH_SHORT
                            ).show();
                        });
                    }
                } catch (Exception ignored) {
                }
            }).start();
        });

        final long myIdForState = AccountInstance
                .getInstance(currentAccount)
                .getUserConfig()
                .clientUserId;

        new Thread(() -> {
            try {
                java.net.URL friendsUrl = new java.net.URL(
                        "http://78.154.103.51:11563/friends/" + myIdForState
                );
                java.net.HttpURLConnection friendsConnection =
                        (java.net.HttpURLConnection) friendsUrl.openConnection();
                friendsConnection.setRequestMethod("GET");

                java.io.BufferedReader friendsReader =
                        new java.io.BufferedReader(
                                new java.io.InputStreamReader(
                                        friendsConnection.getInputStream()
                                )
                        );

                StringBuilder friendsResponse = new StringBuilder();
                String line;
                while ((line = friendsReader.readLine()) != null) {
                    friendsResponse.append(line);
                }
                friendsReader.close();
                friendsConnection.disconnect();

                org.json.JSONArray friends =
                        new org.json.JSONArray(friendsResponse.toString());

                boolean isFriend = false;
                for (int i = 0; i < friends.length(); i++) {
                    org.json.JSONObject item = friends.optJSONObject(i);
                    if (item != null &&
                            item.optLong("id", 0) == profileTelegramUserId) {
                        isFriend = true;
                        break;
                    }
                }

                java.net.URL blocksUrl = new java.net.URL(
                        "http://78.154.103.51:11563/blocks/" + myIdForState
                );
                java.net.HttpURLConnection blocksConnection =
                        (java.net.HttpURLConnection) blocksUrl.openConnection();
                blocksConnection.setRequestMethod("GET");

                java.io.BufferedReader blocksReader =
                        new java.io.BufferedReader(
                                new java.io.InputStreamReader(
                                        blocksConnection.getInputStream()
                                )
                        );

                StringBuilder blocksResponse = new StringBuilder();
                while ((line = blocksReader.readLine()) != null) {
                    blocksResponse.append(line);
                }
                blocksReader.close();
                blocksConnection.disconnect();

                org.json.JSONArray blocked =
                        new org.json.JSONArray(blocksResponse.toString());

                boolean isBlocked = false;
                for (int i = 0; i < blocked.length(); i++) {
                    if (blocked.optLong(i, 0) == profileTelegramUserId) {
                        isBlocked = true;
                        break;
                    }
                }

                final boolean finalIsFriend = isFriend;
                final boolean finalIsBlocked = isBlocked;

                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() -> {
                        friendButton.setText(
                                finalIsFriend
                                        ? "Удалить из контактов"
                                        : "Добавить в контакты"
                        );
                        blockButton.setText(
                                finalIsBlocked
                                        ? "Разблокировать"
                                        : "Заблокировать"
                        );
                    });
                }
            } catch (Exception ignored) {
            }
        }).start();

        long telegramUserId = profileTelegramUserId != 0
                ? profileTelegramUserId
                : AccountInstance
                        .getInstance(currentAccount)
                        .getUserConfig()
                        .clientUserId;

        LookgramProfile.Callback profileCallback = new LookgramProfile.Callback() {
            @Override
            public void onResult(LookgramProfile profile) {
                if (authenticatedProfileLoaded || getParentActivity() == null) {
                    return;
                }

                getParentActivity().runOnUiThread(() -> {
                    if (profile.avatar != null && !profile.avatar.isEmpty()) {
                        new Thread(() -> {
                            try {
                                byte[] bytes = android.util.Base64.decode(profile.avatar, android.util.Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                if (bitmap != null && getParentActivity() != null) {
                                    getParentActivity().runOnUiThread(() -> avatarView.setImageBitmap(bitmap));
                                }
                            } catch (Exception ignored) {
                            }
                        }).start();
                    }
                    if (profile.frozen) {
                        usernameView.setText("❄ @" + profile.username);
                    } else if (telegramUserId == 8043596187L) {
                        usernameView.setText("★ @" + profile.username);
                    } else {
                        usernameView.setText("@" + profile.username);
                    }

                    StringBuilder status = new StringBuilder();

                    if (telegramUserId != 8043596187L && profile.premium) {
                        status.append("★ Premium");
                    }

                    if (telegramUserId != 8043596187L && profile.verified) {
                        if (status.length() > 0) {
                            status.append("   ");
                        }
                        status.append("✓");
                    }

                    if (telegramUserId != 8043596187L) {
                        new Thread(() -> {
                            try {
                                java.net.URL url = new java.net.URL(
                                        "http://78.154.103.51:11563/users/"
                                                + telegramUserId
                                                + "/status"
                                );

                                java.net.HttpURLConnection connection =
                                        (java.net.HttpURLConnection)
                                                url.openConnection();

                                connection.setRequestMethod("GET");

                                java.util.Scanner scanner =
                                        new java.util.Scanner(
                                                connection.getInputStream()
                                        ).useDelimiter("\\A");

                                String response =
                                        scanner.hasNext()
                                                ? scanner.next()
                                                : "";

                                org.json.JSONObject json =
                                        new org.json.JSONObject(response);

                                boolean online =
                                        json.optBoolean("online", false);

                                long lastSeen =
                                        json.optLong("lastSeen", 0);

                                if (getParentActivity() != null) {
                                    getParentActivity().runOnUiThread(() -> {
                                        StringBuilder text =
                                                new StringBuilder();

                                        if (status.length() > 0) {
                                            text.append(status);
                                        }

                                        if (text.length() > 0) {
                                            text.append("   ");
                                        }

                                        if (online) {
                                            text.append("● онлайн");
                                        } else if (lastSeen > 0) {
                                            java.text.SimpleDateFormat format =
                                                    new java.text.SimpleDateFormat(
                                                            "dd.MM.yyyy HH:mm",
                                                            java.util.Locale.getDefault()
                                                    );

                                            text.append("был(а) ")
                                                    .append(
                                                            format.format(
                                                                    new java.util.Date(
                                                                            lastSeen
                                                                    )
                                                            )
                                                    );
                                        }

                                        if (text.length() > 0) {
                                            statusView.setText(
                                                    text.toString()
                                            );
                                            statusView.setVisibility(
                                                    android.view.View.VISIBLE
                                            );
                                        } else {
                                            statusView.setVisibility(
                                                    android.view.View.GONE
                                            );
                                        }
                                    });
                                }

                            } catch (Exception ignored) {
                            }
                        }).start();

                    } else if (status.length() > 0) {
                        statusView.setText(status.toString());
                        statusView.setVisibility(
                                android.view.View.VISIBLE
                        );
                    } else {
                        statusView.setVisibility(
                                android.view.View.GONE
                        );
                    }

                    nameView.setText(profile.name);
                    bioView.setText(profile.bio);
                });
            }

            @Override
            public void onError(Exception e) {
                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() ->
                            nameView.setText("Не удалось загрузить профиль")
                    );
                }
            }
        };

        if (profileTelegramUserId != 0) {
            LookgramProfile.getAsync(telegramUserId, profileCallback);
        } else {
            LookgramProfile.getOrCreateAsync(telegramUserId, profileCallback);
        }
        refreshAuthenticatedProfile(getParentActivity(), telegramUserId);

        if (profileTelegramUserId != 0 &&
                profileTelegramUserId != 8043596187L) {
            refreshOnlineStatus();

            android.os.Handler statusHandler =
                    new android.os.Handler(
                            android.os.Looper.getMainLooper()
                    );

            statusHandler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (getParentActivity() != null) {
                                refreshOnlineStatus();

                                statusHandler.postDelayed(
                                        this,
                                        30000
                                );
                            }
                        }
                    },
                    30000
            );
        }

        return fragmentView = layout;
    }
}