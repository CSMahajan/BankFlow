# BankFlow — Retail Banking Management Platform

BankFlow is a full-stack retail banking management application built to demonstrate the design and implementation of a secure, modular banking system.

It provides customer and administrator workflows for account management, transactions, fund transfers, cards, loans, fixed deposits, KYC processing, auditing, and authentication.

The project is built with **React**, **Java 21**, **Spring Boot**, and **PostgreSQL**, with AWS services used for asynchronous KYC document processing.

> **Portfolio / Learning Project**
>
> BankFlow is an educational and portfolio project and is **not intended for use as production banking software**. Real banking systems require substantially stronger security controls, regulatory compliance, audited infrastructure, fraud detection, operational controls, and resilience mechanisms.

---

## ✨ Features

### 🔐 Authentication & Authorization

- Customer registration and login
- Email verification
- Forgot-password and password-reset workflows
- JWT-based authentication
- Refresh-token based session renewal
- Logout and token invalidation
- Role-based access control
- Customer and administrator access separation
- Customer-owned resource authorization
- Password change
- Rate limiting for selected operations

### 🏦 Banking Accounts

- View customer accounts
- Account details and balances
- Account lifecycle management
- Daily transaction-limit management
- Account freezing and unfreezing
- Administrative account management
- Account transaction history

### 💸 Transactions & Transfers

- Transaction history
- Transaction filtering and searching
- Account-based transaction filtering
- Date-range filtering
- Transaction-type filtering
- Fund transfers between accounts
- Transfer validation
- Scheduled fund transfers
- Scheduled-transfer management

### 💳 Cards

- Customer card management
- View card details
- Card status management
- Administrative card management
- Block and unblock cards

### 💰 Loans

- Loan application
- Loan details and status
- Loan approval/rejection workflows
- Loan repayment / EMI payment
- Administrative loan management

### 📈 Fixed Deposits

- Fixed-deposit calculation
- Open fixed deposits
- View fixed deposits
- Fixed-deposit details and maturity information

### 🪪 KYC Document Processing

BankFlow implements an asynchronous KYC document-processing pipeline using AWS services.

The high-level flow is:

```text
Customer
   │
   ▼
React Frontend
   │
   ▼
Spring Boot KYC API
   │
   ▼
Private Amazon S3
   │
   ▼
Amazon GuardDuty
Malware Protection for S3
   │
   ▼
Amazon EventBridge
   │
   ▼
Amazon SQS
   │
   ▼
KycMalwareScanListener
   │
   ├─────────────── Threat / Invalid ───────────────► Stop processing
   │
   ▼
CLEAN
   │
   ▼
KycExtractionEvent
   │
   ▼
Amazon Textract
   │
   ▼
Extracted KYC Data
```
The malware scan result is processed asynchronously. Clean documents continue to the existing KYC extraction flow, while infected documents are prevented from reaching OCR processing.
The listener also handles unexpected, duplicate, or invalid event scenarios without starting the extraction process.
See the detailed workflow:
[**KYC Document Processing Workflow**](docs/workflows/BankFlow_KYC_Workflow.drawio.png)
## 🛠️ Technology Stack
### Frontend
- React
- JavaScript
- REST API integration
- React-based customer and administrator dashboards
### Backend
- Java 21
- Spring Boot 3.4.7
- Spring Security
- JWT authentication
- Spring Data JPA
- Hibernate
- Bean Validation
- Spring Data JPA Specifications
- Flyway
- Springdoc OpenAPI
- Spring Boot Actuator
### Database
- PostgreSQL
- Neon PostgreSQL
- Flyway database migrations
### AWS
- Amazon S3
- Amazon GuardDuty Malware Protection for S3
- Amazon EventBridge
- Amazon SQS
- Amazon Textract
### Other Services / Libraries
- Brevo — email delivery
- OpenPDF — PDF generation
- Apache POI — document processing
- Mockito / JUnit — testing
- JaCoCo — test coverage
### Deployment
- Render — frontend and backend hosting
- Neon — PostgreSQL
- AWS — cloud services
- Brevo — email delivery
## 🏗️ Architecture
BankFlow follows a layered backend architecture with a React frontend communicating with Spring Boot REST APIs.
The application integrates PostgreSQL for persistence and external services for email, document storage, malware scanning, asynchronous messaging, and OCR.
### System Architecture

