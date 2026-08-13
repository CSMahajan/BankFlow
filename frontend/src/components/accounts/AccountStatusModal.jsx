import React from "react";
import { formatCurrency } from "../../utils/formatUtils";
import modalStyles from "../../styles/modalStyles";

const AccountStatusModal = ({
    showStatusModal,
    selectedAccount,
    setShowStatusModal,
    setSelectedAccount,
    handleToggleStatus,
}) => {

    if (!showStatusModal || !selectedAccount) {
        return null;
    }


    const isActive = selectedAccount.accountStatus === "ACTIVE";


    return (
        <div style={modalStyles.overlay}>
            <div style={modalStyles.modal}>

                <h3>
                    {isActive
                        ? "🔒 Freeze Account"
                        : "🔓 Unfreeze Account"}
                </h3>


                <p style={{ color: "#64748b", marginBottom: "20px" }}>
                    Please review the account details before continuing.
                </p>


                <div style={styles.confirmationCard}>

                    <div style={styles.confirmationRow}>
                        <strong>Customer</strong>
                        <span>
                            {selectedAccount.customerName}
                        </span>
                    </div>


                    <div style={styles.confirmationRow}>
                        <strong>Account Number</strong>

                        <span style={{
                            fontFamily: "monospace",
                        }}>
                            {selectedAccount.accountNumber}
                        </span>

                    </div>


                    <div style={styles.confirmationRow}>
                        <strong>Account Type</strong>
                        <span>
                            {selectedAccount.accountType}
                        </span>
                    </div>


                    <div style={styles.confirmationRow}>
                        <strong>Available Balance</strong>
                        <span>
                            {formatCurrency(
                                selectedAccount.currentBalance
                            )}
                        </span>
                    </div>

                </div>


                <div
                    style={{
                        ...styles.alertCard,
                        ...(isActive
                            ? styles.warningAlert
                            : styles.successAlert),
                    }}
                >

                    {isActive ? (
                        <>
                            <strong>⚠️ Important</strong>
                            <br />

                            Freezing this account will temporarily disable
                            all deposits, withdrawals and fund transfers
                            until it is unfrozen.

                        </>
                    ) : (
                        <>
                            <strong>ℹ️ Information</strong>
                            <br />

                            Unfreezing this account will restore all
                            banking operations immediately.

                        </>
                    )}

                </div>


                <div style={styles.actions}>

                    <button
                        style={styles.cancelButton}
                        onClick={() => setShowStatusModal(false)}
                    >
                        Cancel
                    </button>


                    <button
                        onClick={async () => {

                            const success =
                                await handleToggleStatus(selectedAccount);

                            if (success) {
                                setShowStatusModal(false);
                                setSelectedAccount(null);
                            }

                        }}
                        style={
                            isActive
                                ? styles.freezeButton
                                : styles.unfreezeButton
                        }
                    >
                        {isActive
                            ? "Freeze Account"
                            : "Unfreeze Account"}
                    </button>

                </div>

            </div>
        </div>
    );
};


const styles = {

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

    actions: {
        display: "flex",
        justifyContent: "flex-end",
        gap: "12px",
        marginTop: "24px",
    },

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


export default AccountStatusModal;