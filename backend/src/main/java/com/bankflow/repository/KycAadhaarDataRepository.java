package com.bankflow.repository;

import com.bankflow.entity.KycAadhaarData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KycAadhaarDataRepository
        extends JpaRepository<KycAadhaarData, Long> {

    Optional<KycAadhaarData> findByKycDocumentId(Long kycDocumentId);

}