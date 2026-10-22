package org.telegram.ui.lookgram;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.lookgram.LookgramStars;
import org.telegram.ui.ActionBar.BaseFragment;

public class LookgramStarsActivity extends BaseFragment {

    private LookgramStars stars;
    private TextView balanceView;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        long userId = AccountInstance
                .getInstance(currentAccount)
                .getUserConfig()
                .clientUserId;

        stars = new LookgramStars(getParentActivity(), userId);

        return true;
    }

    @Override
    public android.view.View createView(Context context) {
        actionBar.setTitle("Lookgram Stars");
        actionBar.setBackButtonImage(org.telegram.messenger.R.drawable.ic_ab_back);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(40, 40, 40, 40);

        balanceView = new TextView(context);
        balanceView.setTextSize(28);
        balanceView.setTextColor(Color.WHITE);
        balanceView.setGravity(Gravity.CENTER);
        balanceView.setText("⭐ ...");

        layout.addView(balanceView,
                new LinearLayout.LayoutParams(-1, -2));

        stars.getBalanceAsync(new LookgramStars.BalanceCallback() {
            @Override
            public void onResult(long balance) {
                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() ->
                            balanceView.setText("⭐ " + balance)
                    );
                }
            }

            @Override
            public void onError(Exception e) {
                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() ->
                            balanceView.setText("⭐ Ошибка соединения")
                    );
                }
            }
        });

        return fragmentView = layout;
    }
}
