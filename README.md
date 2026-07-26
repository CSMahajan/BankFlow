# BankFlow — Retail Banking Account Management

A portfolio-ready full-stack banking application based on the supplied Capgemini-style requirement brief. It adds a responsive UI to the required Java/Spring Boot REST API.

## What it demonstrates

- JWT authentication with `CUSTOMER` and `ADMIN` roles
- Savings/current account lifecycle and customer-owned access control
- Fixed-deposit creation, validation, and maturity calculation
- Transaction dashboard with date filtering and credit/debit totals
- Swagger/OpenAPI documentation, global error responses, Bean Validation, JUnit/Mockito test structure
- A responsive React dashboard rather than API-only screens

> This is a learning/portfolio application. It is not suitable for real banking use: production banking needs audited identity checks, encryption key management, rate limiting, monitoring, ledgers, fraud controls, and regulatory compliance.

## Stack

| Layer | Technology |
| --- | --- |
| API | Java 21, Spring Boot 3.4, Spring Security, JPA/Hibernate |
| Data | PostgreSQL 16 (H2 profile for local demo) |
| UI | React 18, Vite, plain CSS |
| Quality | JUnit 5, Mockito, JaCoCo, OpenAPI/Swagger |

## Run locally

### 1. Start the API

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

The API runs at `http://localhost:8080`; Swagger is at `http://localhost:8080/swagger-ui/index.html`.

Demo credentials: `admin@bankflow.dev` / `Password@123`, or `maya@bankflow.dev` / `Password@123`.

### 2. Start the UI

```bash
cd frontend
npm install
npm run dev
```

Open the URL Vite prints (usually `http://localhost:5173`). The UI uses its polished demo data until the API integration is enabled in `.env`.

## Project structure

```text
bankflow/
├── backend/                 # Spring Boot API
│   ├── src/main/java/...    # domain-first packages
│   ├── src/main/resources/
│   └── src/test/java/...
├── frontend/                # React customer dashboard
├── docs/                    # API contract and architecture notes
└── docker-compose.yml       # PostgreSQL for the non-demo profile
```

## Suggested GitHub milestones

1. **Foundation:** this repository, login screen, dashboard, OpenAPI, local demo profile.
2. **Secure APIs:** JWT filter, registration and role-based authorization.
3. **Core banking:** complete account/FD/transaction persistence and service tests.
4. **Polish:** integrate every UI action, add screenshots, CI, coverage badge and deployment.

## Quality checklist before submission

- [ ] Replace demo data with seeded PostgreSQL data and run all tests.
- [ ] Add controller/service/repository test coverage and check `target/site/jacoco/index.html`.
- [ ] Export a Postman collection from Swagger.
- [ ] Add screenshots/GIF to this README and configure GitHub Actions.
- [ ] Never commit `.env`, secrets, or a real JWT key.
