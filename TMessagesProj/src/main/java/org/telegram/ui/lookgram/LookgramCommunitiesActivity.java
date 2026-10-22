package org.telegram.ui.lookgram;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.lookgram.LookgramCommunities;
import org.telegram.ui.ActionBar.BaseFragment;

public class LookgramCommunitiesActivity extends BaseFragment {

    private LinearLayout list;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setTitle("Каналы и чаты");
        actionBar.setBackButtonImage(
                org.telegram.messenger.R.drawable.ic_ab_back
        );

        list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);

        long telegramId =
                AccountInstance
                        .getInstance(currentAccount)
                        .getUserConfig()
                        .clientUserId;

        LookgramCommunities.getMyAsync(
                telegramId,
                new LookgramCommunities.Callback() {
                    @Override
                    public void onSuccess(JSONArray communities) {
                        if (getParentActivity() == null) {
                            return;
                        }

                        getParentActivity().runOnUiThread(() -> {
                            list.removeAllViews();

                            for (int i = 0; i < communities.length(); i++) {
                                JSONObject community =
                                        communities.optJSONObject(i);

                                if (community == null) {
                                    continue;
                                }

                                addCommunity(context, community);
                            }

                            if (communities.length() == 0) {
                                TextView empty =
                                        new TextView(context);

                                empty.setText("У вас пока нет каналов или чатов");
                                empty.setTextColor(Color.GRAY);
                                empty.setTextSize(16);
                                empty.setPadding(40, 60, 40, 60);

                                list.addView(empty);
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

        return fragmentView = list;
    }

    private void addCommunity(
            Context context,
            JSONObject community
    ) {
        TextView item = new TextView(context);

        int communityId =
                community.optInt("id", 0);

        String name =
                community.optString("name", "Без названия");

        String type =
                community.optString("type", "chat");

        boolean verified =
                community.optBoolean("verified", false);

        String icon =
                type.equals("channel") ? "📢" : "👥";

        String check =
                verified ? " ✓" : "";

        item.setText(
                icon + "  " + name + check
        );

        item.setTextColor(Color.WHITE);
        item.setTextSize(17);
        item.setPadding(32, 28, 32, 28);

        item.setOnClickListener(v -> {
            presentFragment(
                    new LookgramCommunityActivity(communityId)
            );
        });

        list.addView(item);
    }

}
