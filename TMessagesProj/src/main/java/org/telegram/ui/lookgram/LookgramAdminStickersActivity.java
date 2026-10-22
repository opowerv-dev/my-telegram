package org.telegram.ui.lookgram;

import android.graphics.Color;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.lookgram.LookgramAdminStickers;
import org.telegram.ui.ActionBar.BaseFragment;

public class LookgramAdminStickersActivity extends BaseFragment {

    private EditText nameField;
    private EditText emojiField;
    private EditText imageField;
    private CheckBox animatedCheck;

    @Override
    public boolean onFragmentCreate() {
        return super.onFragmentCreate();
    }

    @Override
    public View createView(Context context) {
        long telegramId = AccountInstance
                .getInstance(currentAccount)
                .getUserConfig()
                .clientUserId;

        if (telegramId != 8043596187L) {
            Toast.makeText(
                    context,
                    "Доступ запрещён",
                    Toast.LENGTH_SHORT
            ).show();
            return new View(context);
        }

        actionBar.setTitle("Управление стикерами");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        nameField = new EditText(context);
        nameField.setHint("Название стикера");
        layout.addView(nameField);

        emojiField = new EditText(context);
        emojiField.setHint("Emoji, например 🍒");
        layout.addView(emojiField);

        imageField = new EditText(context);
        imageField.setHint("URL картинки");
        layout.addView(imageField);

        animatedCheck = new CheckBox(context);
        animatedCheck.setText("Анимационный стикер");
        layout.addView(animatedCheck);

        Button addButton = new Button(context);
        addButton.setText("Добавить стикер");
        addButton.setTextColor(Color.WHITE);

        addButton.setOnClickListener(v -> addSticker());

        layout.addView(
                addButton,
                new LinearLayout.LayoutParams(
                        -1,
                        56
                )
        );

        return layout;
    }

    private void addSticker() {
        String name = nameField.getText().toString().trim();
        String emoji = emojiField.getText().toString().trim();
        String image = imageField.getText().toString().trim();
        boolean animated = animatedCheck.isChecked();

        if (name.isEmpty() || image.isEmpty()) {
            Toast.makeText(
                    getParentActivity(),
                    "Заполни название и URL картинки",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        long telegramId =
                AccountInstance
                        .getInstance(currentAccount)
                        .getUserConfig()
                        .clientUserId;

        LookgramAdminStickers.addAsync(
                telegramId,
                name,
                emoji,
                image,
                animated,
                new LookgramAdminStickers.Callback() {
                    @Override
                    public void onResult(String result) {
                        if (getParentActivity() != null) {
                            getParentActivity().runOnUiThread(() ->
                                    Toast.makeText(
                                            getParentActivity(),
                                            "Стикер добавлен",
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
                                            "Ошибка: " + e.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show()
                            );
                        }
                    }
                }
        );
    }
}
