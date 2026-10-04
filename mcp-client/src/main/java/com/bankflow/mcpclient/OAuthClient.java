package com.bankflow.mcpclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;

public final class OAuthClient {

    private static final Logger LOG =
            LoggerFactory.getLogger(OAuthClient.class);

    private static final String DEFAULT_BANKFLOW_BASE_URL =
            "http://localhost:8080";

    private static final String BANKFLOW_BASE_URL =
            System.getenv().getOrDefault(
                    "BANKFLOW_BASE_URL",
                    DEFAULT_BANKFLOW_BASE_URL
            );

    private static final String AUTHORIZATION_ENDPOINT =
            BANKFLOW_BASE_URL + "/oauth2/authorize";

    private static final String TOKEN_ENDPOINT =
            BANKFLOW_BASE_URL + "/oauth2/token";

    private static final String MCP_ENDPOINT =
            BANKFLOW_BASE_URL + "/mcp";

    private static final String CLIENT_ID =
            System.getenv().getOrDefault(
                    "BANKFLOW_MCP_CLIENT_ID",
                    "bankflow-mcp"
            );

    private static final String REDIRECT_URI =
            System.getenv().getOrDefault(
                    "BANKFLOW_MCP_REDIRECT_URI",
                    "http://localhost:3334/oauth/callback"
            );

    private static final String CALLBACK_PATH =
            "/oauth/callback";

    private static final String CALLBACK_HOST =
            "localhost";

    private static final int CALLBACK_PORT = 3334;

    private static final int SUCCESS_STATUS_CODE = 200;

    private static final String CONTENT_TYPE_HEADER =
            "Content-Type";

    private static final String FORM_CONTENT_TYPE =
            "application/x-www-form-urlencoded";

    private static final String AUTHORIZATION_CODE_PARAMETER =
            "code";

    private static final String STATE_PARAMETER =
            "state";

    private static final String ACCESS_TOKEN_FIELD =
            "access_token";

    private static final String PKCE_METHOD =
            "S256";

    private static final int STATE_RANDOM_BYTES = 32;

    private static final int CODE_VERIFIER_RANDOM_BYTES = 64;

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private final HttpClient httpClient =
            HttpClient.newHttpClient();

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    public String authorize() {

        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        String state = generateRandomValue(STATE_RANDOM_BYTES);

        CountDownLatch callbackReceived =
                new CountDownLatch(1);

        OAuthCallback callback =
                new OAuthCallback(callbackReceived);

        HttpServer callbackServer =
                createCallbackServer(callback);

        try {
            callbackServer.start();

            String authorizationUrl =
                    buildAuthorizationUrl(
                            codeChallenge,
                            state
                    );

            LOG.info(
                    "Opening BankFlow OAuth authorization..."
            );

            LOG.info(
                    "OAuth authorization URL: {}",
                    authorizationUrl
            );

            LOG.info(
                    "Waiting for OAuth callback on port {}...",
                    CALLBACK_PORT
            );

            awaitCallback(callbackReceived);

            validateOAuthState(
                    state,
                    callback.getReturnedState()
            );

            String authorizationCode =
                    callback.getAuthorizationCode();

            if (authorizationCode == null) {
                throw new IllegalStateException(
                        "OAuth authorization code was not received"
                );
            }

            return exchangeAuthorizationCode(
                    authorizationCode,
                    codeVerifier
            );

        } finally {
            callbackServer.stop(0);
        }
    }

