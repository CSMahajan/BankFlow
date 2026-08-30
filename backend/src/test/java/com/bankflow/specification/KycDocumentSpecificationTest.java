package com.bankflow.specification;

import com.bankflow.entity.KycDocument;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class KycDocumentSpecificationTest {

    @Test
    void status_shouldReturnConjunctionWhenStatusIsNull() {

        Root<KycDocument> root = mock(Root.class);
        CriteriaQuery<KycDocument> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(conjunction);

        Specification<KycDocument> specification =
                KycDocumentSpecification.status(null);

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

        Root<KycDocument> root = mock(Root.class);
        CriteriaQuery<KycDocument> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path statusPath = mock(Path.class);
        Predicate equalPredicate = mock(Predicate.class);

        KycDocument.KycVerificationStatus status =
                KycDocument.KycVerificationStatus.VERIFIED;

        doReturn(statusPath)
                .when(root)
                .get("kycVerificationStatus");

        when(cb.equal(
                statusPath,
                status
        )).thenReturn(equalPredicate);

        Specification<KycDocument> specification =
                KycDocumentSpecification.status(status);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(equalPredicate, result);

        verify(root).get("kycVerificationStatus");

        verify(cb).equal(
                statusPath,
                status
        );
    }

    @Test
    void search_shouldReturnConjunctionWhenSearchIsNull() {

        Root<KycDocument> root = mock(Root.class);
        CriteriaQuery<KycDocument> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(conjunction);

        Specification<KycDocument> specification =
                KycDocumentSpecification.search(null);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(conjunction, result);

        verify(cb).conjunction();
        verifyNoMoreInteractions(cb);
        verifyNoInteractions(root);
    }

    @Test
    void search_shouldReturnConjunctionWhenSearchIsBlank() {

        Root<KycDocument> root = mock(Root.class);
        CriteriaQuery<KycDocument> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate conjunction = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(conjunction);

        Specification<KycDocument> specification =
                KycDocumentSpecification.search("   ");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(conjunction, result);

        verify(cb).conjunction();
        verifyNoMoreInteractions(cb);
        verifyNoInteractions(root);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void search_shouldSearchUserFullNameAndEmail() {

        Root<KycDocument> root = mock(Root.class);
        CriteriaQuery<KycDocument> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path userPath = mock(Path.class);
        Path fullNamePath = mock(Path.class);
        Path emailPath = mock(Path.class);

        Predicate fullNamePredicate = mock(Predicate.class);
        Predicate emailPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

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
         * user.email
         */
        doReturn(emailPath)
                .when(userPath)
                .get("email");

        /*
         * lower(...)
         */
        when(cb.lower(fullNamePath))
                .thenReturn(fullNamePath);

        when(cb.lower(emailPath))
                .thenReturn(emailPath);

        /*
         * like(...)
         */
        when(cb.like(
                fullNamePath,
                "%raj%"
        )).thenReturn(fullNamePredicate);

        when(cb.like(
                emailPath,
                "%raj%"
        )).thenReturn(emailPredicate);

        /*
         * or(...)
         */
        when(cb.or(
                fullNamePredicate,
                emailPredicate
        )).thenReturn(combinedPredicate);

        Specification<KycDocument> specification =
                KycDocumentSpecification.search("raj");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(combinedPredicate, result);

        /*
         * Verify path traversal
         *
         * root.get("user") is called twice by the
         * production specification.
         */
        verify(root, times(2)).get("user");

        verify(userPath).get("fullName");
        verify(userPath).get("email");

        /*
         * Verify lower()
         */
        verify(cb).lower(fullNamePath);
        verify(cb).lower(emailPath);

        /*
         * Verify like()
         */
        verify(cb).like(
                fullNamePath,
                "%raj%"
        );

        verify(cb).like(
                emailPath,
                "%raj%"
        );

        /*
         * Verify OR combination
         */
        verify(cb).or(
                fullNamePredicate,
                emailPredicate
        );
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void search_shouldLowercaseSearchValue() {

        Root<KycDocument> root = mock(Root.class);
        CriteriaQuery<KycDocument> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path userPath = mock(Path.class);
        Path fullNamePath = mock(Path.class);
        Path emailPath = mock(Path.class);

        Predicate fullNamePredicate = mock(Predicate.class);
        Predicate emailPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        doReturn(userPath)
                .when(root)
                .get("user");

        doReturn(fullNamePath)
                .when(userPath)
                .get("fullName");

        doReturn(emailPath)
                .when(userPath)
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

        Specification<KycDocument> specification =
                KycDocumentSpecification.search("RAJ");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(combinedPredicate, result);

        verify(cb).like(
                fullNamePath,
                "%raj%"
        );

        verify(cb).like(
                emailPath,
                "%raj%"
        );
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void search_shouldCreateCorrectPatternWhenSearchContainsWhitespace() {

        Root<KycDocument> root = mock(Root.class);
        CriteriaQuery<KycDocument> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path userPath = mock(Path.class);
        Path fullNamePath = mock(Path.class);
        Path emailPath = mock(Path.class);

        Predicate fullNamePredicate = mock(Predicate.class);
        Predicate emailPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        doReturn(userPath)
                .when(root)
                .get("user");

        doReturn(fullNamePath)
                .when(userPath)
                .get("fullName");

        doReturn(emailPath)
                .when(userPath)
                .get("email");

        when(cb.lower(fullNamePath))
                .thenReturn(fullNamePath);

        when(cb.lower(emailPath))
                .thenReturn(emailPath);

        when(cb.like(
                fullNamePath,
                "% raj %"
        )).thenReturn(fullNamePredicate);

        when(cb.like(
                emailPath,
                "% raj %"
        )).thenReturn(emailPredicate);

        when(cb.or(
                fullNamePredicate,
                emailPredicate
        )).thenReturn(combinedPredicate);

        Specification<KycDocument> specification =
                KycDocumentSpecification.search(" RAJ ");

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(combinedPredicate, result);

        /*
         * Important:
         * KycDocumentSpecification uses:
         *
         * search.toLowerCase()
         *
         * NOT:
         *
         * search.trim().toLowerCase()
         *
         * Therefore the spaces are intentionally retained.
         */
        verify(cb).like(
                fullNamePath,
                "% raj %"
        );

        verify(cb).like(
                emailPath,
                "% raj %"
        );
    }
}