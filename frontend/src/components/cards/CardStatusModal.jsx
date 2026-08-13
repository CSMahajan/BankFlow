import React from "react";
import { formatCurrency } from "../../utils/formatUtils";
import { getCardStatusStyle } from "../../utils/cardStatusUtils";
import CardStatusButton from "./CardStatusButton";
import modalStyles from "../../styles/modalStyles";

const CardStatusModal = ({
    selectedCard,
    showBlockModal,
    setShowBlockModal,
    actionLoading,
    handleToggleStatus,
}) => {
    return (
        <>{
            showBlockModal && (
                <div style={modalStyles.overlay}>
                    <div style={modalStyles.modal}>

                        <h3>
                            {selectedCard.cardStatus === "BLOCKED"
                                ? "Unblock Card"
                                : "Block Card"}
                        </h3>

                        <p style={{ color: "#64748b", marginBottom: "20px" }}>
                            Please review the card details before continuing.
                        </p>

                        <div style={styles.confirmationCard}>

                            <div style={styles.confirmationRow}>
                                <strong>Customer</strong>
                                <span>{selectedCard.customerName}</span>
                            </div>

                            <div style={styles.confirmationRow}>
                                <strong>Account Number</strong>
                                <span
                                    style={{
                                        fontFamily: "monospace",
                                    }}
                                >
                                    {selectedCard.accountNumber}
                                </span>
                            </div>
                            <div style={styles.confirmationRow}>
                                <strong>Card Number</strong>
                                <span
                                    style={{
                                        fontFamily: "monospace",
                                    }}
                                >
                                    {selectedCard.maskedCardNumber}
                                </span>
                            </div>

                            <div style={styles.confirmationRow}>
                                <strong>Card Type</strong>
                                <span>{selectedCard.cardType}</span>
                            </div>

                            <div style={styles.confirmationRow}>
                                <strong>Daily Limit</strong>
                                <span>
                                    {formatCurrency(selectedCard.dailyLimit)}
                                </span>
                            </div>

                        </div>

                        <div
                            style={{
                                ...styles.alertCard,
                                ...(selectedCard.cardStatus === "BLOCKED"
                                    ? styles.successAlert
                                    : styles.warningAlert),
                            }}
                        >
                            {selectedCard.cardStatus === "BLOCKED" ? (
                                <>
                                    <strong>ℹ️ Information</strong>
                                    <br />
                                    Unblocking this card will immediately allow the customer to use the card again for purchases, ATM withdrawals and online transactions.
                                </>
                            ) : (
                                <>
                                    <strong>⚠️ Important</strong>
                                    <br />
                                    You are about to change the status of this card.
                                    Blocking immediately prevents the card from being used for purchases, ATM withdrawals and online transactions.
                                    Only unblock the card after confirming it is safe to reactivate.
                                </>
                            )}
                        </div>

                        <div
                            style={{
                                display: "flex",
                                justifyContent: "flex-end",
                                gap: "12px",
                                marginTop: "24px",
                            }}
                        >
                            <button
                                style={styles.cancelButton}
                                onClick={() => setShowBlockModal(false)}
                                onMouseEnter={(e) => {
                                    e.currentTarget.style.background = "#f8fafc";
                                }}
                                onMouseLeave={(e) => {
                                    e.currentTarget.style.background = "#ffffff";
                                }}
                            >
                                Cancel
                            </button>

                            <CardStatusButton
                                cardStatus={selectedCard.cardStatus}
                                loading={actionLoading}
                                onClick={handleToggleStatus}
                            />

                        </div>

                    </div>
                </div>
            )
        }
        </>
    );
};
const styles = {

    cancelButton: {
        background: "#ffffff",
        color: "#475569",
        border: "1px solid #cbd5e1",
        padding: "10px 18px",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: 600,
        minWidth: "120px",
    },

    confirmationCard: {
        border: "1px solid #e5e7eb",
        borderRadius: "10px",
        padding: "16px",
        display: "flex",
        flexDirection: "column",
        gap: "14px",
        background: "#fafafa",
    },

    confirmationRow: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
    },

    alertCard: {
        marginTop: "20px",
        padding: "14px 16px",
        borderRadius: "10px",
        fontSize: "14px",
        lineHeight: "1.6",
    },

    warningAlert: {
        background: "#FEFCE8",
        border: "1px solid #FDE68A",
        color: "#92400e",
    },

    successAlert: {
        background: "#ECFDF5",
        border: "1px solid #A7F3D0",
        color: "#15803d",
    },

};
export default CardStatusModal;