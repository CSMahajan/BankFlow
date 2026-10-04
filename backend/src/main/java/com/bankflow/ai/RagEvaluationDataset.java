package com.bankflow.ai;

import java.util.List;

public final class RagEvaluationDataset {

    public static final String KYC_WORKFLOW_DRAWIO_XML = "docs/workflows/BankFlow_KYC_Workflow.drawio.xml";
    public static final String DOCUMENT_PROCESSING_WORKFLOW = "Diagram > BankFlow KYC Document Processing Workflow";
    public static final String BANKFLOW_OPENAPI_YML = "docs/api/bankflow_openapi.yml";
    public static final String SOURCE_PATH = "README.md";

    private RagEvaluationDataset() {
    }

    public static List<RagEvaluationCase> cases() {
        return List.of(

                new RagEvaluationCase(
                        "How can I temporarily stop my debit card from being used?",
                        List.of(
                                new RagEvaluationEvidence(
                                        SOURCE_PATH,
                                        "BankFlow — Retail Banking Management Platform > ✨ Features > 💳 Cards"
                                )
                        )
                ),

                new RagEvaluationCase(
                        "Which API is used to freeze or unfreeze a card?",
                        List.of(
                                new RagEvaluationEvidence(
                                        BANKFLOW_OPENAPI_YML,
                                        "API > PATCH /api/v1/cards/{cardId}/toggle-status"
                                )
                        )
                ),

                new RagEvaluationCase(
                        "What endpoint gives me my transaction history?",
                        List.of(
                                new RagEvaluationEvidence(
                                        BANKFLOW_OPENAPI_YML,
                                        "API > GET /api/v1/transactions/my-transactions"
                                )
                        )
                ),

                new RagEvaluationCase(
                        "What database does BankFlow use?",
                        List.of(
                                new RagEvaluationEvidence(
                                        SOURCE_PATH,
                                        "BankFlow — Retail Banking Management Platform > 🛠️ Technology Stack > Database"
                                )
                        )
                ),

                new RagEvaluationCase(
                        "How does BankFlow handle KYC documents?",
                        List.of(
                                new RagEvaluationEvidence(
                                        SOURCE_PATH,
                                        "BankFlow — Retail Banking Management Platform > ✨ Features > 🪪 KYC Document Processing"
                                ),
                                new RagEvaluationEvidence(
                                        KYC_WORKFLOW_DRAWIO_XML,
                                        DOCUMENT_PROCESSING_WORKFLOW
                                )
                        )
                ),

                new RagEvaluationCase(
                        "How does JWT authentication work?",
                        List.of(
                                new RagEvaluationEvidence(
                                        "docs/workflows/BankFlow_JWT_Authorization_Workflow.drawio.xml",
                                        "Diagram > BankFlow JWT Authentication and Authorization"
                                )
                        )
                ),

                new RagEvaluationCase(
                        "What happens during user registration?",
                        List.of(
                                new RagEvaluationEvidence(
                                        SOURCE_PATH,
                                        "BankFlow — Retail Banking Management Platform > ✨ Features > 🔐 Authentication & Authorization"
                                ),
                                new RagEvaluationEvidence(
                                        "docs/workflows/BankFlow_Authentication_Workflows.drawio.xml",
                                        "Diagram > BankFlow Authentication Workflows"
                                ),
                                new RagEvaluationEvidence(
                                        BANKFLOW_OPENAPI_YML,
                                        "API > POST /api/v1/auth/register"
                                )
                        )
                ),

                new RagEvaluationCase(
                        "How are accounts linked to customers?",
                        List.of(
                                new RagEvaluationEvidence(
                                        "docs/data-model/bankflow-erd.drawio.xml",
                                        "Diagram > BankFlow ERD"
                                )
                        )
                ),

                new RagEvaluationCase(
                        "How does the frontend communicate with the backend?",
                        List.of(
                                new RagEvaluationEvidence(
                                        SOURCE_PATH,
                                        "BankFlow — Retail Banking Management Platform > 🏗️ Architecture"
                                )
                        )
                ),

                new RagEvaluationCase(
                        "How do I retrieve my bank accounts?",
                        List.of(
                                new RagEvaluationEvidence(
                                        BANKFLOW_OPENAPI_YML,
                                        "API > GET /api/v1/accounts/my-accounts"
                                )
                        )
                ),

                new RagEvaluationCase(
                        "How can I search my transactions?",
                        List.of(
                                new RagEvaluationEvidence(
                                        SOURCE_PATH,
                                        "BankFlow — Retail Banking Management Platform > ✨ Features > 💸 Transactions & Transfers"
                                )
                        )
                ),

                new RagEvaluationCase(
                        "How are KYC documents processed asynchronously?",
                        List.of(
                                new RagEvaluationEvidence(
                                        KYC_WORKFLOW_DRAWIO_XML,
                                        DOCUMENT_PROCESSING_WORKFLOW
                                )
                        )
                ),

                new RagEvaluationCase(
                        "What happens when a user forgets their password?",
                        List.of(
                                new RagEvaluationEvidence(
                                        SOURCE_PATH,
                                        "BankFlow — Retail Banking Management Platform > ✨ Features > 🔐 Authentication & Authorization"
                                ),
                                new RagEvaluationEvidence(
                                        "docs/workflows/BankFlow_Email_Password_Workflows.drawio.xml",
                                        "Diagram > BankFlow Email Verification and Password Recovery"
                                ),
                                new RagEvaluationEvidence(
                                        BANKFLOW_OPENAPI_YML,
                                        "API > POST /api/v1/auth/forgot-password"
                                )
                        )
                ),

                new RagEvaluationCase(
                        "How does BankFlow renew an authenticated session?",
                        List.of(
                                new RagEvaluationEvidence(
                                        "docs/workflows/BankFlow_Refresh_Logout_Workflow.drawio.xml",
                                        "Diagram > BankFlow Refresh Token Rotation and Logout"
                                ),
                                new RagEvaluationEvidence(
                                        BANKFLOW_OPENAPI_YML,
                                        "API > POST /api/v1/auth/refresh"
                                )
                        )
                ),

                new RagEvaluationCase(
                        "Which API updates the card daily limit?",
                        List.of(
                                new RagEvaluationEvidence(
                                        BANKFLOW_OPENAPI_YML,
                                        "API > PATCH /api/v1/cards/{cardId}/limit"
                                )
                        )
                ),

                new RagEvaluationCase(
                        "What technologies are used by the BankFlow backend?",
                        List.of(
                                new RagEvaluationEvidence(
                                        SOURCE_PATH,
                                        "BankFlow — Retail Banking Management Platform > 🛠️ Technology Stack > Backend"
                                )
                        )
                ),

                new RagEvaluationCase(
                        "What happens when an account is frozen?",
                        List.of(
                                new RagEvaluationEvidence(
                                        SOURCE_PATH,
                                        "BankFlow — Retail Banking Management Platform > ✨ Features > 🏦 Banking Accounts"
                                ),
                                new RagEvaluationEvidence(
                                        BANKFLOW_OPENAPI_YML,
                                        "API > PATCH /api/v1/accounts/{accountNumber}/toggle-status"
                                )
                        )
                ),

                new RagEvaluationCase(
                        "How are customer passwords protected?",
                        List.of(
                                new RagEvaluationEvidence(
                                        SOURCE_PATH,
                                        "BankFlow — Retail Banking Management Platform > 🔐 Security"
                                )
                        )
                ),

                new RagEvaluationCase(
                        "What API is used to view a specific transaction?",
                        List.of(
                                new RagEvaluationEvidence(
                                        BANKFLOW_OPENAPI_YML,
                                        "API > GET /api/v1/transactions/{transactionId}"
                                )
                        )
                ),

                new RagEvaluationCase(
                        "Which AWS services are involved in KYC processing?",
                        List.of(
                                new RagEvaluationEvidence(
                                        SOURCE_PATH,
                                        "BankFlow — Retail Banking Management Platform > ✨ Features > 🪪 KYC Document Processing"
                                ),
                                new RagEvaluationEvidence(
                                        SOURCE_PATH,
                                        "BankFlow — Retail Banking Management Platform > 🛠️ Technology Stack > AWS"
                                ),
                                new RagEvaluationEvidence(
                                        KYC_WORKFLOW_DRAWIO_XML,
                                        DOCUMENT_PROCESSING_WORKFLOW
                                )
                        )
                )
        );
    }
}