package dev.kaooot.debugger.api.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import javax.net.ssl.HttpsURLConnection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class AuthHttpRequest {

    private URI uri;
    private RequestMethod method;
    private final Map<String, String> headers = new Object2ObjectOpenHashMap<>();
    private ContentType contentType;
    private Runnable handler;
    private JsonObject data;

    public static AuthHttpRequest builder() {
        return new AuthHttpRequest();
    }

    public AuthHttpRequest uri(URI uri) {
        this.uri = uri;
        return this;
    }

    public AuthHttpRequest method(RequestMethod method) {
        this.method = method;
        return this;
    }

    public AuthHttpRequest header(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public AuthHttpRequest headers(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    public AuthHttpRequest contentType(ContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    public AuthHttpRequest body(JsonObject data) {
        this.data = data;
        return this;
    }

    public AuthHttpRequest responseHandler(Runnable handler) {
        this.handler = handler;
        return this;
    }

    public Response send0() {
        Objects.requireNonNull(this.uri, "Uri must not be null");
        Objects.requireNonNull(this.method, "Request method must not be null");

        if (!this.method.equals(RequestMethod.POST) && this.data != null) {
            throw new RuntimeException("Data can only be specified for a post request");
        }

        if (this.method.equals(RequestMethod.POST)) {
            Objects.requireNonNull(this.contentType, "Content Type must not be null");
        }

        JsonObject jsonObject = null;
        HttpsURLConnection connection = null;

        try {
            final URL url = this.uri.toURL();
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod(this.method.name());

            if (this.contentType != null) {
                connection.setRequestProperty("Content-Type", this.contentType.getId());
            }

            connection.setDoOutput(true);
            connection.setDoInput(true);

            if (this.contentType.equals(ContentType.FORM)) {
                try (final OutputStream outputStream = connection.getOutputStream()) {
                    final StringBuilder stringBuilder = new StringBuilder();
                    final Iterator<Map.Entry<String, String>> iterator =
                        this.headers.entrySet().iterator();

                    while (iterator.hasNext()) {
                        final Map.Entry<String, String> entry = iterator.next();

                        stringBuilder.append(
                                URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                            .append("=")
                            .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));

                        if (iterator.hasNext()) {
                            stringBuilder.append("&");
                        }
                    }

                    outputStream.write(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                }
            } else {
                if (this.data != null) {
                    for (final Map.Entry<String, String> entry : this.headers.entrySet()) {
                        connection.setRequestProperty(entry.getKey(), entry.getValue());
                    }

                    try (final OutputStream outputStream = connection.getOutputStream()) {
                        outputStream.write(this.data.toString().replaceAll("/", "/")
                            .getBytes(StandardCharsets.UTF_8));
                    }
                }
            }

            if (connection.getResponseCode() == 200) {
                try (final InputStream inputStream = connection.getInputStream();
                     final InputStreamReader reader = new InputStreamReader(inputStream)) {
                    jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                }
            }

            if (this.handler != null && jsonObject == null) {
                this.handler.run();
            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Response(jsonObject, connection);
    }

    public JsonObject send() {
        return this.send0().getJsonObject();
    }

    public enum RequestMethod {
        POST,
        GET
    }

    @Getter
    @RequiredArgsConstructor
    public enum ContentType {
        JSON("application/json"),
        FORM("application/x-www-form-urlencoded");

        private final String id;
    }

    @Value
    public static class Response {
        JsonObject jsonObject;
        HttpsURLConnection connection;
    }
}