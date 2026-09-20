package com.bankflow.ai.tool;

import com.bankflow.dto.AccountResponse;
import com.bankflow.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetMyAccountsTool {

    public static final String NAME = "get_my_accounts";

    private final AccountService accountService;

    public List<AccountResponse> execute() {
        return accountService.getMyAccounts();
    }
}