package com.bankflow.ai.tool;

import com.bankflow.dto.TransactionResponse;
import com.bankflow.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetMyTransactionsTool {

    public static final String NAME = "get_my_transactions";

    private final TransactionService transactionService;

    public List<TransactionResponse> execute() {

        List<TransactionResponse> transactions =
                transactionService.getMyTransactions(
                                null,
                                null,
                                null,
                                null,
                                null,
                                PageRequest.of(
                                        0,
                                        20,
                                        Sort.by(
                                                Sort.Direction.DESC,
                                                "transactionDate"
                                        )
                                )
                        )
                        .getContent();

        log.info(
                "get_my_transactions tool returned {} transactions: {}",
                transactions.size(),
                transactions
        );

        return transactions;
    }
}