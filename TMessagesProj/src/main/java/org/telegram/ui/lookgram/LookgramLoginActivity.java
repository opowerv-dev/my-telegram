package org.telegram.ui.lookgram;

import android.content.Context;
import android.graphics.Color;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.telegram.messenger.lookgram.LookgramAuthApi;
import org.telegram.messenger.lookgram.LookgramAuthSession;
import org.telegram.ui.ActionBar.BaseFragment;

public class LookgramLoginActivity extends BaseFragment {
    private static final String SERVER = "https://lookgram.wisp.uno";

    private EditText codeInput;
    private TextView statusView;
    private Button loginButton;
    private Button logoutButton;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setTitle("Вход в Lookgram");
        actionBar.setBackButtonImage(
            org.telegram.messenger.R.drawable.ic_ab_back
        );

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(28, 32, 28, 32);
        layout.setBackgroundColor(Color.rgb(25, 25, 30));

        TextView logo = new TextView(context);
        logo.setText("L");
        logo.setTextSize(54);
        logo.setTypeface(null, android.graphics.Typeface.BOLD);
        logo.setTextColor(Color.rgb(235, 65, 80));
        logo.setGravity(Gravity.CENTER);
        layout.addView(logo, new LinearLayout.LayoutParams(-1, -2));

        TextView description = new TextView(context);
        description.setText(
            "Отдельный аккаунт Lookgram\n\n" +
            "1. Открой своего Telegram-бота Lookgram.\n" +
            "2. Отправь команду /login.\n" +
            "3. Введи полученный одноразовый код ниже."
        );
        description.setTextSize(16);
        description.setTextColor(Color.WHITE);
        description.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams descParams =
            new LinearLayout.LayoutParams(-1, -2);
        descParams.bottomMargin = 24;
        layout.addView(description, descParams);

        codeInput = new EditText(context);
        codeInput.setSingleLine(true);
        codeInput.setHint("Код из Telegram");
        codeInput.setHintTextColor(Color.GRAY);
        codeInput.setTextColor(Color.WHITE);
        codeInput.setTextSize(20);
        codeInput.setGravity(Gravity.CENTER);
        codeInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        codeInput.setFilters(new InputFilter[] {
            new InputFilter.LengthFilter(8)
        });
        codeInput.setPadding(16, 12, 16, 12);
        layout.addView(codeInput, new LinearLayout.LayoutParams(-1, -2));

        statusView = new TextView(context);
        statusView.setTextSize(14);
        statusView.setTextColor(Color.LTGRAY);
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(0, 18, 0, 18);
        layout.addView(statusView, new LinearLayout.LayoutParams(-1, -2));

        loginButton = new Button(context);
        loginButton.setText("Войти в Lookgram");
        layout.addView(loginButton, new LinearLayout.LayoutParams(-1, -2));
        loginButton.setOnClickListener(v -> verifyCode());

        logoutButton = new Button(context);
        logoutButton.setText("Выйти из Lookgram");
        layout.addView(logoutButton, new LinearLayout.LayoutParams(-1, -2));
        logoutButton.setOnClickListener(v -> logout());

