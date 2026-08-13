import React from "react";
import { formatDate, formatCurrency } from "../../utils/formatUtils";
import { getLoanTypeStyle } from "../../utils/loanTypeUtils";
import LoanDetails from "./LoanDetails";

const LoanTable = ({
    loans,
    expandedLoanId,
    setExpandedLoanId,
    handleApprove,
    handleReject,
}) => {

    return (
        <div
            style={{
                overflowX: "auto",
                border: "1px solid #e5e7eb",
                borderRadius: "12px",
            }}
        >
            <table style={{
                width: '100%', borderCollapse: 'collapse'
            }}>
                <thead>
                    < tr >
                        <th style={styles.header}>Customer</th>
                        <th style={styles.header}>Type</th>
                        <th style={styles.header}>Principal Amount</th>
                        <th style={styles.header}>Application Date</th>
                        <th style={styles.header}>Status</th>
                    </tr>
                </thead>

                <tbody>
                    {loans.map((loan, index) => (
                        <React.Fragment key={loan.id}>
                            <tr
                                key={loan.id}
                                onClick={() =>
                                    setExpandedLoanId(
                                        expandedLoanId === loan.id ? null : loan.id
                                    )
                                }
                                style={{
                                    cursor: "pointer",
                                    transition: ".15s",
                                    background:
                                        expandedLoanId === loan.id
                                            ? "#eff6ff"
                                            : index % 2 === 0
                                                ? "#ffffff"
                                                : "#fafafa",

                                }}
                                onMouseEnter={(e) => {
                                    if (expandedLoanId !== loan.id) {
                                        e.currentTarget.style.background = "#eff6ff";
                                    }
                                }}
                                onMouseLeave={(e) => {
                                    if (expandedLoanId !== loan.id) {
                                        e.currentTarget.style.background =
                                            index % 2 === 0 ? "#ffffff" : "#fafafa";
                                    }
                                }}
                            >
                                <td style={styles.cell}>{loan.customerName}</td>
                                <td style={styles.cell}><span
                                    style={{
                                        padding: "4px 10px",
                                        borderRadius: "999px",
                                        fontWeight: 600,
                                        fontSize: "12px",
                                        ...getLoanTypeStyle(loan.loanType)
                                    }}
                                >
                                    {loan.loanType}
                                </span></td>
                                <td style={styles.cell}>{formatCurrency(loan.principalAmount)}</td>
                                <td style={styles.cell}>{formatDate(loan.applicationDate)}</td>
                                <td style={styles.cell}>
                                    <span
                                        style={{
                                            padding: "5px 10px",
                                            borderRadius: "999px",
                                            background: "#fef3c7",
                                            color: "#92400e",
                                            fontWeight: 600,
                                            fontSize: "12px",
                                        }}
                                    >
                                        {loan.status}
                                    </span>
                                </td>
                            </tr>
                            {expandedLoanId === loan.id && (
                                <tr>
                                    <td
                                        colSpan={5}
                                        style={styles.expandedRow}
                                    >
                                        <LoanDetails
                                            loan={loan}
                                            handleApprove={handleApprove}
                                            handleReject={handleReject}
                                        />
                                    </td>
                                </tr>
                            )}
                        </React.Fragment>
                    ))}
                </tbody>
            </table>
        </div >

    );
};

const styles = {

    header: {
        background: "#f8fafc",
        color: "#334155",
        fontWeight: 700,
        fontSize: "14px",
        padding: "14px 16px",
        textAlign: "left",
        borderBottom: "1px solid #e5e7eb",
        whiteSpace: "nowrap",
    },


    cell: {
        padding: "16px",
        borderBottom: "1px solid #f1f5f9",
        fontSize: "14px",
        color: "#334155",
    },


    expandedRow: {
        padding: "18px",
        background: "#fff",
        borderBottom: "1px solid #e5e7eb",
    }

};

export default LoanTable;