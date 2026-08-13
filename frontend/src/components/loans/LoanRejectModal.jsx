import React from "react";
import { formatCurrency } from "../../utils/formatUtils";
import { getLoanTypeIcon } from "../../utils/loanTypeUtils";
import modalStyles from "../../styles/modalStyles";


const LoanRejectModal = ({
    showRejectModal,
    selectedLoan,
    rejectionRemarks,
    setRejectionRemarks,
    closeModal,
    confirmReject,
}) => {

    if (!showRejectModal) {
        return null;
    }


    const formatLoanType = (loanType) => {

        switch (loanType) {
            case "HOME":
                return "Home";

            case "PERSONAL":
                return "Personal";

            case "VEHICLE":
                return "Vehicle";

            default:
                return loanType;
        }
    };


    return (
        <div style={modalStyles.overlay}>

            <div style={modalStyles.modal}>

                <h3 style={styles.rejectModalTitle}>
                    ⚠️ Reject Loan Application
                </h3>


                <p style={styles.rejectModalSubtitle}>
                    This action cannot be undone.
                    <br />
                    Please provide a clear reason for rejecting this application.
                </p>


                {selectedLoan && (

                    <div style={styles.loanSummaryCard}>

                        <div style={styles.loanSummaryName}>
                            👤 {selectedLoan.customerName}
                        </div>


                        <div style={styles.loanSummaryMeta}>

                            {getLoanTypeIcon(selectedLoan.loanType)}

                            {" "}

                            {formatLoanType(selectedLoan.loanType)} Loan

                            {" • "}

                            <span
                                style={{
                                    fontFamily: "monospace",
                                }}
                            >
                                {selectedLoan.loanNumber}
                            </span>

                        </div>


                        <div style={styles.loanSummaryAmount}>

                            <div style={styles.loanSummaryAmountLabel}>
                                Principal Amount
                            </div>


                            <div style={styles.loanSummaryAmountValue}>
                                {formatCurrency(
                                    selectedLoan.principalAmount
                                )}
                            </div>

                        </div>

                    </div>

                )}



                <div style={styles.rejectReasonSection}>

                    <label style={styles.rejectReasonLabel}>

                        Reason for Rejection

                        <span style={{ color: "#dc2626" }}>
                            *
                        </span>

                    </label>


                    <textarea
                        value={rejectionRemarks}
                        onChange={(e) =>
                            setRejectionRemarks(e.target.value)
                        }
                        rows={5}
                        maxLength={500}
                        placeholder={`Example:
• Income documents could not be verified.
• Credit assessment did not meet approval criteria.
• Required documents were incomplete.`}
                        style={styles.rejectTextarea}
                    />


                    <div style={{
                        ...styles.characterCount,
                        color:
                            rejectionRemarks.length > 450
                                ? "#dc2626"
                                : "#64748b",
                    }}>

                        {rejectionRemarks.length}/500 characters

                    </div>

                </div>



                <div style={styles.actions}>

                    <button
                        style={styles.cancelButton}
                        onClick={closeModal}
                    >
                        Cancel
                    </button>


                    <button
                        style={{
                            ...styles.confirmRejectButton,
                            opacity: rejectionRemarks.trim()
                                ? 1
                                : 0.5,

                            cursor: rejectionRemarks.trim()
                                ? "pointer"
                                : "not-allowed",
                        }}

                        disabled={!rejectionRemarks.trim()}

                        onClick={confirmReject}
                    >
                        ✕ Reject Loan
                    </button>

                </div>

            </div>

        </div>
    );
};



const styles = {

    rejectModalTitle: {
        margin: 0,
        fontSize: "22px",
        fontWeight: 700,
        color: "#991b1b",
    },


    rejectModalSubtitle: {
        marginTop: "10px",
        color: "#64748b",
        lineHeight: 1.5,
        marginBottom: "24px",
    },


    loanSummaryCard: {
        background: "#f8fafc",
        border: "1px solid #e2e8f0",
        borderRadius: "10px",
        padding: "16px",
        marginBottom: "20px",
    },


    loanSummaryName: {
        fontWeight: 700,
        fontSize: "17px",
        color: "#0f172a",
    },


    loanSummaryMeta: {
        marginTop: "6px",
        color: "#64748b",
        fontSize: "14px",
    },


    loanSummaryAmount: {
        marginTop: "16px",
        paddingTop: "16px",
        borderTop: "1px solid #e5e7eb",
    },


    loanSummaryAmountLabel: {
        fontSize: "13px",
        color: "#64748b",
    },


    loanSummaryAmountValue: {
        marginTop: "4px",
        fontSize: "24px",
        fontWeight: 700,
        color: "#1d4ed8",
    },


    rejectReasonSection: {
        marginTop: "24px",
    },


    rejectReasonLabel: {
        display: "block",
        marginBottom: "8px",
        fontWeight: 600,
        color: "#334155",
        fontSize: "14px",
    },


    rejectTextarea: {
        width: "100%",
        minHeight: "120px",
        resize: "vertical",
        padding: "12px",
        borderRadius: "10px",
        border: "1px solid #cbd5e1",
        fontSize: "14px",
        lineHeight: 1.5,
        outline: "none",
        boxSizing: "border-box",
    },


    characterCount: {
        marginTop: "8px",
        textAlign: "right",
        fontSize: "12px",
    },


    actions: {
        display: "flex",
        justifyContent: "flex-end",
        gap: "10px",
    },


    cancelButton: {
        background: "#ffffff",
        color: "#475569",
        border: "1px solid #cbd5e1",
        borderRadius: "8px",
        padding: "10px 18px",
        fontWeight: 600,
        cursor: "pointer",
    },


    confirmRejectButton: {
        background: "#dc2626",
        color: "#ffffff",
        border: "none",
        borderRadius: "8px",
        padding: "10px 18px",
        fontWeight: 600,
    },

};


export default LoanRejectModal;