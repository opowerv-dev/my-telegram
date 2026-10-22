package org.telegram.ui.lookgram;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.telegram.messenger.AccountInstance;
import org.telegram.ui.ActionBar.BaseFragment;

public class LookgramAdminActivity extends BaseFragment {

    private EditText userId;
    private Spinner targetType;
    private Spinner premiumDays;

    private final int[] days = {7, 30, 90, 365};

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setTitle("Lookgram Admin");
        actionBar.setBackButtonImage(
                org.telegram.messenger.R.drawable.ic_ab_back
        );

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        TextView title = new TextView(context);
        title.setText("Управление пользователем");
        title.setTextSize(22);
        title.setTextColor(Color.WHITE);
        layout.addView(title);

        targetType = new Spinner(context);
        String[] targetLabels = {
                "Пользователь",
                "Канал",
                "Чат"
        };
        targetType.setAdapter(
                new ArrayAdapter<>(
                        context,
                        android.R.layout.simple_spinner_dropdown_item,
                        targetLabels
                )
        );
        layout.addView(targetType);

        userId = new EditText(context);
        userId.setHint("ID пользователя / канала / чата");
        userId.setInputType(2);
        userId.setTextColor(Color.WHITE);
        layout.addView(userId);

        Button verify = new Button(context);
        verify.setText("Проверить статус ✓");
        layout.addView(verify);

        Button giveVerified = new Button(context);
        giveVerified.setText("✓ Выдать галочку");
        layout.addView(giveVerified);

        Button removeVerified = new Button(context);
        removeVerified.setText("Снять ✓");
        layout.addView(removeVerified);

        TextView premiumTitle = new TextView(context);
        premiumTitle.setText("Lookgram Premium");
        premiumTitle.setTextSize(20);
        premiumTitle.setTextColor(Color.WHITE);
        layout.addView(premiumTitle);

        premiumDays = new Spinner(context);

        String[] labels = {
                "7 дней",
                "30 дней",
                "90 дней",
                "365 дней"
        };

        premiumDays.setAdapter(
                new ArrayAdapter<>(
                        context,
                        android.R.layout.simple_spinner_dropdown_item,
                        labels
                )
        );

        layout.addView(premiumDays);

        Button premium = new Button(context);
        premium.setText("🎁 Подарить Premium");
        layout.addView(premium);

        Button removePremium = new Button(context);
        removePremium.setText("Снять Premium");
        layout.addView(removePremium);

        TextView freezeTitle = new TextView(context);
        freezeTitle.setText("Заморозка аккаунта");
        freezeTitle.setTextSize(20);
        freezeTitle.setTextColor(Color.WHITE);
        layout.addView(freezeTitle);

        Button freezeMonth = new Button(context);
        freezeMonth.setText("❄ Заморозить на месяц");
        layout.addView(freezeMonth);

        Button freezeYear = new Button(context);
        freezeYear.setText("❄ Заморозить на год");
        layout.addView(freezeYear);

        Button freezePermanent = new Button(context);
        freezePermanent.setText("❄ Заморозить навсегда");
        layout.addView(freezePermanent);

        Button unfreeze = new Button(context);
        unfreeze.setText("🔓 Разморозить аккаунт");
        layout.addView(unfreeze);

        verify.setOnClickListener(v -> checkUser(context));

        giveVerified.setOnClickListener(v ->
                changeVerified(context, true)
        );

        removeVerified.setOnClickListener(v ->
                changeVerified(context, false)
        );

        premium.setOnClickListener(v -> {
            int selected = premiumDays.getSelectedItemPosition();
            changeStatus(context, "premium", true, days[selected]);
        });

        removePremium.setOnClickListener(v ->
                changeStatus(context, "premium", false, 0)
        );

        freezeMonth.setOnClickListener(v ->
                freezeAccount(context, "month")
        );

        freezeYear.setOnClickListener(v ->
                freezeAccount(context, "year")
        );

        freezePermanent.setOnClickListener(v ->
                freezeAccount(context, "permanent")
        );

        unfreeze.setOnClickListener(v ->
                freezeAccount(context, "unfreeze")
        );