    private HttpServer createCallbackServer(
            OAuthCallback callback) {

        try {
            HttpServer server =
                    HttpServer.create(
                            new InetSocketAddress(
                                    CALLBACK_HOST,
                                    CALLBACK_PORT
                            ),
                            0
                    );

            server.createContext(
                    CALLBACK_PATH,
                    callback::handle
            );

            return server;

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to start OAuth callback server",
                    exception
            );
        }
    }

    private String buildAuthorizationUrl(
            String codeChallenge,
            String state) {

        return AUTHORIZATION_ENDPOINT
                + "?response_type=code"
                + "&client_id=" + encode(CLIENT_ID)
                + "&code_challenge=" + encode(codeChallenge)
                + "&code_challenge_method=" + encode(PKCE_METHOD)
                + "&redirect_uri=" + encode(REDIRECT_URI)
                + "&state=" + encode(state)
                + "&scope=" + encode(
                "openid profile email"
        )
                + "&resource=" + encode(MCP_ENDPOINT);
    }

    private void awaitCallback(
            CountDownLatch callbackReceived) {

        try {
            callbackReceived.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Interrupted while waiting for OAuth callback",
                    exception
            );
        }
    }

    private void validateOAuthState(
            String expectedState,
            String returnedState) {

        if (!expectedState.equals(returnedState)) {
            throw new IllegalStateException(
                    "OAuth state validation failed"
            );
        }
    }

    private String exchangeAuthorizationCode(
            String authorizationCode,
            String codeVerifier) {

        String form =
                "grant_type=authorization_code"
                        + "&client_id=" + encode(CLIENT_ID)
                        + "&code=" + encode(authorizationCode)
                        + "&redirect_uri=" + encode(REDIRECT_URI)
                        + "&code_verifier=" + encode(codeVerifier);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(TOKEN_ENDPOINT))
                        .header(
                                CONTENT_TYPE_HEADER,
                                FORM_CONTENT_TYPE
                        )
                        .POST(
                                HttpRequest.BodyPublishers.ofString(form)
                        )
                        .build();

        HttpResponse<String> response;

        try {
            response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "OAuth token request failed",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "OAuth token request was interrupted",
                    exception
            );
        }

        if (response.statusCode() != SUCCESS_STATUS_CODE) {
            throw new IllegalStateException(
                    "OAuth token request failed. HTTP "
                            + response.statusCode()
            );
        }

        JsonNode json = parseResponse(response.body());

        JsonNode accessToken =
                json.get(ACCESS_TOKEN_FIELD);

        if (accessToken == null || accessToken.isNull()) {
            throw new IllegalStateException(
                    "OAuth response did not contain access_token"
            );
        }

        LOG.info("OAuth token successfully received.");

        return accessToken.asText();
    }

    private JsonNode parseResponse(String responseBody) {

        try {
            return objectMapper.readTree(responseBody);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Unable to parse OAuth token response",
                    exception
            );
        }
    }

    private static String generateCodeVerifier() {
        return generateRandomValue(
                CODE_VERIFIER_RANDOM_BYTES
        );
    }

    private static String generateCodeChallenge(
            String codeVerifier) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            codeVerifier.getBytes(
                                    StandardCharsets.US_ASCII
                            )
                    );

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    exception
            );
        }
    }

    private static String generateRandomValue(
            int bytes) {

        byte[] randomBytes =
                new byte[bytes];

        SECURE_RANDOM.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    private static String encode(String value) {
        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }

    private static final class OAuthCallback {

        private final CountDownLatch callbackReceived;

        private String authorizationCode;

        private String returnedState;

        private OAuthCallback(
                CountDownLatch callbackReceived) {

            this.callbackReceived = callbackReceived;
        }

        private void handle(
                com.sun.net.httpserver.HttpExchange exchange)
                throws IOException {

            String query =
                    exchange.getRequestURI().getRawQuery();

            if (query != null) {
                processQueryParameters(query);
            }

            String response =
                    "BankFlow OAuth authorization received. "
                            + "You can close this browser window.";

            byte[] responseBytes =
                    response.getBytes(StandardCharsets.UTF_8);

            exchange.sendResponseHeaders(
                    SUCCESS_STATUS_CODE,
                    responseBytes.length
            );

            try (OutputStream outputStream =
                         exchange.getResponseBody()) {

                outputStream.write(responseBytes);
            } finally {
                callbackReceived.countDown();
            }
        }

        private void processQueryParameters(
                String query) {

            for (String parameter : query.split("&")) {

                String[] parts =
                        parameter.split("=", 2);

                if (parts.length != 2) {
                    continue;
                }

                String name = parts[0];
                String value = parts[1];

                if (AUTHORIZATION_CODE_PARAMETER.equals(name)) {
                    authorizationCode = value;
                } else if (STATE_PARAMETER.equals(name)) {
                    returnedState = value;
                }
            }
        }

        private String getAuthorizationCode() {
            return authorizationCode;
        }

        private String getReturnedState() {
            return returnedState;
        }
    }
}