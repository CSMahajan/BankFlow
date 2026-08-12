package com.bankflow.specification;

import com.bankflow.entity.Loan;
import org.springframework.data.jpa.domain.Specification;

public class LoanSpecification {

    public static Specification<Loan> status(Loan.LoanStatus status) {
        return (root, query, cb) -> {

            if (status == null) {
                return null;
            }

            return cb.equal(
                    root.get("status"),
                    status
            );
        };
    }


    public static Specification<Loan> search(String search) {
        return (root, query, cb) -> {

            if (search == null || search.isBlank()) {
                return null;
            }

            String value = "%" + search.toLowerCase() + "%";

            return cb.or(
                    cb.like(
                            cb.lower(root.get("loanNumber")),
                            value
                    ),
                    cb.like(
                            cb.lower(root.get("user").get("fullName")),
                            value
                    ),
                    cb.like(
                            cb.lower(root.get("disbursementAccount").get("accountNumber")),
                            value
                    )
            );
        };
    }


    public static Specification<Loan> loanType(Loan.LoanType loanType) {

        return (root, query, cb) -> {

            if (loanType == null) {
                return null;
            }

            return cb.equal(
                    root.get("loanType"),
                    loanType
            );
        };
    }
}