        return fragmentView = layout;
    }

    private String getAdminTelegramId() {
        return String.valueOf(
                AccountInstance
                        .getInstance(currentAccount)
                        .getUserConfig()
                        .clientUserId
        );
    }

    private void checkUser(Context context) {
        int type = targetType.getSelectedItemPosition();

        if (type != 0) {
            checkCommunity(context);
            return;
        }

        new Thread(() -> {
            try {
                long id = Long.parseLong(userId.getText().toString());

                URL url = new URL(
                        "http://78.154.103.51:11563/admin/user/" + id
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty(
                        "X-Telegram-ID",
                        getAdminTelegramId()
                );

                int code = connection.getResponseCode();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                code >= 200 && code < 300
                                        ? connection.getInputStream()
                                        : connection.getErrorStream()
                        )
                );

                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                connection.disconnect();

                if (code < 200 || code >= 300) {
                    throw new Exception("HTTP " + code + ": " + result);
                }

                JSONObject json = new JSONObject(result.toString());

                boolean verified = json.optBoolean("verified", false);
                boolean premium = json.optBoolean("premium", false);
                long until = json.optLong("premiumUntil", 0);

                String message =
                        "Пользователь\n✓ " +
                        (verified ? "выдана" : "нет") +
                        "\nPremium: " +
                        (premium ? "активен" : "нет");

                if (premium && until > 0) {
                    message += "\nДо: " + until;
                }

                showResult(context, message);

            } catch (Exception e) {
                showError(context, e);
            }
        }).start();
    }

    private void checkCommunity(Context context) {
        new Thread(() -> {
            try {
                long id = Long.parseLong(userId.getText().toString());

                URL url = new URL(
                        "http://78.154.103.51:11563/communities/" + id
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");

                int code = connection.getResponseCode();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                code >= 200 && code < 300
                                        ? connection.getInputStream()
                                        : connection.getErrorStream()
                        )
                );

                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                connection.disconnect();

                if (code < 200 || code >= 300) {
                    throw new Exception("HTTP " + code + ": " + result);
                }

                JSONObject json = new JSONObject(result.toString());

                boolean verified = json.optBoolean("verified", false);
                String name = json.optString("name", "");
                String type = json.optString("type", "chat");

                String typeName =
                        "channel".equals(type) ? "Канал" : "Чат";

                String message =
                        typeName + ": " + name +
                        "\n✓ " +
                        (verified ? "выдана" : "нет");

                showResult(context, message);

            } catch (Exception e) {
                showError(context, e);
            }
        }).start();
    }

    private void showResult(Context context, String message) {
        if (getParentActivity() != null) {
            getParentActivity().runOnUiThread(() ->
                    Toast.makeText(
                            context,
                            message,
                            Toast.LENGTH_LONG
                    ).show()
            );
        }
    }

    private void changeVerified(Context context, boolean value) {
        int type = targetType.getSelectedItemPosition();

        if (type == 0) {
            changeStatus(context, "verified", value, 0);
            return;
        }

        final String communityType = type == 1 ? "channel" : "chat";

        new Thread(() -> {
            try {
                long id = Long.parseLong(userId.getText().toString());

                URL url = new URL(
                        "http://78.154.103.51:11563/admin/community/status"
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );
                connection.setRequestProperty(
                        "X-Telegram-ID",
                        getAdminTelegramId()
                );
                connection.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("communityId", id);
                json.put("status", "verified");
                json.put("value", value);

                OutputStream output = connection.getOutputStream();
                output.write(json.toString().getBytes("UTF-8"));
                output.close();

                int code = connection.getResponseCode();

                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() ->
                            Toast.makeText(
                                    context,
                                    code == 200
                                            ? (value
                                                ? "✓ Галочка выдана"
                                                : "✓ Галочка снята")
                                            : "Ошибка: HTTP " + code,
                                    Toast.LENGTH_LONG
                            ).show()
                    );
                }

                connection.disconnect();

            } catch (Exception e) {
                showError(context, e);
            }
        }).start();
    }

    private void changeStatus(
            Context context,
            String status,
            boolean value,
            int premiumDays
    ) {
        new Thread(() -> {
            try {
                long id = Long.parseLong(
                        userId.getText().toString()
                );

                URL url = new URL(
                        "http://78.154.103.51:11563/admin/user/status"
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );
                connection.setRequestProperty(
                        "X-Telegram-ID",
                        getAdminTelegramId()
                );
                connection.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("userId", id);
                body.put("status", status);
                body.put("value", value);

                if (status.equals("premium") && value) {
                    body.put("days", premiumDays);
                }

                OutputStream output =
                        connection.getOutputStream();

                output.write(
                        body.toString().getBytes("UTF-8")
                );

                output.close();

                int code = connection.getResponseCode();
                connection.disconnect();

                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() ->
                            Toast.makeText(
                                    context,
                                    code == 200
                                            ? "Готово"
                                            : "Ошибка: " + code,
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
                }

            } catch (Exception e) {
                showError(context, e);
            }
        }).start();
    }

    private void freezeAccount(Context context, String duration) {
        new Thread(() -> {
            try {
                long id = Long.parseLong(
                        userId.getText().toString()
                );

                URL url = new URL(
                        "http://78.154.103.51:11563/admin/user/freeze"
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );
                connection.setRequestProperty(
                        "X-Telegram-ID",
                        getAdminTelegramId()
                );
                connection.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("userId", id);
                body.put("duration", duration);

                OutputStream output =
                        connection.getOutputStream();

                output.write(
                        body.toString().getBytes("UTF-8")
                );
                output.close();

                int code = connection.getResponseCode();

                connection.disconnect();

                if (getParentActivity() != null) {
                    getParentActivity().runOnUiThread(() ->
                            Toast.makeText(
                                    context,
                                    code == 200
                                            ? "Готово"
                                            : "Ошибка: " + code,
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
                }

            } catch (Exception e) {
                showError(context, e);
            }
        }).start();
    }

    private void showError(Context context, Exception e) {
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
}
