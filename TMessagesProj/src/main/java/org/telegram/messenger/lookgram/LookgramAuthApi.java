package org.telegram.messenger.lookgram;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * HTTPS-only transport for the separate Lookgram authentication API.
 * No endpoint is assumed to exist. Never disable TLS verification.
 * Call from a background thread, not the Android main/UI thread.
 */
public final class LookgramAuthApi {
    private LookgramAuthApi() {}

    public static String get(
        String baseUrl,
        String path,
        String bearerToken
    ) throws Exception {
        if (baseUrl == null || !baseUrl.matches("^https://[^/]+/?$")) {
            throw new IllegalArgumentException("A valid HTTPS base URL is required.");
        }
        if (path == null || !path.startsWith("/") || path.startsWith("//")) {
            throw new IllegalArgumentException("Invalid API path");
        }
        if (bearerToken == null || !bearerToken.matches("[a-fA-F0-9]{64}")) {
            throw new IllegalArgumentException("Invalid session token");
        }

        URL url = new URL(baseUrl.replaceAll("/+$", "") + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + bearerToken);

            int status = connection.getResponseCode();
            if (status >= 300 && status < 400) {
                throw new SecurityException("Redirect refused.");
            }

            InputStream stream = status >= 400
                ? connection.getErrorStream() : connection.getInputStream();
            String response = "";
            if (stream != null) {
                try (InputStream in = stream;
                     ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int count;
                    while ((count = in.read(buffer)) != -1) {
                        out.write(buffer, 0, count);
                    }
                    response = new String(out.toByteArray(), StandardCharsets.UTF_8);
                }
            }
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("Lookgram API returned HTTP " + status);
            }
            return response;
        } finally {
            connection.disconnect();
        }
    }

    public static String postJson(
        String baseUrl,
        String path,
        String json,
        String bearerToken
    ) throws Exception {
        if (baseUrl == null || !baseUrl.matches("^https://[^/]+/?$")) {
            throw new IllegalArgumentException(
                "Configure a valid HTTPS base URL before authentication."
            );
        }
        if (path == null || !path.startsWith("/")
                || path.startsWith("//")) {
            throw new IllegalArgumentException("Invalid API path");
        }
        if (json == null) {
            throw new IllegalArgumentException("Missing JSON body");
        }

        URL url = new URL(baseUrl.replaceAll("/+$", "") + path);
        if (!"https".equalsIgnoreCase(url.getProtocol())) {
            throw new IllegalArgumentException("HTTPS is required");
        }

        HttpURLConnection connection =
            (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setInstanceFollowRedirects(false);
            connection.setDoOutput(true);
            connection.setRequestProperty(
                "Content-Type", "application/json; charset=utf-8"
            );
            connection.setRequestProperty("Accept", "application/json");

            if (bearerToken != null && !bearerToken.isEmpty()) {
                connection.setRequestProperty(
                    "Authorization", "Bearer " + bearerToken
                );
            }

            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(body.length);
            try (OutputStream out = connection.getOutputStream()) {
                out.write(body);
            }

            int status = connection.getResponseCode();
            if (status >= 300 && status < 400) {
                throw new SecurityException(
                    "Redirect refused; HTTPS endpoint must be configured directly."
                );
            }

            InputStream stream = status >= 400
                ? connection.getErrorStream()
                : connection.getInputStream();

            String response = "";
            if (stream != null) {
                try (InputStream in = stream;
                     ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int count;
                    while ((count = in.read(buffer)) != -1) {
                        out.write(buffer, 0, count);
                    }
                    response = new String(
                        out.toByteArray(), StandardCharsets.UTF_8
                    );
                }
            }

            if (status < 200 || status >= 300) {
                throw new IllegalStateException(
                    "Lookgram API returned HTTP " + status
                );
            }
            return response;
        } finally {
            connection.disconnect();
        }
    }
}
