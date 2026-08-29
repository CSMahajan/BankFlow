import React from "react";

const KycSummaryCards = ({ summary }) => {

    const cards = [
        {
            title: "Total Documents",
            icon: "📄",
            count: summary?.totalDocuments ?? 0,
            style: {
                background: "#F8FAFC",
                border: "1px solid #CBD5E1"
            }
        },
        {
            title: "Pending Review",
            icon: "⏳",
            count: summary?.pendingDocuments ?? 0,
            style: {
                background: "#FEFCE8",
                border: "1px solid #FDE68A"
            }
        },
        {
            title: "Verified",
            icon: "✅",
            count: summary?.verifiedDocuments ?? 0,
            style: {
                background: "#ECFDF5",
                border: "1px solid #A7F3D0"
            }
        },
        {
            title: "Rejected",
            icon: "❌",
            count: summary?.rejectedDocuments ?? 0,
            style: {
                background: "#FEF2F2",
                border: "1px solid #FECACA"
            }
        },
        {
            title: "Pending Customers",
            icon: "👤",
            count: summary?.pendingCustomers ?? 0,
            style: {
                background: "#EFF6FF",
                border: "1px solid #BFDBFE"
            }
        }
    ];

    return (
        <div style={styles.summaryGrid}>
            {cards.map((card) => (
                <div
                    key={card.title}
                    style={{
                        ...styles.summaryCard,
                        ...card.style
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
            ))}
        </div>
    );
};

const styles = {
    summaryGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(5, 1fr)",
        gap: "18px",
        marginBottom: "26px",
        width: "100%"
    },

    summaryCard: {
        borderRadius: "12px",
        padding: "16px",
        background: "#ffffff",
        display: "flex",
        flexDirection: "column",
        alignItems: "flex-start"
    },

    summaryTitle: {
        display: "flex",
        alignItems: "center",
        gap: "8px",
        fontWeight: "600",
        color: "#334155",
        marginBottom: "18px"
    },

    summaryValue: {
        fontSize: "26px",
        fontWeight: "700",
        color: "#0f172a"
    }
};

export default KycSummaryCards;