[View / edit the Draw.io source](docs/architecture/bankflow-system-architecture.drawio.xml)
## 🗄️ Data Model
The application's relational data model is documented using an ER diagram covering users, accounts, transactions, cards, loans, fixed deposits, KYC data, scheduled transfers, refresh tokens, verification tokens, and audit information.
### Entity Relationship Diagram

[View / edit the Draw.io source](docs/data-model/bankflow-erd.drawio.xml)
## 🔄 Workflows
The repository contains detailed workflow diagrams for the application's authentication, authorization, session, and KYC processing flows.
### Authentication

[View / edit the Draw.io source](docs/workflows/BankFlow_Authentication_Workflows.drawio.xml)
### Email Verification & Password Reset

[View / edit the Draw.io source](docs/workflows/BankFlow_Email_Password_Workflows.drawio.xml)
### JWT Authorization

[View / edit the Draw.io source](docs/workflows/BankFlow_JWT_Authorization_Workflow.drawio.xml)
### Refresh Token & Logout

[View / edit the Draw.io source](docs/workflows/BankFlow_Refresh_Logout_Workflow.drawio.xml)
### KYC Document Processing

[View / edit the Draw.io source](docs/workflows/BankFlow_KYC_Workflow.drawio.xml)
## 📖 API Documentation
BankFlow exposes REST APIs documented using OpenAPI.
### OpenAPI Specification
- [OpenAPI YAML](docs/api/bankflow_openapi.yml)
- [OpenAPI JSON](docs/api/bankflow_openapi.json)
  The OpenAPI specification provides the API contract for the backend endpoints, request/response models, and security configuration.
### Swagger UI
When running the backend locally, Swagger UI is available at:
`http://localhost:8080/swagger-ui/index.html`
## 🖥️ Application Screenshots
The repository contains representative screenshots of both customer and administrator functionality.
### Customer

| Feature | Screenshot |
| --- | --- |
| Login | [Login](docs/screenshots/customer/login.png) |
| Dashboard | [Dashboard](docs/screenshots/customer/dashboard.png) |
| Accounts | [Accounts](docs/screenshots/customer/accounts.png) |
| Transactions | [Transaction History](docs/screenshots/customer/transaction_history.png) |
| Cards | [Cards](docs/screenshots/customer/cards.png) |
| Loans | [Loans](docs/screenshots/customer/view_all_loans.png) |
| KYC | [KYC Upload](docs/screenshots/customer/upload_kyc.png) |

### Administrator

| Feature | Screenshot |
| --- | --- |
| Dashboard | [Dashboard](docs/screenshots/admin/dashboard.png) |
| User Management | [User Management](docs/screenshots/admin/user_management.png) |
| Account Management | [Account Management](docs/screenshots/admin/accounts_management.png) |
| Card Management | [Card Management](docs/screenshots/admin/cards_management.png) |
| Loan Approvals | [Loan Approvals](docs/screenshots/admin/loan_approvals.png) |
| KYC Verification | [KYC Verification](docs/screenshots/admin/kyc_verification.png) |
| Audit Logs | [Audit Logs](docs/screenshots/admin/audit_logs.png) |

## 🧪 Testing
The backend contains unit tests covering the application's important business logic and service-layer components.
Testing uses:
- JUnit
- Mockito
- Spring testing utilities where required
- JaCoCo for code coverage reporting
  The focus of testing is on meaningful application behavior, including business rules, validation, authorization-related logic, and service interactions.
## 🔐 Security
Security-related functionality implemented in BankFlow includes:
- Spring Security
- JWT authentication
- Refresh-token workflow
- Role-based authorization
- Customer resource ownership checks
- Password hashing
- Email verification
- Password-reset tokens
- Rate limiting
- Audit logging
- Private S3 KYC document storage
- Asynchronous malware scanning before OCR processing
- Malware result handling through AWS event-driven processing
## 📂 Project Structure

