import React from "react";
import TransferForm from "./TransferForm";
import TransactionsView from "./TransactionsView";

const PaymentsView = ({
    activeTab,
    accounts,
    refreshAccounts,
    refreshSummary,
}) => {

    return (
        <div style={styles.container}>

            <h2 style={styles.heading}>
                Payments
            </h2>

            <p style={styles.subtitle}>
                Transfer money, manage scheduled transfers and view transaction history.
            </p>

            {activeTab === "transfer" && (
                <div style={styles.card}>
                    <h3 style={styles.cardTitle}>💸 Transfer Money</h3>

                    <TransferForm
                        accounts={accounts}
                        onSuccess={async () => {
                            await refreshAccounts();
                            await refreshSummary();
                        }}
                    />
                </div>
            )}

            {activeTab === "scheduled" && (
                <div style={styles.card}>
                    <h3 style={styles.cardTitle}>🔁 Scheduled Transfers</h3>

                    <p>Coming next...</p>
                </div>
            )}

            {activeTab === "transactions" && (
                <TransactionsView
                    accounts={accounts}
                />
            )}

        </div>
    );
};

const styles = {
    container: {
        display: "flex",
        flexDirection: "column",
        gap: "24px",
    },

    heading: {
        margin: 0,
        fontSize: "28px",
        fontWeight: 700,
    },

    subtitle: {
        color: "#6b7280",
        marginTop: "-10px",
    },

    card: {
        backgroundColor: "#ffffff",
        borderRadius: "16px",
        padding: "32px",
        maxWidth: "600px",
        border: "1px solid #eef0ec",
    },

    cardTitle: {
        marginTop: 0,
        marginBottom: "20px",
    },
};

export default PaymentsView;