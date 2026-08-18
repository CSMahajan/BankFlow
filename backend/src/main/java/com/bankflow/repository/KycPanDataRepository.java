package com.bankflow.repository;

import com.bankflow.entity.KycPanData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KycPanDataRepository
        extends JpaRepository<KycPanData, Long> {

    Optional<KycPanData> findByKycDocumentId(Long documentId);

}