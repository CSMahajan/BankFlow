package com.bankflow.specification;

import com.bankflow.entity.Card;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CardSpecification {


    public static Specification<Card> search(String search) {

        return (root, query, cb) -> {

            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }

            String pattern =
                    "%" + search.trim().toLowerCase() + "%";


            return cb.or(
                    cb.like(
                            cb.lower(root.get("cardNumber")),
                            pattern
                    ),

                    cb.like(
                            cb.lower(
                                    root.get("account")
                                            .get("accountNumber")
                            ),
                            pattern
                    ),

                    cb.like(
                            cb.lower(
                                    root.get("account")
                                            .get("user")
                                            .get("fullName")
                            ),
                            pattern
                    )
            );
        };
    }


    public static Specification<Card> status(
            Card.CardStatus status) {

        return (root, query, cb) -> {

            if (status == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("cardStatus"),
                    status
            );
        };
    }
}