# BankFlow — Retail Banking Management Platform

BankFlow is a full-stack retail banking application built with **React** and **Java Spring Boot**.

The project demonstrates how a modern banking platform can be designed with secure REST APIs, role-based access control, persistent banking data, asynchronous document processing, cloud integrations, validation, auditing, rate limiting, and automated testing.

> **Note:** BankFlow is a portfolio/learning project and is not intended for production banking use. Real banking systems require significantly stronger controls, regulatory compliance, audited infrastructure, enterprise-grade key management, fraud detection, operational monitoring, and additional security measures.

---

## Overview

BankFlow provides separate capabilities for customers and administrators while maintaining customer-owned data access and role-based authorization.

The application currently covers:

- User registration and authentication
- Email verification
- Password reset
- JWT-based authentication
- Role-based access control
- Bank account management
- Account lifecycle management
- Transactions
- Cards
- Loans
- KYC document management
- Malware scanning of uploaded KYC documents
- Document extraction
- Audit logging
- API rate limiting
- Filtering and searching using Spring Data JPA Specifications
- PostgreSQL persistence
- Database versioning with Flyway
- REST API documentation using OpenAPI/Swagger
- Automated unit testing
- Code coverage using JaCoCo

---

# Features

## Authentication & Authorization

- User registration
- Email verification
- Login using JWT authentication
- Password reset flow
- Role-based authorization
- `CUSTOMER` and `ADMIN` roles
- Customer-owned resource access control
- Spring Security integration
- JWT authentication filter

---

## Banking Accounts

Customers can manage their banking accounts through the application.

Supported functionality includes:

- Account creation
- Account lifecycle management
- Account status handling
- Customer-specific account access
- Account number based searching/filtering

---

## Transactions

BankFlow provides transaction management and filtering capabilities.

Features include:

- Transaction history
- Credit/debit transactions
- Transaction type filtering
- Account-based filtering
- Date-range filtering
- Search by transaction ID
- Search by transaction description
- Transaction dashboard information

---

## Cards

Card management functionality includes:

- Card management
- Card status handling
- Searching by card number
- Searching by associated account number
- Searching by customer name

---

## Loans

BankFlow supports loan management functionality including:

- Loan creation and management
- Loan status filtering
- Loan type filtering
- Loan number searching
- Customer-based searching
- Disbursement account based searching

---

# KYC Document Processing

One of the key workflows in BankFlow is the asynchronous processing of KYC documents.

Uploaded documents are stored in **Amazon S3** and processed through a malware scanning workflow before continuing to document extraction.

### KYC processing flow

```text
                  Customer
                     │
                     │ Upload KYC Document
                     ▼
              ┌─────────────┐
              │  Spring API │
              └──────┬──────┘
                     │
                     ▼
                Amazon S3
                     │
                     ▼
              Amazon GuardDuty
               Malware Scan
                     │
                     ▼
                Amazon SQS
                     │
                     ▼
       KycMalwareScanListener
                     │
             ┌───────┴────────┐
             │                │
             ▼                ▼
       CLEAN             THREATS_FOUND
             │                │
             ▼                ▼
    Extraction Event       INFECTED
             │
             ▼
       Document Extraction
             │
             ▼
       Amazon Textract