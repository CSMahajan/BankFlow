package com.bankflow.ai;

import com.google.genai.Client;
import com.google.genai.types.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AiIntentClassifier {

    private final Client client;
    private final String model;

    public AiIntentClassifier(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model:gemini-3.5-flash-lite}") String model) {

        this.client = Client.builder()
                .apiKey(apiKey)
                .build();

        this.model = model;
    }

    public AiIntent classify(String question) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question must not be blank"
            );
        }

        Schema responseSchema =
                Schema.builder()
                        .type("OBJECT")
                        .properties(
                                Map.of(
                                        "route",
                                        Schema.builder()
                                                .type("STRING")
                                                .enum_(List.of(
                                                        "LIVE_DATA",
                                                        "KNOWLEDGE"
                                                ))
                                                .build(),

                                        "operations",
                                        Schema.builder()
                                                .type("ARRAY")
                                                .items(
                                                        Schema.builder()
                                                                .type("OBJECT")
                                                                .properties(
                                                                        Map.ofEntries(
                                                                                Map.entry(
                                                                                        "intent",
                                                                                        Schema.builder()
                                                                                                .type("STRING")
                                                                                                .enum_(List.of(
                                                                                                        "ACCOUNTS",
                                                                                                        "ACCOUNT",
                                                                                                        "ACCOUNT_BALANCE",
                                                                                                        "TRANSACTIONS",
                                                                                                        "TRANSACTION_TOTAL",
                                                                                                        "CARDS",
                                                                                                        "LOANS",
                                                                                                        "LOAN_REPAYMENT_HISTORY",
                                                                                                        "FIXED_DEPOSITS",
                                                                                                        "FIXED_DEPOSIT",
                                                                                                        "CALCULATE_FD_MATURITY",
                                                                                                        "SCHEDULED_TRANSFERS",
                                                                                                        "ADMIN_DASHBOARD_SUMMARY",
                                                                                                        "ADMIN_USERS",
                                                                                                        "ADMIN_USER_DETAILS",
                                                                                                        "ADMIN_ACCOUNTS",
                                                                                                        "ADMIN_USER_ACCOUNTS",
                                                                                                        "ADMIN_CARDS",
                                                                                                        "ADMIN_USER_CARDS",
                                                                                                        "ADMIN_LOANS",
                                                                                                        "ADMIN_USER_LOANS",
                                                                                                        "BANKFLOW_DOCUMENTATION",
                                                                                                        "GENERAL"
                                                                                                ))
                                                                                                .build()
                                                                                ),

                                                                                Map.entry(
                                                                                        "transactionType",
                                                                                        Schema.builder()
                                                                                                .type("STRING")
                                                                                                .enum_(List.of(
                                                                                                        "CREDIT",
                                                                                                        "DEBIT"
                                                                                                ))
                                                                                                .nullable(true)
                                                                                                .build()
                                                                                ),

                                                                                Map.entry(
                                                                                        "period",
                                                                                        Schema.builder()
                                                                                                .type("STRING")
                                                                                                .enum_(List.of(
                                                                                                        "CURRENT_CALENDAR_YEAR",
                                                                                                        "PREVIOUS_CALENDAR_YEAR",
                                                                                                        "CURRENT_FINANCIAL_YEAR",
                                                                                                        "PREVIOUS_FINANCIAL_YEAR",
                                                                                                        "CURRENT_MONTH",
                                                                                                        "PREVIOUS_MONTH",
                                                                                                        "CUSTOM_RANGE"
                                                                                                ))
                                                                                                .nullable(true)
                                                                                                .build()
                                                                                ),

                                                                                Map.entry(
                                                                                        "monthOffset",
                                                                                        Schema.builder()
                                                                                                .type("INTEGER")
                                                                                                .description("""
                                                                                                        Relative month offset for month-based periods.
                                                                                                        
                                                                                                        0 means the current month.
                                                                                                        -1 means the previous month.
                                                                                                        -2 means two months ago.
                                                                                                        -3 means three months ago.
                                                                                                        
                                                                                                        Use this for relative month expressions.
                                                                                                        Do not use CUSTOM_RANGE for relative month expressions.
                                                                                                        """)
                                                                                                .build()
                                                                                ),

                                                                                Map.entry(
                                                                                        "startDate",
                                                                                        Schema.builder()
                                                                                                .type("STRING")
                                                                                                .nullable(true)
                                                                                                .description(
                                                                                                        "Explicit start date in ISO format yyyy-MM-dd. "
                                                                                                                + "Used when the user specifies a custom date range."
                                                                                                )
                                                                                                .build()
                                                                                ),

                                                                                Map.entry(
                                                                                        "endDate",
                                                                                        Schema.builder()
                                                                                                .type("STRING")
                                                                                                .nullable(true)
                                                                                                .description(
                                                                                                        "Explicit end date in ISO format yyyy-MM-dd. "
                                                                                                                + "Used when the user specifies a custom date range."
                                                                                                )
                                                                                                .build()
                                                                                ),

                                                                                Map.entry(
                                                                                        "accountNumber",
                                                                                        Schema.builder()
                                                                                                .type("STRING")
                                                                                                .nullable(true)
                                                                                                .description(
                                                                                                        "Customer's bank account number when the user explicitly "
                                                                                                                + "asks for transactions from a specific account."
                                                                                                )
                                                                                                .build()
                                                                                ),

                                                                                Map.entry(
                                                                                        "search",
                                                                                        Schema.builder()
                                                                                                .type("STRING")
                                                                                                .nullable(true)
                                                                                                .description(
                                                                                                        "Text the user wants to search for in transaction ID "
                                                                                                                + "or transaction description."
                                                                                                )
                                                                                                .build()
                                                                                ),

                                                                                Map.entry(
                                                                                        "accountStatus",
                                                                                        Schema.builder()
                                                                                                .type("STRING")
                                                                                                .nullable(true)
                                                                                                .enum_(List.of(
                                                                                                        "ACTIVE",
                                                                                                        "FROZEN",
                                                                                                        "INACTIVE"
                                                                                                ))
                                                                                                .description(
                                                                                                        "Optional account status filter for ADMIN_ACCOUNTS. "
                                                                                                                + "Use ACTIVE, FROZEN, or INACTIVE."
                                                                                                )
                                                                                                .build()
                                                                                ),

                                                                                Map.entry(
                                                                                        "cardStatus",
                                                                                        Schema.builder()
                                                                                                .type("STRING")
                                                                                                .nullable(true)
                                                                                                .enum_(List.of(
                                                                                                        "ACTIVE",
                                                                                                        "FROZEN",
                                                                                                        "BLOCKED"
                                                                                                ))
                                                                                                .description(
                                                                                                        "Optional card status filter for ADMIN_CARDS. "
                                                                                                                + "Use ACTIVE, FROZEN, or BLOCKED."
                                                                                                )
                                                                                                .build()
                                                                                ),

                                                                                Map.entry(
                                                                                        "role",
                                                                                        Schema.builder()
                                                                                                .type("STRING")
                                                                                                .nullable(true)
                                                                                                .enum_(List.of(
                                                                                                        "CUSTOMER",
                                                                                                        "ADMIN"
                                                                                                ))
                                                                                                .description(
                                                                                                        "Optional user role filter for ADMIN_USERS. "
                                                                                                                + "Use CUSTOMER to filter customers, "
                                                                                                                + "ADMIN to filter administrators."
                                                                                                )
                                                                                                .build()
                                                                                ),

                                                                                Map.entry(
                                                                                        "userId",
                                                                                        Schema.builder()
                                                                                                .type("INTEGER")
                                                                                                .nullable(true)
                                                                                                .description(
                                                                                                        "User ID for ADMIN_USER_DETAILS. "
                                                                                                                + "Use the numeric BankFlow user ID."
                                                                                                )
                                                                                                .build()
                                                                                ),

                                                                                Map.entry(
                                                                                        "fdNumber",
                                                                                        Schema.builder()
                                                                                                .type("STRING")
                                                                                                .nullable(true)
                                                                                                .description(
                                                                                                        "The customer's fixed deposit number when the user explicitly "
                                                                                                                + "identifies a specific fixed deposit."
                                                                                                )
                                                                                                .build()
                                                                                ),

                                                                                Map.entry(
                                                                                        "loanNumber",
                                                                                        Schema.builder()
                                                                                                .type("STRING")
                                                                                                .nullable(true)
                                                                                                .description(
                                                                                                        "The customer's loan number when the user explicitly "
                                                                                                                + "identifies a specific loan."
                                                                                                )
                                                                                                .build()
                                                                                ),

                                                                                Map.entry(
                                                                                        "loanType",
                                                                                        Schema.builder()
                                                                                                .type("STRING")
                                                                                                .nullable(true)
                                                                                                .enum_(List.of(
                                                                                                        "PERSONAL",
                                                                                                        "VEHICLE",
                                                                                                        "HOME"
                                                                                                ))
                                                                                                .description(
                                                                                                        "Loan type filter for ADMIN_LOANS. " +
                                                                                                                "Use only PERSONAL, VEHICLE, or HOME."
                                                                                                )
                                                                                                .build()
                                                                                ),

                                                                                Map.entry(
                                                                                        "depositAmount",
                                                                                        Schema.builder()
                                                                                                .type("NUMBER")
                                                                                                .nullable(true)
                                                                                                .description(
                                                                                                        "FD deposit amount in INR when calculating FD maturity."
                                                                                                )
                                                                                                .build()
                                                                                ),

                                                                                Map.entry(
                                                                                        "tenureYears",
                                                                                        Schema.builder()
                                                                                                .type("INTEGER")
                                                                                                .nullable(true)
                                                                                                .description(
                                                                                                        "FD tenure in years when calculating maturity. "
                                                                                                                + "Supported values are 1, 3, or 5."
                                                                                                )
                                                                                                .build()
                                                                                )
                                                                        )
                                                                )
                                                                .required(List.of(
                                                                        "intent",
                                                                        "transactionType",
                                                                        "period",
                                                                        "monthOffset",
                                                                        "startDate",
                                                                        "endDate",
                                                                        "accountNumber",
                                                                        "search",
                                                                        "accountStatus",
                                                                        "cardStatus",
                                                                        "role",
                                                                        "userId",
                                                                        "fdNumber",
                                                                        "loanNumber",
                                                                        "loanType",
                                                                        "depositAmount",
                                                                        "tenureYears"
                                                                ))
                                                                .build()
                                                )
                                                .build()
                                )
                        )
                        .required(List.of("route", "operations"))
                        .build();

        String instruction = """
                Classify the user's BankFlow question.
                
                Return ONLY the structured classification.
                Do not answer the user's question.
                
                ROUTE:
                
                LIVE_DATA means the question requires the authenticated
                customer's own current banking data.
                
                KNOWLEDGE means the question can be answered from
                BankFlow documentation, functionality, technology,
                security, architecture, or general knowledge.
                
                OPERATIONS:
                
                The operations array contains the backend operations required
                to answer the user's question.
                
                Use exactly one operation when the question requires one
                backend operation.
                
                Use multiple operations when the question explicitly requires
                multiple independent pieces of customer data.
                
                For example:
                
                "How much did I spend this month compared with last month?"
                
                requires two TRANSACTION_TOTAL operations:
                
                1. DEBIT total for CURRENT_MONTH
                2. DEBIT total for PREVIOUS_MONTH
                
                Each operation must independently describe the data required
                from the backend.
                
                Do not create duplicate operations.
                
                INTENTS:
                
                ACCOUNTS:
                The customer's own accounts.
                
                ACCOUNT:
                Details of one specific customer account identified by account number.
                
                ACCOUNT_BALANCE:
                The customer's own current account balance.
                
                TRANSACTIONS:
                The customer's own transaction records or transaction history.
                
                TRANSACTION_TOTAL:
                A question asking for an aggregate amount of transactions,
                such as how much the customer spent or received.
                
                CARDS:
                The customer's own cards.
                
                LOANS:
                The customer's own loans or loan information.
                
                LOAN_REPAYMENT_HISTORY:
                The repayment history for one specific loan identified by loan number.
                
                FIXED_DEPOSITS:
                The customer's own fixed deposits.
                
                FIXED_DEPOSIT:
                Details of one specific fixed deposit identified by FD number.
                
                CALCULATE_FD_MATURITY:
                Calculates the maturity amount and interest for a hypothetical
                fixed deposit.
                
                SCHEDULED_TRANSFERS:
                The customer's own scheduled transfers.
                
                ADMIN_DASHBOARD_SUMMARY:
                Use this intent when an administrator asks for the overall
                current BankFlow administrative dashboard/operational summary,
                such as total customers, total accounts, active loans, pending
                loans, active fixed deposits, total deposits, or pending KYC
                documents.
                
                This intent takes no parameters.
                
                ADMIN_DASHBOARD_SUMMARY is available only for ADMIN users.
                
                ADMIN_USERS:
                - Use ONLY when the administrator wants a list of multiple users.
                - This includes:
                  - all users
                  - all customers
                  - all administrators
                  - users matching a search/filter
                - ADMIN_USERS must NOT be used when the administrator asks about
                  one specific user.
                
                Optional filters:
                - search: text to match against the user's full name or email
                - role: CUSTOMER or ADMIN
                
                Examples:
                - "Show me all users"
                - "Show me all customers"
                - "Find user Amit"
                - "Find users with gmail.com"
                - "Show me all admins"
                - "Find customers named Rahul"
                
                This intent is available only to administrators.
                
                ADMIN_USER_DETAILS:
                - Use when the administrator wants information about ONE specific user.
                - A specific numeric userId must be provided.
                - This intent takes precedence over ADMIN_USERS whenever the question
                  identifies one specific user and asks for that user's details,
                  information, profile, summary, or data.
                - Examples:
                  - "Show me details of user 10"
                  - "Show me user 4 details"
                  - "Get details for user 9"
                
                ADMIN USER INTENT EXAMPLES:
                
                "Show me all users"
                → ADMIN_USERS
                
                "Show me all customers"
                → ADMIN_USERS
                
                "Show me all administrators"
                → ADMIN_USERS
                
                "Find customers with gmail"
                → ADMIN_USERS
                
                "Show me details of user 10"
                → ADMIN_USER_DETAILS, userId=10
                
                "Show me user 10"
                → ADMIN_USER_DETAILS, userId=10
                
                "Get information about user 10"
                → ADMIN_USER_DETAILS, userId=10
                
                "Show me the profile of user 10"
                → ADMIN_USER_DETAILS, userId=10
                
                "Give me a summary of user 10"
                → ADMIN_USER_DETAILS, userId=10
                
                IMPORTANT:
                ADMIN_USERS is for LISTING users.
                ADMIN_USER_DETAILS is for inspecting ONE specific user.
                If a specific userId is present and the request concerns that one user,
                choose ADMIN_USER_DETAILS.
                
                IMPORTANT DISTINCTION:
                - A request for a list → ADMIN_USERS
                - A request for details of one identified user → ADMIN_USER_DETAILS
                - If userId is provided and the question asks for information/details
                  about that specific user, intent MUST be ADMIN_USER_DETAILS.
                - Do not classify such requests as ADMIN_USERS.
                
                ADMIN_ACCOUNTS:
                - Use when an administrator asks for a list of BankFlow accounts.
                - This means accounts across the BankFlow system, not accounts belonging
                  to one specific user.
                - Optional filters:
                  - search: text to search account number or customer full name
                  - accountStatus: ACTIVE, FROZEN, or INACTIVE
                - Use accountStatus when the administrator explicitly asks for accounts
                  with a particular status.
                - Leave accountStatus null when no status filter is requested.
                - Use ADMIN_USER_ACCOUNTS when the administrator asks for accounts
                  belonging to one specific user.
                - This intent is available only to administrators.
                - Do not use ADMIN_ACCOUNTS for accounts belonging to one specific user.
                
                ADMIN_USER_ACCOUNTS:
                - Use this intent when an administrator asks for accounts belonging to
                  one specific BankFlow user.
                - The user ID must be provided or extracted from the question.
                - This is different from ADMIN_ACCOUNTS:
                  - ADMIN_ACCOUNTS = accounts across the entire BankFlow system
                  - ADMIN_USER_ACCOUNTS = accounts belonging to one specific user
                - This intent is available only to administrators.
                
                Examples:
                - "Show me accounts of user 10" → ADMIN_USER_ACCOUNTS, userId=10
                - "Show me the accounts belonging to user 10" → ADMIN_USER_ACCOUNTS, userId=10
                - "List user 10's accounts" → ADMIN_USER_ACCOUNTS, userId=10
                - "Show me all accounts" → ADMIN_ACCOUNTS
                - "Show me accounts of user 10" → ADMIN_USER_ACCOUNTS
                
                ADMIN_CARDS:
                - Use this intent when an administrator asks for cards across the
                  entire BankFlow system.
                - Optional filters:
                  - search: searches card number, account number, or customer full name
                  - cardStatus: ACTIVE, FROZEN, or BLOCKED
                - Leave search and cardStatus null when no filter is requested.
                - Do not use accountStatus for card filtering.
                
                Examples:
                - "Show me all cards" → ADMIN_CARDS
                - "Show me all active cards" → ADMIN_CARDS, cardStatus=ACTIVE
                - "Show me all frozen cards" → ADMIN_CARDS, cardStatus=FROZEN
                - "Show me all blocked cards" → ADMIN_CARDS, cardStatus=BLOCKED
                - "Find cards for account BF123" → ADMIN_CARDS, search=BF123
                
                ADMIN_USER_CARDS:
                - Use this intent when an administrator asks for cards belonging to
                  one specific BankFlow user.
                - The user ID must be provided or extracted from the question.
                - This is different from ADMIN_CARDS:
                  - ADMIN_CARDS = cards across the entire BankFlow system
                  - ADMIN_USER_CARDS = cards belonging to one specific user
                - This intent is available only to administrators.
                
                Examples:
                - "Show me cards of user 10" → ADMIN_USER_CARDS, userId=10
                - "Show me the cards belonging to user 10" → ADMIN_USER_CARDS, userId=10
                - "List user 10's cards" → ADMIN_USER_CARDS, userId=10
                - "Show me all cards" → ADMIN_CARDS
                - "Show me cards of user 10" → ADMIN_USER_CARDS
                
                ADMIN_LOANS:
                - Use when an administrator asks for pending loan applications.
                - This represents the global pending-loans listing.
                - Optional filters:
                  - search: customer name, loan number, or disbursement account number
                  - loanType: PERSONAL, VEHICLE, or HOME
                - Examples:
                  - "Show me pending loans"
                  - "Show me pending home loans"
                  - "Find pending personal loans"
                  - "Show pending loans for Kar Gh"
                
                ADMIN_USER_LOANS:
                - Use when an administrator asks for loans belonging to one specific user.
                - userId is required.
                - Examples:
                  - "Show me loans of user 10"
                  - "Show user 10's loans"
                
                Do not use ADMIN_USER_LOANS for global pending-loan requests.
                Do not use ADMIN_LOANS when a specific user ID is requested.
                
                For administrator requests about loans:
                - Use ADMIN_LOANS for the global pending-loans listing.
                - Use ADMIN_USER_LOANS for loans belonging to a specific user.
                - Do not use the customer LOANS intent for administrator requests.
                
                AUDIENCE RULE:
                - The authenticated user's audience is provided separately.
                - When audience is ADMIN, administrative intents must be used for
                  administrative/system-wide requests.
                - When audience is CUSTOMER, customer intents must be used for
                  authenticated customer's own data.
                - Never use CUSTOMER-only intents for an ADMIN request when an
                  ADMIN-specific intent exists.
                - When audience is ADMIN, use ADMIN_* intents for administrative/system-wide
                  or administrator-requested user data.
                - Do not use customer-only intents such as CARDS for ADMIN requests when
                  an ADMIN-specific intent exists.
                
                Examples:
                - "Show me all accounts" → ADMIN_ACCOUNTS
                - "Show me all frozen accounts" → ADMIN_ACCOUNTS, accountStatus=FROZEN
                - "Show me all active accounts" → ADMIN_ACCOUNTS, accountStatus=ACTIVE
                - "Find account BF123" → ADMIN_ACCOUNTS, search=BF123
                
                BANKFLOW_DOCUMENTATION:
                Questions about how BankFlow works, its features,
                technology, architecture, security, APIs, infrastructure,
                workflows, or implementation.
                
                GENERAL:
                Questions that are neither customer-specific live banking
                data nor BankFlow documentation.
                
                TRANSACTION TYPE:
                
                DEBIT means money spent, debited, withdrawn, or paid.
                
                CREDIT means money received, credited, deposited, or received
                into the customer's accounts.
                
                PERIOD:
                
                CURRENT_CALENDAR_YEAR means the current calendar year,
                January 1 through the current date.
                
                PREVIOUS_CALENDAR_YEAR means the complete calendar year
                immediately preceding the current calendar year.
                
                CURRENT_FINANCIAL_YEAR means the current Indian financial year,
                April 1 through the current date.
                
                PREVIOUS_FINANCIAL_YEAR means the complete Indian financial year
                immediately preceding the current financial year.
                
                When the user says "this year" without explicitly saying
                "financial year", interpret it as CURRENT_CALENDAR_YEAR.
                
                When the user says "last year" without explicitly saying
                "financial year", interpret it as PREVIOUS_CALENDAR_YEAR.
                
                When the user says "this financial year", interpret it as
                CURRENT_FINANCIAL_YEAR.
                
                When the user says "last financial year", interpret it as
                PREVIOUS_FINANCIAL_YEAR.
                
                Do not treat calendar years and financial years as equivalent.
                
                CURRENT_MONTH means the current calendar month.
                
                PREVIOUS_MONTH means the immediately preceding calendar month.
                
                CUSTOM_RANGE means the user explicitly specifies a date range.
                
                Leave transactionType null when it is not relevant.
                
                Leave period null when it is not relevant.
                
                TRANSACTION FILTERS:
                
                These filters are applicable to TRANSACTIONS intent.
                
                transactionType:
                Use DEBIT when the user asks for debit, spending, payments,
                or money going out.
                
                Use CREDIT when the user asks for credit, received, deposits,
                or money coming in.
                
                Leave null when no transaction type is specified.
                
                period:
                Use the appropriate period when the user refers to a relative
                or named period.
                
                CCURRENT_CALENDAR_YEAR means January 1 through the current date.
                
                PREVIOUS_CALENDAR_YEAR means the complete calendar year
                immediately preceding the current calendar year.
                
                CURRENT_FINANCIAL_YEAR means April 1 through the current date.
                
                PREVIOUS_FINANCIAL_YEAR means the complete Indian financial year
                immediately preceding the current financial year.
                
                CURRENT_MONTH means the first day of the current calendar month
                through the current date.
                
                PREVIOUS_MONTH means the complete immediately preceding
                calendar month.
                
                CUSTOM_RANGE means the user explicitly provides a start date,
                end date, or both as a specific date range.
                
                RELATIVE MONTHS:
                
                When the user refers to relative months, represent the month dynamically
                using monthOffset.
                
                Examples:
                
                For relative month expressions, always populate both period and monthOffset.
                
                Examples:
                
                "this month"
                -> period=CURRENT_MONTH, monthOffset=0
                
                "last month"
                -> period=CURRENT_MONTH, monthOffset=-1
                
                "the month before last"
                -> period=CURRENT_MONTH, monthOffset=-2
                
                "two months ago"
                -> period=CURRENT_MONTH, monthOffset=-2
                
                "three months ago"
                -> period=CURRENT_MONTH, monthOffset=-3
                
                For relative month expressions:
                - period must never be null.
                - monthOffset must never be null.
                - Do not use CUSTOM_RANGE.
                - Do not invent explicit dates.
                
                Do not represent relative months as CUSTOM_RANGE.
                
                Do not invent explicit historical dates for relative month expressions.
                
                There is no fixed maximum number of relative months.
                
                For CALCULATE_FD_MATURITY:
                - depositAmount is required.
                - tenureYears is required.
                - Supported tenure values are 1, 3, or 5.
                - Use this intent when the user asks to calculate or estimate
                  FD maturity based on an amount and tenure.
                - Do not use FIXED_DEPOSIT unless the user identifies an
                  existing FD by FD number.
                
                For ACCOUNT:
                - accountNumber is required.
                - Populate accountNumber only when the user explicitly identifies
                  a specific bank account.
                - Do not invent or infer an account number.
                - Use ACCOUNTS when the user asks for their accounts generally.
                
                For ADMIN_USERS:
                - Use role=CUSTOMER when the administrator asks for customers.
                - Use role=ADMIN when the administrator asks for administrators/admins.
                - Use role=null when no role filter is requested.
                - Use search when the user provides a name, email, or other text
                  intended to match users.
                
                startDate:
                Populate only when the user explicitly provides a start date
                for a custom range.
                Use ISO format yyyy-MM-dd.
                
                endDate:
                Populate only when the user explicitly provides an end date
                for a custom range.
                Use ISO format yyyy-MM-dd.
                
                accountNumber:
                Populate only when the user explicitly identifies a specific
                bank account by account number.
                
                Do not invent or infer an account number.
                
                search:
                Populate when the user asks to find or search transactions
                by transaction ID, description, or other text that should be
                matched against transaction ID or transaction description.
                
                Do not populate search for ordinary transaction-list questions.
                
                For TRANSACTIONS:
                
                - Use transactionType when a CREDIT/DEBIT filter is requested.
                - Use period for semantic date periods.
                - Use startDate/endDate for explicitly specified custom dates.
                - Use accountNumber only when a specific account number is identified.
                - Use search only when the user explicitly asks to search transaction text.
                - Leave unrelated fields null.
                
                For TRANSACTION_TOTAL:
                
                - Use transactionType when the question asks about money spent
                  or money received.
                - Use period for semantic date periods.
                - Use startDate/endDate for explicitly specified custom dates.
                - Leave unrelated fields null.
                - period is REQUIRED unless the user explicitly provides a custom
                  date range.
                - Never leave period null when the user refers to a relative or
                  named time period.
                - For comparisons, create one operation for each requested period.
                - Each operation must independently contain the period it represents.
                
                Example:
                
                User:
                "How much did I spend this month compared with last month?"
                
                Return:
                
                Operation 1:
                intent = TRANSACTION_TOTAL
                transactionType = DEBIT
                period = CURRENT_MONTH
                
                Operation 2:
                intent = TRANSACTION_TOTAL
                transactionType = DEBIT
                period = PREVIOUS_MONTH
                
                For intents other than TRANSACTIONS and TRANSACTION_TOTAL,
                leave transaction-list-specific fields such as accountNumber, fdNumber
                and search null unless the intent specifically requires them.
                
                FIXED DEPOSIT:
                
                For FIXED_DEPOSIT:
                - fdNumber is required.
                - Populate fdNumber only when the user explicitly identifies a fixed deposit number.
                - Do not invent or infer an fdNumber.
                - Use FIXED_DEPOSITS when the user asks for their fixed deposits generally.
                
                For LOAN_REPAYMENT_HISTORY:
                - loanNumber is required.
                - Populate loanNumber only when the user explicitly identifies a loan number.
                - Do not invent or infer a loan number.
                - Use LOANS when the user asks about their loans generally.
                
                Important:
                
                Understand the meaning of the complete question.
                
                Do not classify based only on individual keywords.
                
                Before returning the classification, verify every operation:
                
                - TRANSACTION_TOTAL must have transactionType and period.
                - TRANSACTIONS may have transactionType and/or period depending on the question.
                - If multiple operations are required, every operation must be complete
                  independently.
                - Do not omit a field that is required to execute that operation.
                
                When a comparison or question explicitly requires multiple
                independent backend values, create multiple operations.
                
                Do not calculate totals, comparisons, percentages, or other
                derived financial results yourself. Represent the required
                backend operations and let the application calculate the
                result from authoritative backend data.
                """;

        Content systemContent =
                Content.fromParts(
                        Part.fromText(instruction)
                );

        GenerateContentConfig config =
                GenerateContentConfig.builder()
                        .systemInstruction(systemContent)
                        .responseMimeType("application/json")
                        .responseSchema(responseSchema)
                        .build();

        long startTime = System.currentTimeMillis();

        GenerateContentResponse response =
                client.models.generateContent(
                        model,
                        question,
                        config
                );

        log.info(
                "AI TIMING | intent Gemini call = {} ms",
                System.currentTimeMillis() - startTime
        );

        String json = response.text();

        if (json == null || json.isBlank()) {
            throw new IllegalStateException(
                    "Gemini returned an empty intent classification"
            );
        }

        log.info(
                "AI intent classification | question={} | result={}",
                question,
                json
        );

        return parse(json);
    }

    private AiIntent parse(String json) {

        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();

            return objectMapper.readValue(
                    json,
                    AiIntent.class
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to parse AI intent classification: " + json,
                    e
            );
        }
    }
}