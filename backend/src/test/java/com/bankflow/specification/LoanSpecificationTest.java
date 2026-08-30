package com.bankflow.specification;

import com.bankflow.entity.Loan;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class LoanSpecificationTest {

    @Test
    void status_shouldReturnNullWhenStatusIsNull() {

        Root<Loan> root = mock(Root.class);
        CriteriaQuery<Loan> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Specification<Loan> specification =
                LoanSpecification.status(null);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertNull(result);

        verifyNoInteractions(root);
        verifyNoInteractions(cb);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void status_shouldCreateEqualPredicateForGivenStatus() {

        Root<Loan> root = mock(Root.class);
        CriteriaQuery<Loan> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path statusPath = mock(Path.class);
        Predicate equalPredicate = mock(Predicate.class);

        Loan.LoanStatus status =
                Loan.LoanStatus.ACTIVE;

        doReturn(statusPath)
                .when(root)
                .get("status");

        when(cb.equal(
                statusPath,
                status
        )).thenReturn(equalPredicate);

        Specification<Loan> specification =
                LoanSpecification.status(status);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(equalPredicate, result);

        verify(root).get("status");

        verify(cb).equal(
                statusPath,
                status
        );
    }

    @Test
    void search_shouldReturnNullWhenSearchIsNull() {

        Root<Loan> root = mock(Root.class);
        CriteriaQuery<Loan> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Specification<Loan> specification =
                LoanSpecification.search(null);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertNull(result);

        verifyNoInteractions(root);
        verifyNoInteractions(cb);
    }

    @Test
    void search_shouldReturnNullWhenSearchIsBlank() {

        Root<Loan> root = mock(Root.class);
        CriteriaQuery<Loan> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Specification<Loan> specification =
                LoanSpecification.search("   ");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertNull(result);

        verifyNoInteractions(root);
        verifyNoInteractions(cb);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void search_shouldSearchLoanNumberUserFullNameAndDisbursementAccountNumber() {

        Root<Loan> root = mock(Root.class);
        CriteriaQuery<Loan> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path loanNumberPath = mock(Path.class);

        Path userPath = mock(Path.class);
        Path fullNamePath = mock(Path.class);

        Path disbursementAccountPath = mock(Path.class);
        Path accountNumberPath = mock(Path.class);

        Predicate loanNumberPredicate = mock(Predicate.class);
        Predicate fullNamePredicate = mock(Predicate.class);
        Predicate accountNumberPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        /*
         * loanNumber
         */
        doReturn(loanNumberPath)
                .when(root)
                .get("loanNumber");

        /*
         * user.fullName
         */
        doReturn(userPath)
                .when(root)
                .get("user");

        doReturn(fullNamePath)
                .when(userPath)
                .get("fullName");

        /*
         * disbursementAccount.accountNumber
         */
        doReturn(disbursementAccountPath)
                .when(root)
                .get("disbursementAccount");

        doReturn(accountNumberPath)
                .when(disbursementAccountPath)
                .get("accountNumber");

        /*
         * lower(...)
         */
        when(cb.lower(loanNumberPath))
                .thenReturn(loanNumberPath);

        when(cb.lower(fullNamePath))
                .thenReturn(fullNamePath);

        when(cb.lower(accountNumberPath))
                .thenReturn(accountNumberPath);

        /*
         * like(...)
         */
        when(cb.like(
                loanNumberPath,
                "%loan123%"
        )).thenReturn(loanNumberPredicate);

        when(cb.like(
                fullNamePath,
                "%loan123%"
        )).thenReturn(fullNamePredicate);

        when(cb.like(
                accountNumberPath,
                "%loan123%"
        )).thenReturn(accountNumberPredicate);

        /*
         * or(...)
         */
        when(cb.or(
                loanNumberPredicate,
                fullNamePredicate,
                accountNumberPredicate
        )).thenReturn(combinedPredicate);

        Specification<Loan> specification =
                LoanSpecification.search("loan123");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(combinedPredicate, result);

        /*
         * Verify path traversal
         */
        verify(root).get("loanNumber");
        verify(root).get("user");
        verify(root).get("disbursementAccount");

        verify(userPath).get("fullName");
        verify(disbursementAccountPath).get("accountNumber");

        /*
         * Verify lower()
         */
        verify(cb).lower(loanNumberPath);
        verify(cb).lower(fullNamePath);
        verify(cb).lower(accountNumberPath);

        /*
         * Verify like()
         */
        verify(cb).like(
                loanNumberPath,
                "%loan123%"
        );

        verify(cb).like(
                fullNamePath,
                "%loan123%"
        );

        verify(cb).like(
                accountNumberPath,
                "%loan123%"
        );

        /*
         * Verify OR
         */
        verify(cb).or(
                loanNumberPredicate,
                fullNamePredicate,
                accountNumberPredicate
        );
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void search_shouldLowercaseSearchValue() {

        Root<Loan> root = mock(Root.class);
        CriteriaQuery<Loan> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path loanNumberPath = mock(Path.class);

        Path userPath = mock(Path.class);
        Path fullNamePath = mock(Path.class);

        Path disbursementAccountPath = mock(Path.class);
        Path accountNumberPath = mock(Path.class);

        Predicate loanNumberPredicate = mock(Predicate.class);
        Predicate fullNamePredicate = mock(Predicate.class);
        Predicate accountNumberPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        doReturn(loanNumberPath)
                .when(root)
                .get("loanNumber");

        doReturn(userPath)
                .when(root)
                .get("user");

        doReturn(fullNamePath)
                .when(userPath)
                .get("fullName");

        doReturn(disbursementAccountPath)
                .when(root)
                .get("disbursementAccount");

        doReturn(accountNumberPath)
                .when(disbursementAccountPath)
                .get("accountNumber");

        when(cb.lower(loanNumberPath))
                .thenReturn(loanNumberPath);

        when(cb.lower(fullNamePath))
                .thenReturn(fullNamePath);

        when(cb.lower(accountNumberPath))
                .thenReturn(accountNumberPath);

        when(cb.like(
                loanNumberPath,
                "%raj%"
        )).thenReturn(loanNumberPredicate);

        when(cb.like(
                fullNamePath,
                "%raj%"
        )).thenReturn(fullNamePredicate);

        when(cb.like(
                accountNumberPath,
                "%raj%"
        )).thenReturn(accountNumberPredicate);

        when(cb.or(
                loanNumberPredicate,
                fullNamePredicate,
                accountNumberPredicate
        )).thenReturn(combinedPredicate);

        Specification<Loan> specification =
                LoanSpecification.search("RAJ");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(combinedPredicate, result);

        verify(cb).like(
                loanNumberPath,
                "%raj%"
        );

        verify(cb).like(
                fullNamePath,
                "%raj%"
        );

        verify(cb).like(
                accountNumberPath,
                "%raj%"
        );
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void search_shouldRetainWhitespaceBecauseSpecificationDoesNotTrim() {

        Root<Loan> root = mock(Root.class);
        CriteriaQuery<Loan> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path loanNumberPath = mock(Path.class);

        Path userPath = mock(Path.class);
        Path fullNamePath = mock(Path.class);

        Path disbursementAccountPath = mock(Path.class);
        Path accountNumberPath = mock(Path.class);

        Predicate loanNumberPredicate = mock(Predicate.class);
        Predicate fullNamePredicate = mock(Predicate.class);
        Predicate accountNumberPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        doReturn(loanNumberPath)
                .when(root)
                .get("loanNumber");

        doReturn(userPath)
                .when(root)
                .get("user");

        doReturn(fullNamePath)
                .when(userPath)
                .get("fullName");

        doReturn(disbursementAccountPath)
                .when(root)
                .get("disbursementAccount");

        doReturn(accountNumberPath)
                .when(disbursementAccountPath)
                .get("accountNumber");

        when(cb.lower(loanNumberPath))
                .thenReturn(loanNumberPath);

        when(cb.lower(fullNamePath))
                .thenReturn(fullNamePath);

        when(cb.lower(accountNumberPath))
                .thenReturn(accountNumberPath);

        /*
         * Production code uses:
         *
         * "%" + search.toLowerCase() + "%"
         *
         * There is NO trim().
         *
         * Therefore " RAJ " becomes "% raj %".
         */
        when(cb.like(
                loanNumberPath,
                "% raj %"
        )).thenReturn(loanNumberPredicate);

        when(cb.like(
                fullNamePath,
                "% raj %"
        )).thenReturn(fullNamePredicate);

        when(cb.like(
                accountNumberPath,
                "% raj %"
        )).thenReturn(accountNumberPredicate);

        when(cb.or(
                loanNumberPredicate,
                fullNamePredicate,
                accountNumberPredicate
        )).thenReturn(combinedPredicate);

        Specification<Loan> specification =
                LoanSpecification.search(" RAJ ");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(combinedPredicate, result);

        verify(cb).like(
                loanNumberPath,
                "% raj %"
        );

        verify(cb).like(
                fullNamePath,
                "% raj %"
        );

        verify(cb).like(
                accountNumberPath,
                "% raj %"
        );
    }

    @Test
    void loanType_shouldReturnNullWhenLoanTypeIsNull() {

        Root<Loan> root = mock(Root.class);
        CriteriaQuery<Loan> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Specification<Loan> specification =
                LoanSpecification.loanType(null);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertNull(result);

        verifyNoInteractions(root);
        verifyNoInteractions(cb);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void loanType_shouldCreateEqualPredicateForGivenLoanType() {

        Root<Loan> root = mock(Root.class);
        CriteriaQuery<Loan> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path loanTypePath = mock(Path.class);
        Predicate equalPredicate = mock(Predicate.class);

        Loan.LoanType loanType =
                Loan.LoanType.PERSONAL;

        doReturn(loanTypePath)
                .when(root)
                .get("loanType");

        when(cb.equal(
                loanTypePath,
                loanType
        )).thenReturn(equalPredicate);

        Specification<Loan> specification =
                LoanSpecification.loanType(loanType);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(equalPredicate, result);

        verify(root).get("loanType");

        verify(cb).equal(
                loanTypePath,
                loanType
        );
    }
}