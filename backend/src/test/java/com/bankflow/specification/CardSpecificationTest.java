package com.bankflow.specification;

import com.bankflow.entity.Card;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class CardSpecificationTest {

    @Test
    void search_shouldReturnConjunctionWhenSearchIsNull() {

        Root<Card> root = mock(Root.class);
        CriteriaQuery<Card> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(conjunction);

        Specification<Card> specification =
                CardSpecification.search(null);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(conjunction, result);

        verify(cb).conjunction();
        verifyNoMoreInteractions(cb);
        verifyNoInteractions(root);
    }

    @Test
    void search_shouldReturnConjunctionWhenSearchIsBlank() {

        Root<Card> root = mock(Root.class);
        CriteriaQuery<Card> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(conjunction);

        Specification<Card> specification =
                CardSpecification.search("   ");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(conjunction, result);

        verify(cb).conjunction();
        verifyNoMoreInteractions(cb);
        verifyNoInteractions(root);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void search_shouldSearchCardNumberAccountNumberAndFullName() {

        Root<Card> root = mock(Root.class);
        CriteriaQuery<Card> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path cardNumberPath = mock(Path.class);
        Path accountPath = mock(Path.class);
        Path accountNumberPath = mock(Path.class);
        Path userPath = mock(Path.class);
        Path fullNamePath = mock(Path.class);

        Predicate cardNumberPredicate = mock(Predicate.class);
        Predicate accountNumberPredicate = mock(Predicate.class);
        Predicate fullNamePredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        /*
         * cardNumber
         */
        doReturn(cardNumberPath)
                .when(root)
                .get("cardNumber");

        /*
         * account.accountNumber
         */
        doReturn(accountPath)
                .when(root)
                .get("account");

        doReturn(accountNumberPath)
                .when(accountPath)
                .get("accountNumber");

        /*
         * account.user.fullName
         */
        doReturn(userPath)
                .when(accountPath)
                .get("user");

        doReturn(fullNamePath)
                .when(userPath)
                .get("fullName");

        /*
         * lower(...)
         */
        when(cb.lower(cardNumberPath))
                .thenReturn(cardNumberPath);

        when(cb.lower(accountNumberPath))
                .thenReturn(accountNumberPath);

        when(cb.lower(fullNamePath))
                .thenReturn(fullNamePath);

        /*
         * like(...)
         */
        when(cb.like(
                cardNumberPath,
                "%1234%"
        )).thenReturn(cardNumberPredicate);

        when(cb.like(
                accountNumberPath,
                "%1234%"
        )).thenReturn(accountNumberPredicate);

        when(cb.like(
                fullNamePath,
                "%1234%"
        )).thenReturn(fullNamePredicate);

        /*
         * or(...)
         */
        when(cb.or(
                cardNumberPredicate,
                accountNumberPredicate,
                fullNamePredicate
        )).thenReturn(combinedPredicate);

        Specification<Card> specification =
                CardSpecification.search("  1234  ");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(combinedPredicate, result);

        /*
         * Verify path traversal
         */
        verify(root).get("cardNumber");
        verify(root, times(2)).get("account");

        verify(accountPath).get("accountNumber");
        verify(accountPath).get("user");

        verify(userPath).get("fullName");

        /*
         * Verify lower()
         */
        verify(cb).lower(cardNumberPath);
        verify(cb).lower(accountNumberPath);
        verify(cb).lower(fullNamePath);

        /*
         * Verify like()
         */
        verify(cb).like(
                cardNumberPath,
                "%1234%"
        );

        verify(cb).like(
                accountNumberPath,
                "%1234%"
        );

        verify(cb).like(
                fullNamePath,
                "%1234%"
        );

        /*
         * Verify OR combination
         */
        verify(cb).or(
                cardNumberPredicate,
                accountNumberPredicate,
                fullNamePredicate
        );
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void search_shouldTrimAndLowercaseSearchValue() {

        Root<Card> root = mock(Root.class);
        CriteriaQuery<Card> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path cardNumberPath = mock(Path.class);
        Path accountPath = mock(Path.class);
        Path accountNumberPath = mock(Path.class);
        Path userPath = mock(Path.class);
        Path fullNamePath = mock(Path.class);

        Predicate cardNumberPredicate = mock(Predicate.class);
        Predicate accountNumberPredicate = mock(Predicate.class);
        Predicate fullNamePredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        /*
         * Path traversal
         */
        doReturn(cardNumberPath)
                .when(root)
                .get("cardNumber");

        doReturn(accountPath)
                .when(root)
                .get("account");

        doReturn(accountNumberPath)
                .when(accountPath)
                .get("accountNumber");

        doReturn(userPath)
                .when(accountPath)
                .get("user");

        doReturn(fullNamePath)
                .when(userPath)
                .get("fullName");

        /*
         * lower()
         */
        when(cb.lower(cardNumberPath))
                .thenReturn(cardNumberPath);

        when(cb.lower(accountNumberPath))
                .thenReturn(accountNumberPath);

        when(cb.lower(fullNamePath))
                .thenReturn(fullNamePath);

        /*
         * like()
         *
         * "  RAJ  " should become "%raj%"
         */
        when(cb.like(
                cardNumberPath,
                "%raj%"
        )).thenReturn(cardNumberPredicate);

        when(cb.like(
                accountNumberPath,
                "%raj%"
        )).thenReturn(accountNumberPredicate);

        when(cb.like(
                fullNamePath,
                "%raj%"
        )).thenReturn(fullNamePredicate);

        when(cb.or(
                cardNumberPredicate,
                accountNumberPredicate,
                fullNamePredicate
        )).thenReturn(combinedPredicate);

        Specification<Card> specification =
                CardSpecification.search("  RAJ  ");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(combinedPredicate, result);

        verify(cb).like(
                cardNumberPath,
                "%raj%"
        );

        verify(cb).like(
                accountNumberPath,
                "%raj%"
        );

        verify(cb).like(
                fullNamePath,
                "%raj%"
        );

        verify(cb).or(
                cardNumberPredicate,
                accountNumberPredicate,
                fullNamePredicate
        );
    }

    @Test
    void status_shouldReturnConjunctionWhenStatusIsNull() {

        Root<Card> root = mock(Root.class);
        CriteriaQuery<Card> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(conjunction);

        Specification<Card> specification =
                CardSpecification.status(null);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(conjunction, result);

        verify(cb).conjunction();
        verifyNoMoreInteractions(cb);
        verifyNoInteractions(root);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void status_shouldCreateEqualPredicateForGivenStatus() {

        Root<Card> root = mock(Root.class);
        CriteriaQuery<Card> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path statusPath = mock(Path.class);
        Predicate equalPredicate = mock(Predicate.class);

        Card.CardStatus status =
                Card.CardStatus.ACTIVE;

        doReturn(statusPath)
                .when(root)
                .get("cardStatus");

        when(cb.equal(
                statusPath,
                status
        )).thenReturn(equalPredicate);

        Specification<Card> specification =
                CardSpecification.status(status);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(equalPredicate, result);

        verify(root).get("cardStatus");

        verify(cb).equal(
                statusPath,
                status
        );
    }
}