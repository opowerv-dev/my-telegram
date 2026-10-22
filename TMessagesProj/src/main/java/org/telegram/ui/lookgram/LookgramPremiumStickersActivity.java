package org.telegram.ui.lookgram;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.lookgram.LookgramPremiumStickers;
import org.telegram.ui.ActionBar.BaseFragment;

public class LookgramPremiumStickersActivity extends BaseFragment {

    private LinearLayout list;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setTitle("Premium-стикеры");
        actionBar.setBackButtonImage(
                org.telegram.messenger.R.drawable.ic_ab_back
        );

        list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(30, 30, 30, 30);

        TextView title = new TextView(context);
        title.setText("Premium-стикеры");
        title.setTextSize(24);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        list.addView(title);

        TextView loading = new TextView(context);
        loading.setText("Загрузка...");
        loading.setTextColor(Color.LTGRAY);
        list.addView(loading);

        LookgramPremiumStickers.getAsync(
                stickers -> {
                    if (getParentActivity() == null) {
                        return;
                    }

                    getParentActivity().runOnUiThread(() -> {
                        list.removeView(loading);

                        if (stickers.isEmpty()) {
                            TextView empty =
                                    new TextView(context);

                            empty.setText(
                                    "Premium-стикеров пока нет"
                            );
                            empty.setTextColor(
                                    Color.LTGRAY
                            );

                            list.addView(empty);
                            return;
                        }

                        long userId =
                                AccountInstance
                                        .getInstance(currentAccount)
                                        .getUserConfig()
                                        .clientUserId;

                        for (
                                LookgramPremiumStickers.Sticker sticker
                                : stickers
                        ) {
                            LinearLayout row =
                                    new LinearLayout(context);

                            row.setOrientation(
                                    LinearLayout.HORIZONTAL
                            );
                            row.setGravity(
                                    Gravity.CENTER_VERTICAL
                            );
                            row.setPadding(
                                    0, 20, 0, 20
                            );

                            TextView name =
                                    new TextView(context);

                            name.setText(
                                    sticker.emoji +
                                    "  " +
                                    sticker.name
                            );
                            name.setTextSize(18);
                            name.setTextColor(
                                    Color.WHITE
                            );

                            row.addView(
                                    name,
                                    new LinearLayout.LayoutParams(
                                            0,
                                            -2,
                                            1
                                    )
                            );

                            Button add =
                                    new Button(context);

                            add.setText("Добавить");

                            add.setOnClickListener(v ->
                                    LookgramPremiumStickers
                                            .addToUserAsync(
                                                    userId,
                                                    sticker.id,
                                                    new LookgramPremiumStickers.Callback() {
                                                        @Override
                                                        public void onResult(
                                                                java.util.List<LookgramPremiumStickers.Sticker> result
                                                        ) {
                                                            if (getParentActivity() != null) {
                                                                getParentActivity()
                                                                        .runOnUiThread(() ->
                                                                                Toast.makeText(
                                                                                        context,
                                                                                        "Стикер добавлен",
                                                                                        Toast.LENGTH_SHORT
                                                                                ).show()
                                                                        );
                                                            }
                                                        }

                                                        @Override
                                                        public void onError(
                                                                Exception e
                                                        ) {
                                                            if (getParentActivity() != null) {
                                                                getParentActivity()
                                                                        .runOnUiThread(() ->
                                                                                Toast.makeText(
                                                                                        context,
                                                                                        "Ошибка: " + e.getMessage(),
                                                                                        Toast.LENGTH_SHORT
                                                                                ).show()
                                                                        );
                                                            }
                                                        }
                                                    }
                                            )
                            );

                            row.addView(add);
                            list.addView(row);
                        }
                    });
                }
        );

        return fragmentView = list;
    }
}
