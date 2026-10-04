package com.bankflow.mcpclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;

public class OAuthClient {

    private static final Logger log =
            LoggerFactory.getLogger(OAuthClient.class);

    private static final String BANKFLOW_BASE_URL =
            System.getenv().getOrDefault(
                    "BANKFLOW_BASE_URL",
                    "http://localhost:8080"
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

    private final HttpClient httpClient =
            HttpClient.newHttpClient();

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    public String authorize() throws Exception {

        String codeVerifier =
                generateCodeVerifier();

        String codeChallenge =
                generateCodeChallenge(codeVerifier);

        String state =
                generateRandomValue(32);

        CountDownLatch callbackReceived =
                new CountDownLatch(1);

        String[] authorizationCode =
                new String[1];

        String[] returnedState =
                new String[1];

        HttpServer callbackServer =
                HttpServer.create(
                        new InetSocketAddress("localhost", 3334),
                        0
                );

        callbackServer.createContext(
                "/oauth/callback",
                exchange -> {

                    String query =
                            exchange.getRequestURI().getRawQuery();

                    if (query != null) {

                        for (String parameter : query.split("&")) {

                            String[] parts =
                                    parameter.split("=", 2);

                            if (parts.length != 2) {
                                continue;
                            }

                            String name = parts[0];
                            String value = parts[1];

                            if ("code".equals(name)) {
                                authorizationCode[0] = value;
                            }

                            if ("state".equals(name)) {
                                returnedState[0] = value;
                            }
                        }
                    }

                    String response =
                            "BankFlow OAuth authorization received. "
                                    + "You can close this browser window.";

                    byte[] responseBytes =
                            response.getBytes(StandardCharsets.UTF_8);

                    exchange.sendResponseHeaders(
                            200,
                            responseBytes.length
                    );

                    try (OutputStream outputStream =
                                 exchange.getResponseBody()) {

                        outputStream.write(responseBytes);
                    }

                    callbackReceived.countDown();
                }
        );

        callbackServer.start();

        String authorizationUrl =
                AUTHORIZATION_ENDPOINT
                        + "?response_type=code"
                        + "&client_id=" + encode(CLIENT_ID)
                        + "&code_challenge=" + encode(codeChallenge)
                        + "&code_challenge_method=S256"
                        + "&redirect_uri=" + encode(REDIRECT_URI)
                        + "&state=" + encode(state)
                        + "&scope=" + encode("openid profile email")
                        + "&resource=" + encode(MCP_ENDPOINT);

        log.info(
                "Opening BankFlow OAuth authorization..."
        );

        log.info(
                "Authorization URL: {}",
                authorizationUrl
        );

        log.info(
                "Waiting for OAuth callback on port 3334..."
        );

        callbackReceived.await();

        callbackServer.stop(0);

        if (!state.equals(returnedState[0])) {

            throw new IllegalStateException(
                    "OAuth state validation failed"
            );
        }

        if (authorizationCode[0] == null) {

            throw new IllegalStateException(
                    "OAuth authorization code was not received"
            );
        }

        return exchangeAuthorizationCode(
                authorizationCode[0],
                codeVerifier
        );
    }

    private String exchangeAuthorizationCode(
            String authorizationCode,
            String codeVerifier
    ) throws Exception {

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
                                "Content-Type",
                                "application/x-www-form-urlencoded"
                        )
                        .POST(
                                HttpRequest.BodyPublishers.ofString(form)
                        )
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() != 200) {

            throw new IllegalStateException(
                    "OAuth token request failed. HTTP "
                            + response.statusCode()
                            + ": "
                            + response.body()
            );
        }

        JsonNode json =
                objectMapper.readTree(response.body());

        JsonNode accessToken =
                json.get("access_token");

        if (accessToken == null || accessToken.isNull()) {

            throw new IllegalStateException(
                    "OAuth response did not contain access_token"
            );
        }

        log.info("OAuth token successfully received.");

        return accessToken.asText();
    }

    private static String generateCodeVerifier() {

        return generateRandomValue(64);
    }

    private static String generateCodeChallenge(
            String codeVerifier
    ) throws Exception {

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
    }

    private static String generateRandomValue(
            int bytes
    ) {

        byte[] randomBytes =
                new byte[bytes];

        new SecureRandom()
                .nextBytes(randomBytes);

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
}