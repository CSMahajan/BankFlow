package com.bankflow.specification;

import com.bankflow.entity.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSpecification {


    public static Specification<User> search(String search) {

        return (root, query, cb) -> {

            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }

            String pattern =
                    "%" + search.trim().toLowerCase() + "%";


            return cb.or(

                    cb.like(
                            cb.lower(root.get("fullName")),
                            pattern
                    ),

                    cb.like(
                            cb.lower(root.get("email")),
                            pattern
                    )
            );
        };
    }


    public static Specification<User> role(
            User.Role role) {

        return (root, query, cb) -> {

            if (role == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("role"),
                    role
            );
        };
    }
}