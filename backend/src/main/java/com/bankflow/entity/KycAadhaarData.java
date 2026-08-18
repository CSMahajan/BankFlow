package com.bankflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "kyc_aadhaar_data",
        schema = "retail_banking"
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycAadhaarData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "kyc_document_id",
            nullable = false,
            unique = true
    )
    private KycDocument kycDocument;


    @Column(name = "aadhaar_number")
    private String aadhaarNumber;


    @Column(name = "full_name")
    private String fullName;


    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;


    @Column(name = "gender")
    private String gender;


    @Column(name = "address", columnDefinition = "TEXT")
    private String address;


    @Column(name = "mobile_number")
    private String mobileNumber;


    @Column(nullable = false)
    private LocalDateTime createdAt;


    private LocalDateTime updatedAt;
}