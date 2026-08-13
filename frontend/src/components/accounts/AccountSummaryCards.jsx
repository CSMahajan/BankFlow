import React from "react";

const AccountSummaryCards = ({
    accountSummary
}) => {

    const accountSummaryCards = [
        {
            title: "Active Accounts",
            icon: "🟢",
            count: accountSummary?.activeAccounts ?? 0,
            style: {
                background: "#ECFDF5",
                border: "1px solid #A7F3D0",
            },
        },
        {
            title: "Frozen Accounts",
            icon: "🔒",
            count: accountSummary?.frozenAccounts ?? 0,
            style: {
                background: "#FEF2F2",
                border: "1px solid #FECACA",
            },
        },
        {
            title: "Savings Accounts",
            icon: "💰",
            count: accountSummary?.savingsAccounts ?? 0,
            style: {
                background: "#EFF6FF",
                border: "1px solid #BFDBFE",
            },
        },
        {
            title: "Current Accounts",
            icon: "🏦",
            count: accountSummary?.currentAccounts ?? 0,
            style: {
                background: "#FEFCE8",
                border: "1px solid #FDE68A",
            },
        },
    ];


    return (
        <div style={styles.summaryGrid}>
            {
                accountSummaryCards.map((card) => (
                    <div
                        key={card.title}
                        style={{
                            ...styles.summaryCard,
                            ...card.style,
                        }}
                    >
                        <div style={styles.summaryTitle}>
                            <span>{card.icon}</span>
                            <span>{card.title}</span>
                        </div>

                        <div style={styles.summaryValue}>
                            {card.count}
                        </div>

                    </div>
                ))
            }
        </div>
    );
};


const styles = {

    summaryGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(4, 1fr)",
        gap: "18px",
        marginBottom: "26px",
    },

    summaryCard: {
        border: "1px solid #e5e7eb",
        borderRadius: "12px",
        padding: "18px",
        background: "#fff",
        display: "flex",
        flexDirection: "column",
        alignItems: "flex-start",
    },

    summaryTitle: {
        display: "flex",
        alignItems: "center",
        gap: "8px",
        fontWeight: 600,
        color: "#334155",
        marginBottom: "18px",
    },

    summaryValue: {
        fontSize: "30px",
        fontWeight: 700,
        color: "#0f172a",
    }

};


export default AccountSummaryCards;