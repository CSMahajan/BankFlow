package com.bankflow.specification;

import com.bankflow.entity.Transaction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionSpecification {

    public static Specification<Transaction> belongsToUser(Long userId) {
        return (root, query, cb) ->
                cb.equal(root.get("account").get("user").get("id"), userId);
    }

    public static Specification<Transaction> accountNumber(String accountNumber) {
        return (root, query, cb) -> {
            if (accountNumber == null || accountNumber.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("account").get("accountNumber"), accountNumber
            );
        };
    }

    public static Specification<Transaction> transactionType(
            Transaction.TransactionType type) {
        return (root, query, cb) -> {
            if (type == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("transactionType"), type);
        };
    }

    public static Specification<Transaction> dateRange(
            LocalDate startDate,
            LocalDate endDate) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        return (root, query, cb) ->
                cb.between(root.get("transactionDate"), start, end);
    }

    public static Specification<Transaction> search(String search) {

        return (root, query, cb) -> {

            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }

            String pattern = "%" + search.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("transactionId")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }
}