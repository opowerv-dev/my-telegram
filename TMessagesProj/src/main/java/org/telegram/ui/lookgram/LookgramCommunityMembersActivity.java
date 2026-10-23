package org.telegram.ui.lookgram;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.telegram.messenger.lookgram.LookgramCommunityMemberList;
import org.telegram.messenger.lookgram.LookgramProfile;
import org.telegram.messenger.lookgram.LookgramCommunityModeration;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.BaseFragment;

public class LookgramCommunityMembersActivity extends BaseFragment {

    private final int communityId;
    private final long currentUserId = UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId();

    public LookgramCommunityMembersActivity(int communityId) {
        this.communityId = communityId;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(org.telegram.messenger.R.drawable.ic_ab_back);
        actionBar.setTitle("Участники");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        TextView loading = new TextView(context);
        loading.setText("Загрузка участников...");
        loading.setTextSize(16);
        loading.setTextColor(Color.WHITE);
        loading.setGravity(Gravity.CENTER_VERTICAL);

        layout.addView(
                loading,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        LookgramCommunityMemberList.getAsync(
                communityId,
                new LookgramCommunityMemberList.Callback() {
                    @Override
                    public void onSuccess(JSONArray members) {
                        if (getParentActivity() != null) {
                            getParentActivity().runOnUiThread(() -> {
                                layout.removeAllViews();

                                for (int i = 0; i < members.length(); i++) {
                                    String telegramId =
                                            members.optString(i, "");

                                    TextView member = new TextView(context);
                                    member.setText("👤 " + telegramId);
                                    member.setTextSize(17);
                                    member.setTextColor(Color.WHITE);
                                    member.setPadding(16, 20, 16, 20);

                                    try {
                                        long userId = Long.parseLong(telegramId);
                                        LookgramProfile.getAsync(
                                                userId,
                                                new LookgramProfile.Callback() {
                                                    @Override
                                                    public void onResult(LookgramProfile profile) {
                                                        if (getParentActivity() != null) {
                                                            getParentActivity().runOnUiThread(() -> {
                                                                String displayName =
                                                                        profile.name == null || profile.name.isEmpty()
                                                                                ? "Без имени"
                                                                                : profile.name;
                                                                String username =
                                                                        profile.username == null || profile.username.isEmpty()
                                                                                ? ""
                                                                                : "  @" + profile.username;
                                                                member.setText("👤 " + displayName + username);
                                                            });
                                                        }
                                                    }

                                                    @Override
                                                    public void onError(Exception e) {
                                                    }
                                                }
                                        );
                                    } catch (Exception ignored) {
                                    }

                                    member.setOnClickListener(v -> {
                                        try {
                                            long userId = Long.parseLong(telegramId);
                                            presentFragment(
                                                    new LookgramProfileActivity(userId)
                                            );
                                        } catch (Exception ignored) {
                                        }
                                    });

                                    member.setOnLongClickListener(v -> {
                                        try {
                                            long userId = Long.parseLong(telegramId);

                                            if (userId == currentUserId) {
                                                return true;
                                            }

                                            new Thread(() -> {
                                                try {
                                                    java.net.URL url = new java.net.URL(
                                                            "http://78.154.103.51:11563/communities/" + communityId
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

                                                    StringBuilder result = new StringBuilder();
                                                    String line;

                                                    while ((line = reader.readLine()) != null) {
                                                        result.append(line);
                                                    }

                                                    reader.close();
                                                    connection.disconnect();

                                                    org.json.JSONObject community =
                                                            new org.json.JSONObject(result.toString());

                                                    boolean canModerate =
                                                            String.valueOf(currentUserId).equals(
                                                                    community.optString("ownerTelegramId")
                                                            );

                                                    org.json.JSONArray admins =
                                                            community.optJSONArray("admins");

                                                    if (!canModerate && admins != null) {
                                                        for (int adminIndex = 0; adminIndex < admins.length(); adminIndex++) {
                                                            org.json.JSONObject admin =
                                                                    admins.optJSONObject(adminIndex);

                                                            if (admin == null) {
                                                                continue;
                                                            }

                                                            if (!String.valueOf(currentUserId).equals(
                                                                    admin.optString("telegramId")
                                                            )) {
                                                                continue;
                                                            }

                                                            org.json.JSONObject rights =
                                                                    admin.optJSONObject("rights");

                                                            canModerate =
                                                                    rights != null &&
                                                                    rights.optBoolean("banUsers", false);

                                                            break;
                                                        }
                                                    }

                                                    if (!canModerate) {
                                                        return;
                                                    }

                                                    if (getParentActivity() != null) {
                                                        getParentActivity().runOnUiThread(() -> {
                                                            new android.app.AlertDialog.Builder(context)
                                                                    .setTitle("Участник")
                                                                    .setItems(
                                                                            new String[]{
                                                                                    "Замутить",
                                                                                    "Размутить",
                                                                                    "Забанить"
                                                                            },
                                                                            (d, which) -> {
                                                                                if (which == 0) {
                                                                                    LookgramCommunityModeration.mute(
                                                                                            communityId,
                                                                                            currentUserId,
                                                                                            userId,
                                                                                            (success, error) -> {}
                                                                                    );
                                                                                } else if (which == 1) {
                                                                                    LookgramCommunityModeration.unmute(
                                                                                            communityId,
                                                                                            currentUserId,
                                                                                            userId,
                                                                                            (success, error) -> {}
                                                                                    );
                                                                                } else {
                                                                                    LookgramCommunityModeration.ban(
                                                                                            communityId,
                                                                                            currentUserId,
                                                                                            userId,
                                                                                            (success, error) -> {}
                                                                                    );
                                                                                }
                                                                            }
                                                                    )
                                                                    .show();
                                                        });
                                                    }

                                                } catch (Exception ignored) {
                                                }
                                            }).start();

                                        } catch (Exception ignored) {
                                        }

                                        return true;
                                    });

                                    layout.addView(
                                            member,
                                            new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                            )
                                    );
                                }

                                if (members.length() == 0) {
                                    TextView empty = new TextView(context);
                                    empty.setText("Участников нет");
                                    empty.setTextColor(Color.WHITE);
                                    empty.setTextSize(16);
                                    layout.addView(empty);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        if (getParentActivity() != null) {
                            getParentActivity().runOnUiThread(() ->
                                    loading.setText("Не удалось загрузить участников")
                            );
                        }
                    }
                }
        );

        fragmentView = layout;
        return layout;
    }
}
