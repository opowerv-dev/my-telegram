package org.telegram.messenger.lookgram;

import android.content.Context;
import org.telegram.messenger.AccountInstance;

public class LookgramAccount {

    public static LookgramStars getStars(Context context, int account) {
        long userId = AccountInstance.getInstance(account)
                .getUserConfig().clientUserId;
        return new LookgramStars(context, userId);
    }
}
