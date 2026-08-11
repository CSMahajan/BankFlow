package com.bankflow.specification;

import com.bankflow.entity.Account;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountSpecification {

    public static Specification<Account> search(String search) {

        return (root, query, cb) -> {

            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }

            String pattern = "%" + search.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(
                            cb.lower(root.get("accountNumber")),
                            pattern
                    ),
                    cb.like(
                            cb.lower(root.get("user").get("fullName")),
                            pattern
                    )
            );
        };
    }

    public static Specification<Account> status(
            Account.AccountStatus status) {

        return (root, query, cb) -> {

            if (status == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("accountStatus"),
                    status
            );
        };
    }
}