        refreshSessionState();
        fragmentView = layout;
        return fragmentView;
    }

    private void refreshSessionState() {
        Context context = getParentActivity();
        if (context == null) return;

        final String token;
        try {
            token = LookgramAuthSession.getToken(context);
        } catch (Exception e) {
            statusView.setText("Не удалось прочитать сохранённую сессию.");
            return;
        }

        if (token == null || token.isEmpty()) {
            statusView.setText("Вход не выполнен. Код действует 5 минут и используется один раз.");
            logoutButton.setVisibility(View.GONE);
            return;
        }

        statusView.setText("Проверяем сессию Lookgram…");
        logoutButton.setVisibility(View.VISIBLE);

        new Thread(() -> {
            String message;
            boolean valid = false;
            boolean rejected = false;
            try {
                String response = LookgramAuthApi.get(SERVER, "/auth/me", token);
                JSONObject result = new JSONObject(response);
                JSONObject profile = result.optJSONObject("profile");
                String name = profile == null ? "" : profile.optString("name", "");
                String username = profile == null ? "" : profile.optString("username", "");
                message = "Сессия активна.";
                if (!name.isEmpty()) {
                    message += " Добро пожаловать, " + name + ".";
                } else if (!username.isEmpty()) {
                    message += " @" + username;
                }
                valid = true;
            } catch (Exception e) {
                String error = e.getMessage() == null ? "" : e.getMessage();
                if (error.contains("HTTP 401") || error.contains("HTTP 423")) {
                    message = error.contains("HTTP 423")
                        ? "Аккаунт Lookgram заблокирован."
                        : "Сессия истекла. Войди заново через /login.";
                    rejected = true;
                } else {
                    message = "Не удалось проверить сессию. Проверь соединение и повтори попытку.";
                }
            }

            final String resultMessage = message;
            final boolean sessionValid = valid;
            final boolean sessionRejected = rejected;
            Context activityContext = getParentActivity();
            if (activityContext instanceof android.app.Activity) {
                ((android.app.Activity) activityContext).runOnUiThread(() -> {
                    if (statusView == null || logoutButton == null) return;
                    if (sessionRejected) {
                        try {
                            LookgramAuthSession.clear(activityContext);
                        } catch (Exception ignored) {
                        }
                    }
                    statusView.setText(resultMessage);
                    logoutButton.setVisibility(
                        sessionValid || !sessionRejected ? View.VISIBLE : View.GONE
                    );
                });
            }
        }).start();
    }

    private void verifyCode() {
        final String code = codeInput.getText().toString().trim();

        if (!code.matches("[0-9]{5,8}")) {
            codeInput.setError("Введи код из 5–8 цифр");
            return;
        }

        loginButton.setEnabled(false);
        statusView.setText("Проверяем код…");

        new Thread(() -> {
            String message;
            boolean success = false;

            try {
                JSONObject body = new JSONObject();
                body.put("code", code);

                String response = LookgramAuthApi.postJson(
                    SERVER, "/auth/verify", body.toString(), null
                );

                JSONObject result = new JSONObject(response);
                String token = result.optString("token", "");
                if (token.isEmpty()) {
                    throw new IllegalStateException("Сервер не вернул токен сессии");
                }

                LookgramAuthSession.saveToken(getParentActivity(), token);

                JSONObject user = result.optJSONObject("user");
                JSONObject profile = user == null ? null : user.optJSONObject("profile");
                String username = profile == null ? "" : profile.optString("username", "");
                String name = profile == null ? "" : profile.optString("name", "");

                message = "Вход выполнен!";
                if (!name.isEmpty()) {
                    message += " Добро пожаловать, " + name + ".";
                } else if (!username.isEmpty()) {
                    message += " @" + username;
                }
                success = true;
            } catch (Exception e) {
                String error = e.getMessage() == null ? "" : e.getMessage();
                if (error.contains("HTTP 400") ||
                    error.contains("HTTP 401") ||
                    error.contains("HTTP 404")) {
                    message = "Код неверный или истёк. Получи новый через /login.";
                } else if (error.contains("HTTP 429")) {
                    message = "Слишком много попыток. Подожди немного.";
                } else {
                    message = "Не удалось войти. Проверь доступность сервера и попробуй снова.";
                }
            }

            final boolean loggedIn = success;
            final String finalMessage = message;
            Context context = getParentActivity();
            if (context != null) {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    if (loginButton == null) return;
                    loginButton.setEnabled(true);
                    statusView.setText(finalMessage);
                    logoutButton.setVisibility(
                        loggedIn ? View.VISIBLE :
                        (LookgramAuthSession.hasSession(context)
                            ? View.VISIBLE : View.GONE)
                    );
                    if (loggedIn) {
                        codeInput.setText("");
                    }
                    Toast.makeText(
                        context, finalMessage, Toast.LENGTH_LONG
                    ).show();
                });
            }
        }).start();
    }

    private void logout() {
        try {
            LookgramAuthSession.clear(getParentActivity());
            statusView.setText("Ты вышел из Lookgram.");
            logoutButton.setVisibility(View.GONE);
            codeInput.setText("");
            Toast.makeText(
                getParentActivity(),
                "Сессия Lookgram удалена с устройства",
                Toast.LENGTH_SHORT
            ).show();
        } catch (Exception e) {
            Toast.makeText(
                getParentActivity(),
                "Не удалось удалить локальную сессию",
                Toast.LENGTH_LONG
            ).show();
        }
    }
}
