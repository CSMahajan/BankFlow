package com.bankflow.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class OAuthProtectedResourceController {

    @Value("${oauth2.issuer}")
    private String oauth2Issuer;

    @GetMapping({
            "/.well-known/oauth-protected-resource",
            "/.well-known/oauth-protected-resource/mcp"
    })
    public Map<String, Object> protectedResourceMetadata() {
        return Map.of(
                "resource", oauth2Issuer + "/mcp",
                "authorization_servers", new String[]{oauth2Issuer},
                "bearer_methods_supported", new String[]{"header"},
                "scopes_supported", new String[]{
                        "openid",
                        "profile",
                        "email"
                }
        );
    }
}