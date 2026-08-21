package dev.kaooot.debugger.core.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import javax.net.ssl.HttpsURLConnection;
import lombok.Builder;
import dev.kaooot.debugger.BedrockDebuggerProxy;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Builder
public class HttpRequest<R> {

    private final BedrockDebuggerProxy proxy;
    private final HttpRequestMethod method;
    private final String url;
    private final Object body;
    private final String auth;

    public R send(Class<R> clazz) {
        return this.send(clazz, Collections.emptyMap());
    }

    public R send(Class<R> clazz, Map<String, Object> params) {
        Objects.requireNonNull(this.url);
        Objects.requireNonNull(this.method);
        try {
            final boolean writeBody = this.body != null;
            final byte[] body = !writeBody ? new byte[0] : this.proxy.getGson().newBuilder()
                .serializeNulls()
                .create()
                .toJson(this.body).getBytes(StandardCharsets.UTF_8);
            final HttpsURLConnection connection = (HttpsURLConnection) new URL(this.url)
                .openConnection();
            connection.setRequestMethod(this.method.name());
            connection.setRequestProperty("content-type", "application/json");
            connection.setRequestProperty("user-agent", "libhttpclient/1.0.0.0");
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            connection.setRequestProperty("Host", this.getHostFromUrl(this.url));
            connection.setRequestProperty("Content-Length", String.valueOf(body.length));
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Cache-Control", "no-cache");
            if (this.auth != null) {
                connection.setRequestProperty("Authorization", this.auth);
            }
            for (final Map.Entry<String, Object> entry : params.entrySet()) {
                connection.setRequestProperty(entry.getKey(), String.valueOf(entry.getValue()));
            }
            connection.setDoInput(true);
            connection.setDoOutput(writeBody);

            if (writeBody) {
                try (final OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(body);
                }
            }

            final int code = connection.getResponseCode();
            final String message = connection.getResponseMessage();

            if (code != 200) {
                throw new IllegalStateException("Received code: " + code + " " + message);
            }

            final R result;
            try (final InputStream inputStream = connection.getInputStream()) {
                final String data = new String(inputStream.readAllBytes());
                result = this.proxy.getGson().fromJson(data, clazz);
            }

            connection.disconnect();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getHostFromUrl(String url) {
        final String s = url.substring(url.indexOf("/") + 2);
        return s.substring(0, s.indexOf("/"));
    }
}