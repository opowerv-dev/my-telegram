package org.telegram.ui.lookgram;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.lookgram.LookgramStickers;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.List;

public class LookgramStickersActivity extends BaseFragment {

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setTitle("Стикеры");
        actionBar.setBackButtonImage(
                org.telegram.messenger.R.drawable.ic_ab_back
        );

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);

        TextView loading = new TextView(context);
        loading.setText("Загрузка стикеров...");
        loading.setTextColor(Color.WHITE);
        loading.setTextSize(18);
        layout.addView(loading);

        LookgramStickers.getAsync(new LookgramStickers.Callback() {

            @Override
            public void onResult(List<LookgramStickers.Sticker> stickers) {
                if (getParentActivity() == null) {
                    return;
                }

                getParentActivity().runOnUiThread(() -> {
                    layout.removeAllViews();

                    if (stickers.isEmpty()) {
                        TextView empty = new TextView(context);
                        empty.setText("Стикеров пока нет");
                        empty.setTextColor(Color.LTGRAY);
                        empty.setTextSize(17);
                        layout.addView(empty);
                        return;
                    }

                    for (LookgramStickers.Sticker sticker : stickers) {
                        TextView item = new TextView(context);

                        String text = sticker.emoji + "  " + sticker.name;

                        if (sticker.animated) {
                            text += "  • Анимация";
                        }

                        item.setText(text);
                        item.setTextColor(Color.WHITE);
                        item.setTextSize(18);
                        item.setPadding(20, 25, 20, 25);

                        item.setOnClickListener(v ->
                                Toast.makeText(
                                        context,
                                        "Стикер: " + sticker.name,
                                        Toast.LENGTH_SHORT
                                ).show()
                        );

                        layout.addView(item);
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
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
                }
            }
        });

        return fragmentView = layout;
    }
}
