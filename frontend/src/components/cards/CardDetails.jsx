import React from "react";
import { formatDate, formatCurrency } from "../../utils/formatUtils";
import { getCardStatusStyle } from "../../utils/cardStatusUtils";
import CardStatusButton from "./CardStatusButton";

const CardDetails = ({
    card,
    handleStatusClick,
}) => {
    return (
        <div style={styles.cardDetailsContainer}>
            <div style={styles.detailsHeader}>
                <div>
                    <h3 style={styles.customerName}>
                        👤 {card.customerName}
                    </h3>
                    <p style={styles.detailsSubtitle}>
                        Manage card status and review card information.
                    </p>
                </div>
            </div>
            <div style={styles.cardDetailsGrid}>
                <div
                    style={{
                        ...styles.detailCard,
                        background: "#eff6ff",
                        border: "1px solid #bfdbfe",
                    }}
                >
                    <div style={styles.detailLabel}>
                        💳 Daily Limit
                    </div>
                    <div
                        style={{
                            ...styles.detailValue,
                            fontSize: "22px",
                            fontWeight: 700,
                            color: "#1d4ed8",
                        }}
                    >
                        {formatCurrency(card.dailyLimit)}
                    </div>
                </div>
                <div
                    style={{
                        ...styles.detailCard,
                        ...getCardStatusStyle(card.cardStatus),
                    }}
                >
                    <div style={styles.detailLabel}>
                        {getCardStatusStyle(card.cardStatus).icon} Status
                    </div>
                    <div
                        style={{
                            ...styles.detailValue,
                            color:
                                getCardStatusStyle(card.cardStatus).color,
                            fontWeight: 700,
                        }}
                    >
                        {card.cardStatus}
                    </div>
                </div>
                <div style={styles.detailCard}>
                    <div style={styles.detailLabel}>
                        🔢 Account Number
                    </div>
                    <div
                        style={{
                            ...styles.detailValue,
                            fontFamily: "monospace",
                        }}
                    >
                        {card.accountNumber}
                    </div>
                </div>
                <div style={styles.detailCard}>
                    <div style={styles.detailLabel}>
                        💳 Card Number
                    </div>
                    <div style={styles.detailValue}>
                        {card.maskedCardNumber}
                    </div>
                </div>
                <div style={styles.detailCard}>
                    <div style={styles.detailLabel}>
                        📄 Card Type
                    </div>
                    <div style={styles.detailValue}>
                        {card.cardType}
                    </div>
                </div>
                <div style={styles.detailCard}>
                    <div style={styles.detailLabel}>
                        📅 Expiry Date
                    </div>
                    <div style={styles.detailValue}>
                        {formatDate(card.expiryDate)}
                    </div>
                </div>
            </div>
            <hr
                style={{
                    border: "none",
                    borderTop: "1px solid #e5e7eb",
                    margin: "28px 0",
                }}
            />
            <div style={styles.actionSection}>
                <div style={styles.actionInfo}>
                    <h4 style={styles.actionTitle}>
                        Card Actions
                    </h4>
                    <p style={styles.actionSubtitle}>
                        Block or unblock this card when required.
                    </p>
                </div>
            </div>
            <div style={styles.detailsActions}>
                <CardStatusButton
                    cardStatus={card.cardStatus}
                    onClick={() => handleStatusClick(card)}
                />
            </div>
        </div>
    );
};
const styles = {
    cardDetailsContainer: {
        background: "#ffffff",
        border: "1px solid #e5e7eb",
        borderRadius: "14px",
        padding: "14px",
        boxShadow: "0 2px 8px rgba(15, 23, 42, 0.05)",
    },
    cardDetailsGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(2, 1fr)",
        gap: "20px 32px",
    },
    detailCard: {
        background: "#ffffff",
        border: "1px solid #e5e7eb",
        borderRadius: "10px",
        padding: "16px",
    },
    detailLabel: {
        fontSize: "13px",
        color: "#64748b",
        marginBottom: "6px",
    },
    detailValue: {
        fontWeight: 600,
        color: "#0f172a",
        fontSize: "15px",
    },
    detailsHeader: {
        marginBottom: "24px",
    },
    detailsSubtitle: {
        marginTop: "4px",
        fontSize: "14px",
        color: "#64748b",
    },
    customerName: {
        margin: 0,
        fontSize: "22px",
        fontWeight: 700,
        color: "#0f172a",
    },
    actionSection: {
        display: "flex",
        flexDirection: "column",
        gap: "12px",
        marginTop: "20px",
    },
    actionInfo: {
        display: "flex",
        flexDirection: "column",
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
    }
}
export default CardDetails;