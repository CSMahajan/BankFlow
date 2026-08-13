import React from "react";
import { formatCurrency } from "../../utils/formatUtils";
import { formatDate } from "../../utils/formatUtils";
import { getLoanTypeIcon } from "../../utils/loanTypeUtils";

const LoanDetails = ({
    loan,
    handleApprove,
    handleReject,
}) => {
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

        <div style={styles.loanDetailsContainer}>
            <div style={styles.detailsHeader}>
                <div>
                    <h3 style={styles.customerName}>
                        👤 {loan.customerName}
                    </h3>
                    <p style={styles.detailsSubtitle}>
                        {getLoanTypeIcon(loan.loanType)}{" "}
                        {formatLoanType(loan.loanType)} Loan
                        {" • "}
                        <span
                            style={{
                                fontFamily: "monospace",
                                fontWeight: 600,
                            }}
                        >
                            {loan.loanNumber}
                        </span>
                    </p>
                </div>
            </div>
            <div style={styles.loanDetailsGrid}>
                <div
                    style={{
                        ...styles.detailCard,
                        background: "#eff6ff",
                        border: "1px solid #bfdbfe",
                    }}
                >
                    <div style={styles.detailLabel}>💰 Principal Amount</div>
                    <div
                        style={{
                            ...styles.detailValue,
                            fontSize: "20px",
                            fontWeight: 700,
                            color: "#1d4ed8",
                        }}
                    >
                        {formatCurrency(loan.principalAmount)}
                    </div>
                </div>
                <div
                    style={{
                        ...styles.detailCard,
                        background: "#ecfdf5",
                        border: "1px solid #a7f3d0",
                    }}
                >
                    <div style={styles.detailLabel}>💵 Monthly EMI</div>
                    <div style={{
                        ...styles.detailValue,
                        fontSize: "18px",
                        color: "#0f766e",
                    }}
                    >
                        {formatCurrency(loan.monthlyEmi)}
                    </div>
                </div>
                <div style={styles.detailCard}>
                    <div style={styles.detailLabel}>🏦 Account Number</div>
                    <div style={{
                        ...styles.detailValue,
                        fontFamily: "monospace",
                    }}
                    >
                        {loan.accountNumber}</div>
                </div>
                <div style={styles.detailCard}>
                    <div style={styles.detailLabel}>📈 Interest Rate</div>
                    <div style={styles.detailValue}>{loan.annualInterestRate}%</div>
                </div>
                <div style={styles.detailCard}>
                    <div style={styles.detailLabel}>📅 Tenure</div>
                    <div style={styles.detailValue}>
                        {loan.tenureMonths} Months
                    </div>
                </div>
                <div style={styles.detailCard}>
                    <div style={styles.detailLabel}>📝 Applied On</div>
                    <div style={styles.detailValue}>
                        {formatDate(loan.applicationDate)}
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
                        Decision
                    </h4>
                    <p style={styles.actionSubtitle}>
                        Approve or reject this loan application.
                    </p>
                </div>
            </div>
            <div style={styles.detailsActions}>
                <button
                    style={styles.approveButton}
                    onClick={() => handleApprove(loan.id)}
                >
                    ✓ Approve Loan
                </button>
                <button
                    style={styles.rejectButton}
                    onClick={() => handleReject(loan)}
                >
                    ✕ Reject Application
                </button>
            </div>
        </div>
    );
};

const styles = {

    loanDetailsContainer: {
        background: "#ffffff",
        border: "1px solid #e5e7eb",
        borderRadius: "14px",
        padding: "14px",
        boxShadow: "0 2px 8px rgba(15, 23, 42, 0.05)",
    },


    loanDetailsGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(auto-fit,minmax(250px,1fr))",
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


    approveButton: {
        background: "#16a34a",
        color: "#fff",
        border: "none",
        padding: "12px 22px",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: 600,
        minWidth: "170px",
    },


    rejectButton: {
        background: "#fff",
        color: "#dc2626",
        border: "1px solid #dc2626",
        padding: "12px 22px",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: 600,
        minWidth: "170px",
    }

};

export default LoanDetails;