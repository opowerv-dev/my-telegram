package org.telegram.ui.lookgram;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONObject;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.lookgram.LookgramCommunityDetails;
import org.telegram.messenger.lookgram.LookgramCommunityInfo;

public class LookgramCommunityEditActivity extends BaseFragment {

    private final int communityId;

    public LookgramCommunityEditActivity(int communityId) {
        super();
        this.communityId = communityId;
    }

    @Override
    public View createView(Context context) {
        actionBar.setTitle("Изменить информацию");
        actionBar.setBackButtonImage(
                org.telegram.messenger.R.drawable.ic_ab_back
        );

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        EditText nameInput = new EditText(context);
        nameInput.setHint("Название");

        EditText usernameInput = new EditText(context);
        usernameInput.setHint("Username");

        EditText avatarInput = new EditText(context);
        avatarInput.setHint("Аватар (URL)");
        layout.addView(avatarInput);

        EditText descriptionInput = new EditText(context);
        descriptionInput.setHint("Описание");
        descriptionInput.setMinLines(3);
        descriptionInput.setGravity(Gravity.TOP);

        Button saveButton = new Button(context);
        saveButton.setText("Сохранить");

        layout.addView(nameInput);
        layout.addView(usernameInput);
        layout.addView(descriptionInput);
        layout.addView(saveButton);

        setFragmentView(layout);

        LookgramCommunityDetails.getAsync(
                communityId,
                new LookgramCommunityDetails.Callback() {
                    @Override
                    public void onSuccess(JSONObject community) {
                        if (getParentActivity() == null) {
                            return;
                        }

                        getParentActivity().runOnUiThread(() -> {
                            nameInput.setText(
                                    community.optString("name", "")
                            );
                            usernameInput.setText(
                                    community.optString("username", "")
                            );
                            avatarInput.setText(
                                    community.optString("avatar", "")
                            );
                            descriptionInput.setText(
                                    community.optString(
                                            "description",
                                            ""
                                    )
                            );
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                }
        );

        saveButton.setOnClickListener(v -> {
            long telegramId =
                    UserConfig.getInstance(
                            UserConfig.selectedAccount
                    ).getClientUserId();

            LookgramCommunityInfo.updateAsync(
                    communityId,
                    telegramId,
                    nameInput.getText().toString(),
                    usernameInput.getText().toString(),
                    descriptionInput.getText().toString(),
                    avatarInput.getText().toString(),
                    new LookgramCommunityInfo.Callback() {
                        @Override
                        public void onSuccess(JSONObject community) {
                            if (getParentActivity() == null) {
                                return;
                            }

                            getParentActivity().runOnUiThread(() -> {
                                Toast.makeText(
                                        context,
                                        "Сохранено",
                                        Toast.LENGTH_SHORT
                                ).show();
                                finishFragment();
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
                                            "Ошибка сохранения",
                                            Toast.LENGTH_SHORT
                                    ).show()
                            );
                        }
                    }
            );
        });

        return layout;
    }
}
