package com.bankflow.specification;

import com.bankflow.entity.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class UserSpecificationTest {

    @Test
    void search_shouldReturnConjunctionWhenSearchIsNull() {

        Root<User> root = mock(Root.class);
        CriteriaQuery<User> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(conjunction);

        Specification<User> specification =
                UserSpecification.search(null);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(conjunction, result);

        verify(cb).conjunction();
        verifyNoMoreInteractions(cb);
    }

    @Test
    void search_shouldReturnConjunctionWhenSearchIsBlank() {

        Root<User> root = mock(Root.class);
        CriteriaQuery<User> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(conjunction);

        Specification<User> specification =
                UserSpecification.search("   ");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(conjunction, result);

        verify(cb).conjunction();
        verifyNoMoreInteractions(cb);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void search_shouldSearchFullNameAndEmail() {

        Root<User> root = mock(Root.class);
        CriteriaQuery<User> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<String> fullNamePath = mock(Path.class);
        Path<String> emailPath = mock(Path.class);

        Predicate fullNamePredicate = mock(Predicate.class);
        Predicate emailPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        doReturn(fullNamePath)
                .when(root)
                .get("fullName");

        doReturn(emailPath)
                .when(root)
                .get("email");

        when(cb.lower(fullNamePath))
                .thenReturn(fullNamePath);

        when(cb.lower(emailPath))
                .thenReturn(emailPath);

        when(cb.like(
                fullNamePath,
                "%raj%"
        )).thenReturn(fullNamePredicate);

        when(cb.like(
                emailPath,
                "%raj%"
        )).thenReturn(emailPredicate);

        when(cb.or(
                fullNamePredicate,
                emailPredicate
        )).thenReturn(combinedPredicate);

        Specification<User> specification =
                UserSpecification.search("  RAJ  ");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(combinedPredicate, result);

        verify(root).get("fullName");
        verify(root).get("email");

        verify(cb).lower(fullNamePath);
        verify(cb).lower(emailPath);

        verify(cb).like(
                fullNamePath,
                "%raj%"
        );

        verify(cb).like(
                emailPath,
                "%raj%"
        );

        verify(cb).or(
                fullNamePredicate,
                emailPredicate
        );
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void search_shouldTrimAndLowercaseSearchValue() {

        Root<User> root = mock(Root.class);
        CriteriaQuery<User> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<String> fullNamePath = mock(Path.class);
        Path<String> emailPath = mock(Path.class);

        Predicate fullNamePredicate = mock(Predicate.class);
        Predicate emailPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        doReturn(fullNamePath)
                .when(root)
                .get("fullName");

        doReturn(emailPath)
                .when(root)
                .get("email");

        when(cb.lower(fullNamePath))
                .thenReturn(fullNamePath);

        when(cb.lower(emailPath))
                .thenReturn(emailPath);

        when(cb.like(
                fullNamePath,
                "%chaitanya%"
        )).thenReturn(fullNamePredicate);

        when(cb.like(
                emailPath,
                "%chaitanya%"
        )).thenReturn(emailPredicate);

        when(cb.or(
                fullNamePredicate,
                emailPredicate
        )).thenReturn(combinedPredicate);

        Specification<User> specification =
                UserSpecification.search("  ChAiTaNyA  ");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(combinedPredicate, result);

        verify(cb).like(
                fullNamePath,
                "%chaitanya%"
        );

        verify(cb).like(
                emailPath,
                "%chaitanya%"
        );
    }

    @Test
    void role_shouldReturnConjunctionWhenRoleIsNull() {

        Root<User> root = mock(Root.class);
        CriteriaQuery<User> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(conjunction);

        Specification<User> specification =
                UserSpecification.role(null);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(conjunction, result);

        verify(cb).conjunction();
        verifyNoMoreInteractions(cb);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void role_shouldCreateEqualPredicateForGivenRole() {

        Root<User> root = mock(Root.class);
        CriteriaQuery<User> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path rolePath = mock(Path.class);
        Predicate equalPredicate = mock(Predicate.class);

        User.Role role =
                User.Role.values()[0];

        doReturn(rolePath)
                .when(root)
                .get("role");

        when(cb.equal(
                rolePath,
                role
        )).thenReturn(equalPredicate);

        Specification<User> specification =
                UserSpecification.role(role);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(equalPredicate, result);

        verify(root).get("role");

        verify(cb).equal(
                rolePath,
                role
        );
    }
}