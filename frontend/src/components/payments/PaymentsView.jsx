import React, { useState } from "react";
import TransferForm from "./TransferForm";
import TransactionsView from "./TransactionsView";
import ScheduledTransferForm from "./ScheduledTransferForm";
import ScheduledTransfersList from "./ScheduledTransfersList";

const PaymentsView = ({
    activeTab,
    accounts,
    refreshDashboard,
}) => {
    const [scheduledRefreshKey, setScheduledRefreshKey] = useState(0);

    return (
        <div style={styles.container}>

            {activeTab === "transfer" && (
                <div style={styles.card}>
                    <h3 style={styles.cardTitle}>💸 Transfer Money</h3>

                    <TransferForm
                        accounts={accounts}
                        onSuccess={refreshDashboard}
                    />
                </div>
            )}

            {activeTab === "scheduled" && (
                <div style={styles.scheduledLayout}>

                    <div style={styles.leftPanel}>
                        <div style={styles.card}>
                            <h3 style={styles.cardTitle}>
                                🔁 Schedule Transfer
                            </h3>

                            <ScheduledTransferForm
                                accounts={accounts}
                                onSuccess={async () => {
                                    await refreshDashboard();
                                    setScheduledRefreshKey(prev => prev + 1);
                                }}
                            />
                        </div>
                    </div>

                    <div style={styles.rightPanel}>
                        <ScheduledTransfersList
                            refreshTrigger={scheduledRefreshKey}
                        />
                    </div>
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

    scheduledLayout: {
        display: "grid",
        gridTemplateColumns: "430px 1fr",
        gap: "24px",
        alignItems: "start",
    },

    leftPanel: {
        position: "sticky",
        top: "20px",
    },

    rightPanel: {
        display: "flex",
        flexDirection: "column",
        gap: "16px",
    },
};

export default PaymentsView;