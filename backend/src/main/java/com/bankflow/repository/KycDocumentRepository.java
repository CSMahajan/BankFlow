package com.bankflow.repository;

import com.bankflow.entity.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KycDocumentRepository
        extends JpaRepository<KycDocument, Long>,
        JpaSpecificationExecutor<KycDocument> {

    List<KycDocument> findByUserIdOrderByUploadedAtDesc(Long userId);

    Optional<KycDocument> findByUserIdAndDocumentType(
            Long userId,
            KycDocument.DocumentType documentType
    );

    long countByKycVerificationStatus(KycDocument.KycVerificationStatus status);

    @Query("""
        SELECT COUNT(DISTINCT k.user.id)
        FROM KycDocument k
        WHERE k.kycVerificationStatus = 
        com.bankflow.entity.KycDocument$KycVerificationStatus.PENDING
        """)
    long countPendingCustomers();

}