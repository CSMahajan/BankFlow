package com.bankflow.specification;

import com.bankflow.entity.Account;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountSpecificationTest {

    @Test
    void search_shouldReturnConjunctionWhenSearchIsNull() {

        Root<Account> root = mock(Root.class);
        CriteriaQuery<Account> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(conjunction);

        Specification<Account> specification =
                AccountSpecification.search(null);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(conjunction, result);

        verify(cb).conjunction();
        verifyNoMoreInteractions(cb);
    }


    @Test
    void search_shouldReturnConjunctionWhenSearchIsBlank() {

        Root<Account> root = mock(Root.class);
        CriteriaQuery<Account> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(conjunction);

        Specification<Account> specification =
                AccountSpecification.search("   ");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(conjunction, result);

        verify(cb).conjunction();
        verifyNoMoreInteractions(cb);
    }


    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void search_shouldSearchAccountNumberAndFullName() {

        Root<Account> root = mock(Root.class);
        CriteriaQuery<Account> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<String> accountNumberPath = mock(Path.class);
        Path userPath = mock(Path.class);
        Path<String> fullNamePath = mock(Path.class);

        Predicate accountNumberPredicate = mock(Predicate.class);
        Predicate fullNamePredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        /*
         * Root#get(String) has generic return-type inference that can
         * cause Mockito's thenReturn(...) to fail compilation.
         *
         * doReturn(...) avoids that problem.
         */
        doReturn(accountNumberPath)
                .when(root)
                .get("accountNumber");

        doReturn(userPath)
                .when(root)
                .get("user");

        doReturn(fullNamePath)
                .when(userPath)
                .get("fullName");

        when(cb.lower(accountNumberPath))
                .thenReturn(accountNumberPath);

        when(cb.lower(fullNamePath))
                .thenReturn(fullNamePath);

        when(cb.like(
                accountNumberPath,
                "%1234%"
        )).thenReturn(accountNumberPredicate);

        when(cb.like(
                fullNamePath,
                "%1234%"
        )).thenReturn(fullNamePredicate);

        when(cb.or(
                accountNumberPredicate,
                fullNamePredicate
        )).thenReturn(combinedPredicate);

        Specification<Account> specification =
                AccountSpecification.search("  1234  ");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(combinedPredicate, result);

        verify(root).get("accountNumber");
        verify(root).get("user");
        verify(userPath).get("fullName");

        verify(cb).lower(accountNumberPath);
        verify(cb).lower(fullNamePath);

        verify(cb).like(
                accountNumberPath,
                "%1234%"
        );

        verify(cb).like(
                fullNamePath,
                "%1234%"
        );

        verify(cb).or(
                accountNumberPredicate,
                fullNamePredicate
        );
    }


    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void search_shouldTrimAndLowercaseSearchValue() {

        Root<Account> root = mock(Root.class);
        CriteriaQuery<Account> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<String> accountNumberPath = mock(Path.class);
        Path userPath = mock(Path.class);
        Path<String> fullNamePath = mock(Path.class);

        Predicate accountNumberPredicate = mock(Predicate.class);
        Predicate fullNamePredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        doReturn(accountNumberPath)
                .when(root)
                .get("accountNumber");

        doReturn(userPath)
                .when(root)
                .get("user");

        doReturn(fullNamePath)
                .when(userPath)
                .get("fullName");

        when(cb.lower(accountNumberPath))
                .thenReturn(accountNumberPath);

        when(cb.lower(fullNamePath))
                .thenReturn(fullNamePath);

        when(cb.like(
                accountNumberPath,
                "%raj%"
        )).thenReturn(accountNumberPredicate);

        when(cb.like(
                fullNamePath,
                "%raj%"
        )).thenReturn(fullNamePredicate);

        when(cb.or(
                accountNumberPredicate,
                fullNamePredicate
        )).thenReturn(combinedPredicate);

        Specification<Account> specification =
                AccountSpecification.search("  RAJ  ");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(combinedPredicate, result);

        verify(root).get("accountNumber");
        verify(root).get("user");
        verify(userPath).get("fullName");

        verify(cb).lower(accountNumberPath);
        verify(cb).lower(fullNamePath);

        verify(cb).like(
                accountNumberPath,
                "%raj%"
        );

        verify(cb).like(
                fullNamePath,
                "%raj%"
        );

        /*
         * Correct Mockito verification syntax:
         *
         * verify(mock).method(...)
         *
         * NOT:
         * verify(mock.method(...))
         */
        verify(cb).or(
                accountNumberPredicate,
                fullNamePredicate
        );
    }


    @Test
    void status_shouldReturnConjunctionWhenStatusIsNull() {

        Root<Account> root = mock(Root.class);
        CriteriaQuery<Account> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(conjunction);

        Specification<Account> specification =
                AccountSpecification.status(null);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(conjunction, result);

        verify(cb).conjunction();
        verifyNoMoreInteractions(cb);
    }


    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void status_shouldCreateEqualPredicateForGivenStatus() {

        Root<Account> root = mock(Root.class);
        CriteriaQuery<Account> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path statusPath = mock(Path.class);
        Predicate equalPredicate = mock(Predicate.class);

        Account.AccountStatus status =
                Account.AccountStatus.ACTIVE;

        doReturn(statusPath)
                .when(root)
                .get("accountStatus");

        when(cb.equal(
                statusPath,
                status
        )).thenReturn(equalPredicate);

        Specification<Account> specification =
                AccountSpecification.status(status);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(equalPredicate, result);

        verify(root).get("accountStatus");

        verify(cb).equal(
                statusPath,
                status
        );
    }
}