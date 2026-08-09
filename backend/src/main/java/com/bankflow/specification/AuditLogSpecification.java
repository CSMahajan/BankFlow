package com.bankflow.specification;

import com.bankflow.entity.AuditAction;
import com.bankflow.entity.AuditLog;
import com.bankflow.entity.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuditLogSpecification {

    public static Specification<AuditLog> search(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("performedBy")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<AuditLog> role(User.Role role) {
        return (root, query, cb) -> {
            if (role == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("role"), role);
        };
    }

    public static Specification<AuditLog> action(AuditAction action) {
        return (root, query, cb) -> {
            if (action == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("action"), action);
        };
    }

    public static Specification<AuditLog> actions(
            java.util.List<AuditAction> actions) {
        return (root, query, cb) -> {
            if (actions == null || actions.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("action").in(actions);
        };
    }
}