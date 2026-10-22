package org.telegram.ui.lookgram;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import android.util.Base64;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.lookgram.LookgramProfile;
import org.telegram.ui.ActionBar.BaseFragment;

public class LookgramEditProfileActivity extends BaseFragment {
    private EditText username;
    private EditText name;
    private EditText bio;
    private Uri avatarUri;
    private boolean avatarCleared;
    private android.widget.ImageView avatarPreview;
    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001 && data != null && data.getData() != null) {
            avatarUri = data.getData();
            avatarCleared = false;
            try {
                InputStream previewInput =
                        getParentActivity().getContentResolver().openInputStream(avatarUri);
                android.graphics.Bitmap bitmap =
                        android.graphics.BitmapFactory.decodeStream(previewInput);
                if (previewInput != null) {
                    previewInput.close();
                }
                if (bitmap != null) {
                    avatarPreview.setImageBitmap(bitmap);
                }
            } catch (Exception ignored) {
            }
        }
    }
    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        return true;
    }
    private String getAvatarBase64() {
        if (avatarUri == null || getParentActivity() == null) {
            return null;
        }
        try {
            InputStream input = getParentActivity().getContentResolver().openInputStream(avatarUri);
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(input);
            input.close();

            if (bitmap == null) {
                return null;
            }

            int maxSize = 512;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            if (width > maxSize || height > maxSize) {
                float scale = Math.min(
                        (float) maxSize / width,
                        (float) maxSize / height
                );
                bitmap = android.graphics.Bitmap.createScaledBitmap(
                        bitmap,
                        Math.round(width * scale),
                        Math.round(height * scale),
                        true
                );
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            bitmap.compress(
                    android.graphics.Bitmap.CompressFormat.JPEG,
                    80,
                    output
            );

            bitmap.recycle();

            return Base64.encodeToString(
                    output.toByteArray(),
                    Base64.NO_WRAP
            );
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public android.view.View createView(Context context) {
        actionBar.setTitle("Редактировать профиль");
        actionBar.setBackButtonImage(
                org.telegram.messenger.R.drawable.ic_ab_back
        );
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);
        username = new EditText(context);
        name = new EditText(context);
        bio = new EditText(context);
        username.setHint("@username");
        name.setHint("Имя");
        bio.setHint("О себе");
        bio.setMinLines(3);
        username.setTextColor(Color.WHITE);
        name.setTextColor(Color.WHITE);
        bio.setTextColor(Color.WHITE);
        layout.addView(username);
        layout.addView(name);
        layout.addView(bio);
        avatarPreview = new android.widget.ImageView(context);
        avatarPreview.setLayoutParams(new LinearLayout.LayoutParams(180, 180));
        avatarPreview.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        layout.addView(avatarPreview);

        Button avatarButton = new Button(context);
        avatarButton.setText("Выбрать аватар");
        layout.addView(avatarButton);
        avatarButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1001);
        });
        Button clearAvatar = new Button(context);
        clearAvatar.setText("Удалить аватар");
        layout.addView(clearAvatar);
        clearAvatar.setOnClickListener(v -> {
            avatarUri = null;
            avatarCleared = true;
            avatarPreview.setImageDrawable(null);
        });

        Button save = new Button(context);
        save.setText("Сохранить");
        layout.addView(save);

        long telegramUserId = AccountInstance
                .getInstance(currentAccount)
                .getUserConfig()
                .clientUserId;

        LookgramProfile.getOrCreateAsync(telegramUserId, profile -> {
            if (getParentActivity() == null) return;
            getParentActivity().runOnUiThread(() -> {
                username.setText(profile.username);
                name.setText(profile.name);
                bio.setText(profile.bio);

                if (profile.avatar != null && !profile.avatar.isEmpty()) {
                    try {
                        byte[] bytes = Base64.decode(profile.avatar, Base64.DEFAULT);
                        android.graphics.Bitmap bitmap =
                                android.graphics.BitmapFactory.decodeByteArray(
                                        bytes, 0, bytes.length
                                );
                        if (bitmap != null) {
                            avatarPreview.setImageBitmap(bitmap);
                        }
                    } catch (Exception ignored) {
                    }
                }

            });
        });
        save.setOnClickListener(v -> {
            LookgramProfile.getOrCreateAsync(telegramUserId, profile -> {
                String avatarBase64 = getAvatarBase64();
                LookgramProfile.updateAsync(
                        telegramUserId,
                        profile.id,
                        username.getText().toString(),
                        name.getText().toString(),
                        bio.getText().toString(),
                        avatarCleared ? "" : (avatarBase64 != null ? avatarBase64 : profile.avatar),
                        new LookgramProfile.Callback() {
                            @Override
                            public void onResult(LookgramProfile updated) {
                                if (getParentActivity() != null) {
                                    getParentActivity().runOnUiThread(() ->
                                            Toast.makeText(
                                                    getParentActivity(),
                                                    "Профиль сохранён",
                                                    Toast.LENGTH_SHORT
                                            ).show()
                                    );
                                }
                            }
                            @Override
                            public void onError(Exception e) {
                                if (getParentActivity() != null) {
                                    getParentActivity().runOnUiThread(() ->
                                            Toast.makeText(
                                                    getParentActivity(),
                                                    "Ошибка сохранения",
                                                    Toast.LENGTH_SHORT
                                            ).show()
                                    );
                                }
                            }
                        }
                );
            });
        });
        return fragmentView = layout;
    }
}
