package com.bankflow.service;

import com.bankflow.dto.AdminKycDocumentResponse;
import com.bankflow.entity.KycDocument;
import com.bankflow.repository.KycDocumentRepository;
import com.bankflow.specification.KycDocumentSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminKycService {


    private final KycDocumentRepository kycDocumentRepository;


    @Transactional(readOnly = true)
    public Page<AdminKycDocumentResponse> getAllDocuments(
            int page,
            int size,
            String search,
            KycDocument.KycVerificationStatus status
    ){

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by("uploadedAt")
                                .descending()
                );


        Specification<KycDocument> specification =
                Specification
                        .where(
                                KycDocumentSpecification.search(search)
                        )
                        .and(
                                KycDocumentSpecification.status(status)
                        );


        return kycDocumentRepository
                .findAll(
                        specification,
                        pageable
                )
                .map(document ->
                        new AdminKycDocumentResponse(
                                document.getId(),
                                document.getUser().getId(),
                                document.getUser().getFullName(),
                                document.getUser().getEmail(),
                                document.getDocumentType().name(),
                                document.getOriginalFileName(),
                                document.getKycVerificationStatus().name(),
                                document.getRejectionReason(),
                                document.getUploadedAt()
                        )
                );
    }
}