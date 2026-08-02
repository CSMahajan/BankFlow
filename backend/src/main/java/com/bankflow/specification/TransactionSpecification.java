package com.bankflow.specification;

import com.bankflow.entity.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TransactionSpecification {

    public static Specification<Transaction> belongsToUser(Long userId) {
        return (root, query, cb) ->
                cb.equal(root.get("account").get("user").get("id"), userId);
    }

    public static Specification<Transaction> accountNumber(String accountNumber) {
        return (root, query, cb) ->
                cb.equal(root.get("account").get("accountNumber"), accountNumber);
    }

    public static Specification<Transaction> transactionType(Transaction.TransactionType type) {
        return (root, query, cb) ->
                cb.equal(root.get("transactionType"), type);
    }

    public static Specification<Transaction> dateRange(
            LocalDate startDate,
            LocalDate endDate) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        return (root, query, cb) ->
                cb.between(root.get("transactionDate"), start, end);
    }
}