import React from "react";
import AccountInfoCards from "./AccountInfoCards";
import TransactionHistory from "./TransactionHistory";

const AccountDetails = ({
    account,
    statusStyle,
    transactionAccountId,
    setTransactionAccountId,
    accountTransactions,
    loadAccountTransactions,
    transactionLoading,
    handleStatusClick,
}) => {

    return (
        <div style={styles.accountDetailsContainer}>

            <div style={styles.detailsHeader}>
                <h3 style={styles.customerName}>
                    👤 {account.customerName}
                </h3>

                <p style={styles.detailsSubtitle}>
                    Manage account status and review account information.
                </p>
            </div>

            <AccountInfoCards
                account={account}
                statusStyle={statusStyle}
            />

            <hr
                style={{
                    border: "none",
                    borderTop: "1px solid #e5e7eb",
                    margin: "28px 0",
                }}
            />

            <div style={styles.actionSection}>
                <h4 style={styles.actionTitle}>
                    Account Actions
                </h4>

                <p style={styles.actionSubtitle}>
                    Freeze or unfreeze this account when required.
                </p>
            </div>

            <div style={styles.detailsActions}>

                <button
                    onClick={(e) => {
                        e.stopPropagation();

                        if (transactionAccountId === account.id) {
                            setTransactionAccountId(null);
                            return;
                        }

                        setTransactionAccountId(account.id);

                        if (!accountTransactions[account.accountNumber]) {
                            loadAccountTransactions(
                                account.accountNumber,
                                0
                            );
                        }
                    }}
                    style={styles.transactionButton}
                >
                    {
                        transactionAccountId === account.id
                            ? "Hide Transactions"
                            : "View Transactions"
                    }

                </button>


                <button
                    onClick={(e) => {
                        e.stopPropagation();
                        handleStatusClick(account);
                    }}
                    style={
                        account.accountStatus === "ACTIVE"
                            ? styles.freezeButton
                            : styles.unfreezeButton
                    }
                >
                    {
                        account.accountStatus === "ACTIVE"
                            ? "🔒 Freeze Account"
                            : "🔓 Unfreeze Account"
                    }
                </button>
            </div>
            {
                transactionAccountId === account.id && (
                    <TransactionHistory
                        account={account}
                        accountTransactions={accountTransactions}
                        transactionLoading={transactionLoading}
                        loadAccountTransactions={loadAccountTransactions}
                    />
                )
            }
        </div>
    );
};

const styles = {

    accountDetailsContainer: {
        background: "#fafafa",
        border: "1px solid #e5e7eb",
        borderRadius: "14px",
        padding: "14px",
        boxShadow: "0 2px 8px rgba(15, 23, 42, 0.05)",
    },

    detailsHeader: {
        marginBottom: "24px",
    },

    customerName: {
        margin: 0,
        fontSize: "22px",
        fontWeight: 700,
        color: "#0f172a",
    },

    detailsSubtitle: {
        marginTop: "4px",
        fontSize: "14px",
        color: "#64748b",
    },

    actionSection: {
        display: "flex",
        flexDirection: "column",
        gap: "12px",
        marginTop: "20px",
    },

    actionTitle: {
        margin: 0,
        fontSize: "17px",
        fontWeight: 700,
        color: "#0f172a",
    },

    actionSubtitle: {
        marginTop: "4px",
        fontSize: "14px",
        color: "#64748b",
    },

    detailsActions: {
        width: "100%",
        display: "flex",
        justifyContent: "flex-end",
        gap: "12px",
    },

    transactionButton: {
        background: "#2563eb",
        color: "#fff",
        border: "none",
        padding: "10px 18px",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: 600,
        minWidth: "180px",
    },

    freezeButton: {
        background: "#dc2626",
        color: "#fff",
        border: "none",
        padding: "10px 18px",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: 600,
        minWidth: "180px",
    },

    unfreezeButton: {
        background: "#16a34a",
        color: "#fff",
        border: "none",
        padding: "10px 18px",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: 600,
        minWidth: "180px",
    },

};

export default AccountDetails;