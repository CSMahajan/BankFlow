export const actionDisplayNames = {
    LOGIN: "🔐 Login",
    USER_REGISTERED: "👤 User Registered",

    ACCOUNT_CREATED: "🏦 Account Created",
    ACCOUNT_ACTIVATED: "✅ Account Activated",
    ACCOUNT_FROZEN: "❄️ Account Frozen",

    MONEY_TRANSFER: "💸 Money Transfer",
    SCHEDULED_TRANSFER_CREATED: "📅 Scheduled Transfer Created",
    SCHEDULED_TRANSFER_CANCELLED: "🚫 Scheduled Transfer Cancelled",
    SCHEDULED_TRANSFER_EXECUTED: "▶️ Scheduled Transfer Executed",

    FD_CREATED: "💰 Fixed Deposit Created",
    FD_CLOSED: "💰 Fixed Deposit Closed",

    LOAN_APPLIED: "📄 Loan Applied",
    LOAN_APPROVED: "✅ Loan Approved",
    LOAN_REJECTED: "❌ Loan Rejected",
    EMI_PAID: "💵 EMI Paid",

    CARD_ISSUED: "💳 Card Issued",
    CARD_FROZEN: "❄️ Card Frozen",
    CARD_ACTIVATED: "✅ Card Activated",
    CARD_BLOCKED: "⛔ Card Blocked",
    CARD_UNBLOCKED: "🔓 Card Unblocked",
    CARD_LIMIT_UPDATED: "✏️ Daily Limit Updated",

    PROFILE_UPDATED: "👤 Profile Updated",
};

export const moduleDisplayNames = {
    LOGIN: "🔐 Authentication",
    ACCOUNTS: "🏦 Accounts",
    TRANSFERS: "💸 Transfers",
    FIXED_DEPOSITS: "💰 Fixed Deposits",
    LOANS: "📄 Loans",
    CARDS: "💳 Cards",
    PROFILE: "👤 Profile",
};

export const moduleActions = {
    LOGIN: [
        "LOGIN",
        "USER_REGISTERED",
    ],

    ACCOUNTS: [
        "ACCOUNT_CREATED",
        "ACCOUNT_ACTIVATED",
        "ACCOUNT_FROZEN",
    ],

    TRANSFERS: [
        "MONEY_TRANSFER",
        "SCHEDULED_TRANSFER_CREATED",
        "SCHEDULED_TRANSFER_CANCELLED",
        "SCHEDULED_TRANSFER_EXECUTED",
    ],

    FIXED_DEPOSITS: [
        "FD_CREATED",
        "FD_CLOSED",
    ],

    LOANS: [
        "LOAN_APPLIED",
        "LOAN_APPROVED",
        "LOAN_REJECTED",
        "EMI_PAID",
    ],

    CARDS: [
        "CARD_ISSUED",
        "CARD_FROZEN",
        "CARD_ACTIVATED",
        "CARD_BLOCKED",
        "CARD_UNBLOCKED",
        "CARD_LIMIT_UPDATED",
    ],

    PROFILE: [
        "PROFILE_UPDATED",
    ],
};