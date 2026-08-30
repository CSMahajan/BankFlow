package com.bankflow.specification;

import com.bankflow.entity.Transaction;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class TransactionSpecificationTest {

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void belongsToUser_shouldCreateUserIdPredicate() {

        Root<Transaction> root = mock(Root.class);
        CriteriaQuery<Transaction> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path accountPath = mock(Path.class);
        Path userPath = mock(Path.class);
        Path userIdPath = mock(Path.class);

        Predicate predicate = mock(Predicate.class);

        doReturn(accountPath)
                .when(root)
                .get("account");

        doReturn(userPath)
                .when(accountPath)
                .get("user");

        doReturn(userIdPath)
                .when(userPath)
                .get("id");

        when(cb.equal(userIdPath, 123L))
                .thenReturn(predicate);

        Specification<Transaction> specification =
                TransactionSpecification.belongsToUser(123L);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(predicate, result);

        verify(root).get("account");
        verify(accountPath).get("user");
        verify(userPath).get("id");

        verify(cb).equal(userIdPath, 123L);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void accountNumber_shouldReturnConjunctionWhenAccountNumberIsNull() {

        Root<Transaction> root = mock(Root.class);
        CriteriaQuery<Transaction> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(conjunction);

        Specification<Transaction> specification =
                TransactionSpecification.accountNumber(null);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(conjunction, result);

        verify(cb).conjunction();
        verifyNoMoreInteractions(cb);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void accountNumber_shouldReturnConjunctionWhenAccountNumberIsBlank() {

        Root<Transaction> root = mock(Root.class);
        CriteriaQuery<Transaction> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(conjunction);

        Specification<Transaction> specification =
                TransactionSpecification.accountNumber("   ");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(conjunction, result);

        verify(cb).conjunction();
        verifyNoMoreInteractions(cb);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void accountNumber_shouldCreateEqualPredicate() {

        Root<Transaction> root = mock(Root.class);
        CriteriaQuery<Transaction> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path accountPath = mock(Path.class);
        Path accountNumberPath = mock(Path.class);

        Predicate predicate = mock(Predicate.class);

        doReturn(accountPath)
                .when(root)
                .get("account");

        doReturn(accountNumberPath)
                .when(accountPath)
                .get("accountNumber");

        when(cb.equal(accountNumberPath, "1234567890"))
                .thenReturn(predicate);

        Specification<Transaction> specification =
                TransactionSpecification.accountNumber("1234567890");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(predicate, result);

        verify(root).get("account");
        verify(accountPath).get("accountNumber");

        verify(cb).equal(
                accountNumberPath,
                "1234567890"
        );
    }

    @Test
    void transactionType_shouldReturnConjunctionWhenTypeIsNull() {

        Root<Transaction> root = mock(Root.class);
        CriteriaQuery<Transaction> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(conjunction);

        Specification<Transaction> specification =
                TransactionSpecification.transactionType(null);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(conjunction, result);

        verify(cb).conjunction();
        verifyNoMoreInteractions(cb);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void transactionType_shouldCreateEqualPredicate() {

        Root<Transaction> root = mock(Root.class);
        CriteriaQuery<Transaction> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path transactionTypePath = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        Transaction.TransactionType type =
                Transaction.TransactionType.values()[0];

        doReturn(transactionTypePath)
                .when(root)
                .get("transactionType");

        when(cb.equal(transactionTypePath, type))
                .thenReturn(predicate);

        Specification<Transaction> specification =
                TransactionSpecification.transactionType(type);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(predicate, result);

        verify(root).get("transactionType");

        verify(cb).equal(
                transactionTypePath,
                type
        );
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void dateRange_shouldCreateBetweenPredicate() {

        Root<Transaction> root = mock(Root.class);
        CriteriaQuery<Transaction> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path transactionDatePath = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        LocalDate startDate =
                LocalDate.of(2026, 1, 10);

        LocalDate endDate =
                LocalDate.of(2026, 1, 15);

        LocalDateTime expectedStart =
                startDate.atStartOfDay();

        LocalDateTime expectedEnd =
                endDate.atTime(LocalTime.MAX);

        doReturn(transactionDatePath)
                .when(root)
                .get("transactionDate");

        when(cb.between(
                transactionDatePath,
                expectedStart,
                expectedEnd
        )).thenReturn(predicate);

        Specification<Transaction> specification =
                TransactionSpecification.dateRange(
                        startDate,
                        endDate
                );

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(predicate, result);

        verify(root).get("transactionDate");

        verify(cb).between(
                transactionDatePath,
                expectedStart,
                expectedEnd
        );
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void search_shouldReturnConjunctionWhenSearchIsNull() {

        Root<Transaction> root = mock(Root.class);
        CriteriaQuery<Transaction> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(conjunction);

        Specification<Transaction> specification =
                TransactionSpecification.search(null);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(conjunction, result);

        verify(cb).conjunction();
        verifyNoMoreInteractions(cb);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void search_shouldReturnConjunctionWhenSearchIsBlank() {

        Root<Transaction> root = mock(Root.class);
        CriteriaQuery<Transaction> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(conjunction);

        Specification<Transaction> specification =
                TransactionSpecification.search("   ");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(conjunction, result);

        verify(cb).conjunction();
        verifyNoMoreInteractions(cb);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void search_shouldSearchTransactionIdAndDescription() {

        Root<Transaction> root = mock(Root.class);
        CriteriaQuery<Transaction> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<String> transactionIdPath = mock(Path.class);
        Path<String> descriptionPath = mock(Path.class);

        Predicate transactionIdPredicate = mock(Predicate.class);
        Predicate descriptionPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        doReturn(transactionIdPath)
                .when(root)
                .get("transactionId");

        doReturn(descriptionPath)
                .when(root)
                .get("description");

        when(cb.lower(transactionIdPath))
                .thenReturn(transactionIdPath);

        when(cb.lower(descriptionPath))
                .thenReturn(descriptionPath);

        when(cb.like(
                transactionIdPath,
                "%abc123%"
        )).thenReturn(transactionIdPredicate);

        when(cb.like(
                descriptionPath,
                "%abc123%"
        )).thenReturn(descriptionPredicate);

        when(cb.or(
                transactionIdPredicate,
                descriptionPredicate
        )).thenReturn(combinedPredicate);

        Specification<Transaction> specification =
                TransactionSpecification.search("  ABC123  ");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(combinedPredicate, result);

        verify(root).get("transactionId");
        verify(root).get("description");

        verify(cb).lower(transactionIdPath);
        verify(cb).lower(descriptionPath);

        verify(cb).like(
                transactionIdPath,
                "%abc123%"
        );

        verify(cb).like(
                descriptionPath,
                "%abc123%"
        );

        verify(cb).or(
                transactionIdPredicate,
                descriptionPredicate
        );
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void search_shouldTrimAndLowercaseSearchValue() {

        Root<Transaction> root = mock(Root.class);
        CriteriaQuery<Transaction> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<String> transactionIdPath = mock(Path.class);
        Path<String> descriptionPath = mock(Path.class);

        Predicate transactionIdPredicate = mock(Predicate.class);
        Predicate descriptionPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        doReturn(transactionIdPath)
                .when(root)
                .get("transactionId");

        doReturn(descriptionPath)
                .when(root)
                .get("description");

        when(cb.lower(transactionIdPath))
                .thenReturn(transactionIdPath);

        when(cb.lower(descriptionPath))
                .thenReturn(descriptionPath);

        when(cb.like(
                transactionIdPath,
                "%raj%"
        )).thenReturn(transactionIdPredicate);

        when(cb.like(
                descriptionPath,
                "%raj%"
        )).thenReturn(descriptionPredicate);

        when(cb.or(
                transactionIdPredicate,
                descriptionPredicate
        )).thenReturn(combinedPredicate);

        Specification<Transaction> specification =
                TransactionSpecification.search("  RAJ  ");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(combinedPredicate, result);

        verify(cb).like(
                transactionIdPath,
                "%raj%"
        );

        verify(cb).like(
                descriptionPath,
                "%raj%"
        );
    }
}