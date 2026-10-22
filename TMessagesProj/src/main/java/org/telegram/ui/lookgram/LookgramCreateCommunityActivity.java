package org.telegram.ui.lookgram;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.lookgram.LookgramCommunity;
import org.telegram.ui.ActionBar.BaseFragment;

import org.json.JSONObject;

public class LookgramCreateCommunityActivity extends BaseFragment {

    private EditText nameInput;
    private Spinner typeSpinner;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setTitle("Создать");
        actionBar.setBackButtonImage(
                org.telegram.messenger.R.drawable.ic_ab_back
        );

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        typeSpinner = new Spinner(context);

        String[] types = {
                "Канал",
                "Чат"
        };

        typeSpinner.setAdapter(
                new ArrayAdapter<>(
                        context,
                        android.R.layout.simple_spinner_dropdown_item,
                        types
                )
        );

        layout.addView(typeSpinner);

        nameInput = new EditText(context);
        nameInput.setHint("Название");
        nameInput.setTextColor(Color.WHITE);
        nameInput.setHintTextColor(Color.GRAY);
        layout.addView(nameInput);

        Button create = new Button(context);
        create.setText("Создать");
        layout.addView(create);

        create.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(
                        context,
                        "Введите название",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            int position =
                    typeSpinner.getSelectedItemPosition();

            String type =
                    position == 0 ? "channel" : "chat";

            long telegramId =
                    AccountInstance
                            .getInstance(currentAccount)
                            .getUserConfig()
                            .clientUserId;

            LookgramCommunity.createAsync(
                    telegramId,
                    type,
                    name,
                    new LookgramCommunity.Callback() {
                        @Override
                        public void onSuccess(JSONObject community) {
                            if (getParentActivity() != null) {
                                getParentActivity().runOnUiThread(() -> {
                                    Toast.makeText(
                                            context,
                                            "Создано! ID: " +
                                                    community.optInt("id"),
                                            Toast.LENGTH_LONG
                                    ).show();

                                    finishFragment();
                                });
                            }
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
        });

        return fragmentView = layout;
    }
}
