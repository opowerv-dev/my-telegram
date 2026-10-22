package org.telegram.messenger.lookgram;

import android.content.Context;
import android.content.SharedPreferences;

public class LookgramMessageOverrides {

    private static final String PREFS = "lookgram_message_overrides";

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String key(long dialogId, int messageId, String type) {
        return dialogId + "_" + messageId + "_" + type;
    }

    public static void setText(Context context, long dialogId, int messageId, String text) {
        prefs(context).edit()
                .putString(key(dialogId, messageId, "text"), text)
                .apply();
    }

    public static String getText(Context context, long dialogId, int messageId) {
        return prefs(context).getString(
                key(dialogId, messageId, "text"),
                null
        );
    }

    public static void setCaption(Context context, long dialogId, int messageId, String text) {
        prefs(context).edit()
                .putString(key(dialogId, messageId, "caption"), text)
                .apply();
    }

    public static String getCaption(Context context, long dialogId, int messageId) {
        return prefs(context).getString(
                key(dialogId, messageId, "caption"),
                null
        );
    }

    public static void removeText(Context context, long dialogId, int messageId) {
        prefs(context).edit()
                .remove(key(dialogId, messageId, "text"))
                .apply();
    }

    public static void removeCaption(Context context, long dialogId, int messageId) {
        prefs(context).edit()
                .remove(key(dialogId, messageId, "caption"))
                .apply();
    }

    public static boolean hasText(Context context, long dialogId, int messageId) {
        return prefs(context).contains(
                key(dialogId, messageId, "text")
        );
    }

    public static boolean hasCaption(Context context, long dialogId, int messageId) {
        return prefs(context).contains(
                key(dialogId, messageId, "caption")
        );
    }

    public static void setDeleted(Context context, long dialogId, int messageId, boolean deleted) {
        prefs(context).edit()
                .putBoolean(key(dialogId, messageId, "deleted"), deleted)
                .apply();
    }

    public static boolean isDeleted(Context context, long dialogId, int messageId) {
        return prefs(context).getBoolean(
                key(dialogId, messageId, "deleted"),
                false
        );
    }

    public static void restore(Context context, long dialogId, int messageId) {
        prefs(context).edit()
                .remove(key(dialogId, messageId, "deleted"))
                .apply();
    }
}
