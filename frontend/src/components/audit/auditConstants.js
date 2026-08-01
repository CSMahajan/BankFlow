export const actionDisplayNames = {
    LOGIN: "🔐 Login",

    ACCOUNT_CREATED: "🏦 Account Created",

    MONEY_TRANSFER: "💸 Money Transfer",

    FD_CREATED: "💰 Fixed Deposit Created",
    FD_CLOSED: "💰 Fixed Deposit Closed",

    LOAN_APPLIED: "📄 Loan Applied",
    LOAN_APPROVED: "✅ Loan Approved",
    LOAN_REJECTED: "❌ Loan Rejected",
    EMI_PAID: "💵 EMI Paid",

    CARD_ISSUED: "💳 Card Issued",
    CARD_FROZEN: "❄ Card Frozen",
    CARD_ACTIVATED: "✅ Card Activated",
    CARD_LIMIT_UPDATED: "✏ Daily Limit Updated",

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
    ],

    ACCOUNTS: [
        "ACCOUNT_CREATED",
    ],

    TRANSFERS: [
        "MONEY_TRANSFER",
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
        "CARD_LIMIT_UPDATED",
    ],

    PROFILE: [
        "PROFILE_UPDATED",
    ],
};