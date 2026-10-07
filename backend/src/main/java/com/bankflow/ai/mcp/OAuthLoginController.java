package com.bankflow.ai.mcp;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OAuthLoginController {

    @GetMapping("/oauth/login")
    public String login() {
        return "oauth/oauth-login";
    }
}