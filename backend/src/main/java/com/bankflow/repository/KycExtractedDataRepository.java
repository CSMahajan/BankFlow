package com.bankflow.repository;

import com.bankflow.entity.KycExtractedData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KycExtractedDataRepository
        extends JpaRepository<KycExtractedData,Long> {

    Optional<KycExtractedData> findByKycDocumentId(Long kycDocumentId);

}