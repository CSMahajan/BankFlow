package com.bankflow.specification;

import com.bankflow.entity.KycDocument;
import org.springframework.data.jpa.domain.Specification;

public class KycDocumentSpecification {


    public static Specification<KycDocument> status(
            KycDocument.KycVerificationStatus status) {

        return (root, query, cb) -> {

            if(status == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("kycVerificationStatus"),
                    status
            );
        };
    }


    public static Specification<KycDocument> search(
            String search) {


        return (root, query, cb) -> {

            if(search == null || search.isBlank()) {
                return cb.conjunction();
            }


            String pattern =
                    "%" + search.toLowerCase() + "%";


            return cb.or(

                    cb.like(
                            cb.lower(
                                    root.get("user")
                                            .get("fullName")
                            ),
                            pattern
                    ),

                    cb.like(
                            cb.lower(
                                    root.get("user")
                                            .get("email")
                            ),
                            pattern
                    )

            );
        };
    }
}