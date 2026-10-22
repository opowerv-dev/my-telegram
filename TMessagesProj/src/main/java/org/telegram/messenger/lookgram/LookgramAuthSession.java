package org.telegram.messenger.lookgram;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Local Lookgram-only session storage.
 * This class does not activate or modify a Telegram account.
 * Call only after the server has verified the OTP over HTTPS.
 */
public final class LookgramAuthSession {
    private static final String PREFS = "lookgram_auth_session_v1";
    private static final String KEY_ALIAS = "lookgram_auth_aes_v1";
    private static final String TOKEN = "encrypted_token";
    private static final String IV = "token_iv";

    private LookgramAuthSession() {}

    private static void requireSupported() {
        if (Build.VERSION.SDK_INT < 23) {
            throw new IllegalStateException(
                "Secure Lookgram session storage requires Android 6.0 or newer."
            );
        }
    }

    private static SecretKey getKey() throws Exception {
        requireSupported();
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null);

        java.security.Key existing = store.getKey(KEY_ALIAS, null);
        if (existing instanceof SecretKey) {
            return (SecretKey) existing;
        }

        KeyGenerator generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        );
        generator.init(new KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build());

        return generator.generateKey();
    }

    public static synchronized void saveToken(
        Context context, String token
    ) throws Exception {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty session token");
        }

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, getKey());

        byte[] encrypted = cipher.doFinal(
            token.getBytes(StandardCharsets.UTF_8)
        );

        SharedPreferences prefs = context.getApplicationContext()
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        boolean saved = prefs.edit()
            .putString(TOKEN, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .putString(IV, Base64.encodeToString(
                cipher.getIV(), Base64.NO_WRAP
            ))
            .commit();

        if (!saved) {
            throw new IllegalStateException("Could not save Lookgram session");
        }
    }

    public static synchronized String getToken(Context context) {
        try {
            requireSupported();

            SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);

            String encoded = prefs.getString(TOKEN, null);
            String encodedIv = prefs.getString(IV, null);
            if (encoded == null || encodedIv == null) {
                return null;
            }

            byte[] encrypted = Base64.decode(encoded, Base64.NO_WRAP);
            byte[] iv = Base64.decode(encodedIv, Base64.NO_WRAP);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(
                Cipher.DECRYPT_MODE,
                getKey(),
                new GCMParameterSpec(128, iv)
            );

            return new String(
                cipher.doFinal(encrypted), StandardCharsets.UTF_8
            );
        } catch (Exception e) {
            // Never return a possibly corrupted or undecryptable token.
            return null;
        }
    }

    public static synchronized void clear(Context context) throws Exception {
        SharedPreferences prefs = context.getApplicationContext()
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        if (!prefs.edit().clear().commit()) {
            throw new IllegalStateException("Could not clear Lookgram session");
        }

        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null);
        if (store.containsAlias(KEY_ALIAS)) {
            store.deleteEntry(KEY_ALIAS);
        }
    }

    public static boolean hasSession(Context context) {
        String token = getToken(context);
        return token != null && !token.isEmpty();
    }
}