```text
BankFlow/
├── frontend/
│   └── React application
│
├── backend/
│   └── Spring Boot application
│
├── docs/
│   ├── architecture/
│   │   ├── bankflow-system-architecture.drawio.png
│   │   └── bankflow-system-architecture.drawio.xml
│   │
│   ├── data-model/
│   │   ├── bankflow-erd.drawio.png
│   │   └── bankflow-erd.drawio.xml
│   │
│   ├── workflows/
│   │   ├── BankFlow_Authentication_Workflows.drawio.png
│   │   ├── BankFlow_Authentication_Workflows.drawio.xml
│   │   ├── BankFlow_Email_Password_Workflows.drawio.png
│   │   ├── BankFlow_Email_Password_Workflows.drawio.xml
│   │   ├── BankFlow_JWT_Authorization_Workflow.drawio.png
│   │   ├── BankFlow_JWT_Authorization_Workflow.drawio.xml
│   │   ├── BankFlow_KYC_Workflow.drawio.png
│   │   ├── BankFlow_KYC_Workflow.drawio.xml
│   │   ├── BankFlow_Refresh_Logout_Workflow.drawio.png
│   │   └── BankFlow_Refresh_Logout_Workflow.drawio.xml
│   │
│   ├── api/
│   │   ├── bankflow_openapi.yml
│   │   └── bankflow_openapi.json
│   │
│   └── screenshots/
│       ├── customer/
│       └── admin/
│
└── README.md
```

## 🚀 Running Locally
### Backend
##### Requirements
- Java 21
- Maven
- PostgreSQL or a configured PostgreSQL-compatible database
- Required AWS configuration
- Required Brevo configuration
  Configure the required environment variables/application properties before starting the application.
  Start the Spring Boot backend with:

```bash
./mvnw spring-boot:run
```

Or, if Maven is installed globally:

```bash
mvn spring-boot:run
```
The backend runs by default on:
http://localhost:8080
### Swagger UI
Once the backend is running:
`http://localhost:8080/swagger-ui/index.html`
### Frontend
##### Requirements
- Node.js
- npm
  Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm start
```
Configure the frontend API base URL to point to the running backend.
## ☁️ Deployment
BankFlow is deployed using Render, Neon, AWS, and Brevo.
### React Frontend
│
▼
Render
│
│ REST API
▼
Spring Boot Backend
│
├──► Neon PostgreSQL
│
├──► AWS S3
├──► AWS GuardDuty
├──► AWS EventBridge
├──► AWS SQS
├──► AWS Textract
│
└──► Brevo
The deployed application can be accessed through the project's configured Render deployment.
## 📚 Documentation
Documentation	Description
[System Architecture](docs/architecture/bankflow-system-architecture.drawio.png)	Application and infrastructure architecture
[ERD](docs/data-model/bankflow-erd.drawio.png)	Database relationships and entities
[Authentication Workflow](docs/workflows/BankFlow_Authentication_Workflows.drawio.png)	Authentication flow
[Email & Password Workflow](docs/workflows/BankFlow_Email_Password_Workflows.drawio.png)	Email verification and password reset
[JWT Authorization Workflow](docs/workflows/BankFlow_JWT_Authorization_Workflow.drawio.png)	JWT authorization flow
[Refresh & Logout Workflow](docs/workflows/BankFlow_Refresh_Logout_Workflow.drawio.png)	Refresh-token and logout flow
[KYC Workflow](docs/workflows/BankFlow_KYC_Workflow.drawio.png)	Asynchronous KYC malware scanning and extraction
[OpenAPI YAML](docs/api/bankflow_openapi.yml)	API specification
[OpenAPI JSON](docs/api/bankflow_openapi.json)	API specification in JSON format

## ⚠️ Disclaimer
BankFlow is a portfolio and learning project intended to demonstrate full-stack development, backend architecture, security concepts, database design, testing, and cloud integration.
It should not be used for handling real banking operations or sensitive financial information.
A production banking platform would require additional controls such as:
- Regulatory compliance
- Independent security audits
- Enterprise-grade key management
- Hardware-backed security controls
- Advanced fraud detection
- Transaction monitoring
- High-availability and disaster-recovery architecture
- Comprehensive observability
- Stronger operational controls
- Formal threat modeling and penetration testing
- Industry-specific compliance requirements
## 👨‍💻 Project
BankFlow — Retail Banking Management Platform
Built as a full-stack engineering project to explore secure REST API design, banking-domain workflows, asynchronous cloud processing, database design, testing, and deployment.
