package org.telegram.ui.lookgram;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.telegram.messenger.lookgram.LookgramCommunitySearch;
import org.telegram.ui.ActionBar.BaseFragment;

public class LookgramCommunitySearchActivity extends BaseFragment {

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

        actionBar.setTitle("Поиск Lookgram");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 40, 32, 40);

        EditText input = new EditText(context);
        input.setHint("@username");
        input.setSingleLine(true);
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);

        layout.addView(
                input,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        Button searchButton = new Button(context);
        searchButton.setText("Найти");

        layout.addView(searchButton);

        TextView result = new TextView(context);
        result.setTextColor(Color.WHITE);
        result.setTextSize(18);
        result.setGravity(Gravity.CENTER);
        result.setPadding(0, 30, 0, 0);

        layout.addView(
                result,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        Button joinButton = new Button(context);
        joinButton.setText("Вступить");
        joinButton.setVisibility(View.GONE);

        layout.addView(
                joinButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        searchButton.setOnClickListener(v -> {
            String username = input.getText().toString().trim();

            if (username.isEmpty()) {
                return;
            }

            searchButton.setEnabled(false);

            LookgramCommunitySearch.searchAsync(
                    username,
                    new LookgramCommunitySearch.Callback() {
                        @Override
                        public void onSuccess(JSONObject community) {
                            if (getParentActivity() == null) {
                                return;
                            }

                            getParentActivity().runOnUiThread(() -> {
                                searchButton.setEnabled(true);

                                String name =
                                        community.optString(
                                                "name",
                                                "Без названия"
                                        );

                                String foundUsername =
                                        community.optString(
                                                "username",
                                                ""
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

                                result.setText(
                                        (channel ? "📢 " : "👥 ") +
                                        name +
                                        (verified ? " ✓" : "") +
                                        "\n@" + foundUsername
                                );

                                joinButton.setVisibility(View.VISIBLE);
                                joinButton.setEnabled(true);

                                long currentTelegramId =
                                        org.telegram.messenger.UserConfig
                                                .getInstance(
                                                        org.telegram.messenger.UserConfig.selectedAccount
                                                ).getClientUserId();

                                org.telegram.messenger.lookgram.LookgramCommunityMemberCheck.checkAsync(
                                        community.optInt("id"),
                                        currentTelegramId,
                                        new org.telegram.messenger.lookgram.LookgramCommunityMemberCheck.Callback() {
                                            @Override
                                            public void onSuccess(boolean member, boolean owner) {
                                                if (getParentActivity() != null) {
                                                    getParentActivity().runOnUiThread(() -> {
                                                        if (owner) {
                                                            joinButton.setText("Владелец");
                                                            joinButton.setEnabled(false);
                                                        } else if (member) {
                                                            joinButton.setText("Вы участник");
                                                            joinButton.setEnabled(false);
                                                        } else {
                                                            joinButton.setText("Вступить");
                                                            joinButton.setEnabled(true);
                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                // Оставляем кнопку "Вступить"
                                            }
                                        }
                                );

                                joinButton.setOnClickListener(view -> {
                                    long telegramId =
                                            org.telegram.messenger.UserConfig
                                                    .getInstance(
                                                            org.telegram.messenger.UserConfig.selectedAccount
                                                    ).getClientUserId();

                                    joinButton.setEnabled(false);

                                    org.telegram.messenger.lookgram.LookgramCommunityMembers.joinAsync(
                                            community.optInt("id"),
                                            telegramId,
                                            new org.telegram.messenger.lookgram.LookgramCommunityMembers.Callback() {
                                                @Override
                                                public void onSuccess(JSONObject joined) {
                                                    if (getParentActivity() != null) {
                                                        getParentActivity().runOnUiThread(() -> {
                                                            joinButton.setText("Вы вступили");
                                                            Toast.makeText(
                                                                    context,
                                                                    "Вы вступили в сообщество",
                                                                    Toast.LENGTH_SHORT
                                                            ).show();
                                                        });
                                                    }
                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    if (getParentActivity() != null) {
                                                        getParentActivity().runOnUiThread(() -> {
                                                            joinButton.setEnabled(true);
                                                            Toast.makeText(
                                                                    context,
                                                                    "Не удалось вступить",
                                                                    Toast.LENGTH_SHORT
                                                            ).show();
                                                        });
                                                    }
                                                }
                                            }
                                    );
                                });

                                result.setOnClickListener(view ->
                                        presentFragment(
                                                new LookgramCommunityActivity(
                                                        community.optInt("id")
                                                )
                                        )
                                );
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            if (getParentActivity() == null) {
                                return;
                            }

                            getParentActivity().runOnUiThread(() -> {
                                searchButton.setEnabled(true);
                                result.setText("Сообщество не найдено");

                                Toast.makeText(
                                        context,
                                        "Ничего не найдено",
                                        Toast.LENGTH_SHORT
                                ).show();
                            });
                        }
                    }
            );
        });

        return layout;
    }
}
