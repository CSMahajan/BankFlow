package com.bankflow.specification;

import com.bankflow.entity.AuditAction;
import com.bankflow.entity.AuditLog;
import com.bankflow.entity.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditLogSpecificationTest {

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void search_shouldReturnConjunctionWhenSearchIsNull() {

        Root<AuditLog> root = mock(Root.class);
        CriteriaQuery<AuditLog> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(conjunction);

        Specification<AuditLog> specification =
                AuditLogSpecification.search(null);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(conjunction, result);

        verify(cb).conjunction();
        verifyNoMoreInteractions(cb);
    }


    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void search_shouldReturnConjunctionWhenSearchIsBlank() {

        Root<AuditLog> root = mock(Root.class);
        CriteriaQuery<AuditLog> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(conjunction);

        Specification<AuditLog> specification =
                AuditLogSpecification.search("   ");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(conjunction, result);

        verify(cb).conjunction();
        verifyNoMoreInteractions(cb);
    }


    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void search_shouldSearchPerformedByAndDescription() {

        Root<AuditLog> root = mock(Root.class);
        CriteriaQuery<AuditLog> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<String> performedByPath = mock(Path.class);
        Path<String> descriptionPath = mock(Path.class);

        Predicate performedByPredicate = mock(Predicate.class);
        Predicate descriptionPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        doReturn(performedByPath)
                .when(root)
                .get("performedBy");

        doReturn(descriptionPath)
                .when(root)
                .get("description");

        when(cb.lower(performedByPath))
                .thenReturn(performedByPath);

        when(cb.lower(descriptionPath))
                .thenReturn(descriptionPath);

        when(cb.like(
                performedByPath,
                "%login%"
        )).thenReturn(performedByPredicate);

        when(cb.like(
                descriptionPath,
                "%login%"
        )).thenReturn(descriptionPredicate);

        when(cb.or(
                performedByPredicate,
                descriptionPredicate
        )).thenReturn(combinedPredicate);

        Specification<AuditLog> specification =
                AuditLogSpecification.search("  LOGIN  ");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(combinedPredicate, result);

        verify(root).get("performedBy");
        verify(root).get("description");

        verify(cb).lower(performedByPath);
        verify(cb).lower(descriptionPath);

        verify(cb).like(
                performedByPath,
                "%login%"
        );

        verify(cb).like(
                descriptionPath,
                "%login%"
        );

        verify(cb).or(
                performedByPredicate,
                descriptionPredicate
        );
    }


    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void role_shouldReturnConjunctionWhenRoleIsNull() {

        Root<AuditLog> root = mock(Root.class);
        CriteriaQuery<AuditLog> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(conjunction);

        Specification<AuditLog> specification =
                AuditLogSpecification.role(null);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(conjunction, result);

        verify(cb).conjunction();
        verifyNoMoreInteractions(cb);
    }


    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void role_shouldCreateEqualPredicateForGivenRole() {

        Root<AuditLog> root = mock(Root.class);
        CriteriaQuery<AuditLog> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path rolePath = mock(Path.class);
        Predicate equalPredicate = mock(Predicate.class);

        User.Role role = User.Role.ADMIN;

        doReturn(rolePath)
                .when(root)
                .get("role");

        when(cb.equal(
                rolePath,
                role
        )).thenReturn(equalPredicate);

        Specification<AuditLog> specification =
                AuditLogSpecification.role(role);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(equalPredicate, result);

        verify(root).get("role");

        verify(cb).equal(
                rolePath,
                role
        );
    }


    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void action_shouldReturnConjunctionWhenActionIsNull() {

        Root<AuditLog> root = mock(Root.class);
        CriteriaQuery<AuditLog> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(conjunction);

        Specification<AuditLog> specification =
                AuditLogSpecification.action(null);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(conjunction, result);

        verify(cb).conjunction();
        verifyNoMoreInteractions(cb);
    }


    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void action_shouldCreateEqualPredicateForGivenAction() {

        Root<AuditLog> root = mock(Root.class);
        CriteriaQuery<AuditLog> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path actionPath = mock(Path.class);
        Predicate equalPredicate = mock(Predicate.class);

        AuditAction action =
                AuditAction.LOGIN;

        doReturn(actionPath)
                .when(root)
                .get("action");

        when(cb.equal(
                actionPath,
                action
        )).thenReturn(equalPredicate);

        Specification<AuditLog> specification =
                AuditLogSpecification.action(action);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(equalPredicate, result);

        verify(root).get("action");

        verify(cb).equal(
                actionPath,
                action
        );
    }


    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void actions_shouldReturnConjunctionWhenActionsAreNull() {

        Root<AuditLog> root = mock(Root.class);
        CriteriaQuery<AuditLog> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(conjunction);

        Specification<AuditLog> specification =
                AuditLogSpecification.actions(null);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(conjunction, result);

        verify(cb).conjunction();
        verifyNoMoreInteractions(cb);
    }


    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void actions_shouldReturnConjunctionWhenActionsAreEmpty() {

        Root<AuditLog> root = mock(Root.class);
        CriteriaQuery<AuditLog> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(conjunction);

        Specification<AuditLog> specification =
                AuditLogSpecification.actions(List.of());

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(conjunction, result);

        verify(cb).conjunction();
        verifyNoMoreInteractions(cb);
    }


    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void actions_shouldCreateInPredicateForGivenActions() {

        Root<AuditLog> root = mock(Root.class);
        CriteriaQuery<AuditLog> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path actionPath = mock(Path.class);
        Predicate inPredicate = mock(Predicate.class);

        List<AuditAction> actions = List.of(
                AuditAction.LOGIN
        );

        doReturn(actionPath)
                .when(root)
                .get("action");

        when(actionPath.in(actions))
                .thenReturn(inPredicate);

        Specification<AuditLog> specification =
                AuditLogSpecification.actions(actions);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(inPredicate, result);

        verify(root).get("action");

        verify(actionPath).in(actions);

        verifyNoInteractions(cb);
    }
}