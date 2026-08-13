import React from "react";
import {
    formatDate,
    formatCurrency
} from "../../utils/formatUtils";


const AccountInfoCards = ({
    account,
    statusStyle
}) => {

    return (
        <div style={styles.loanDetailsGrid}>

            <div
                style={{
                    ...styles.detailCard,
                    background: "#eff6ff",
                    border: "1px solid #bfdbfe",
                }}
            >
                <div style={styles.detailLabel}>
                    💰 Available Balance
                </div>

                <div
                    style={{
                        ...styles.detailValue,
                        fontSize: "22px",
                        fontWeight: 700,
                        color: "#1d4ed8",
                    }}
                >
                    {formatCurrency(account.currentBalance)}
                </div>
            </div>


            <div
                style={{
                    ...styles.detailCard,
                    ...statusStyle,
                }}
            >
                <div style={styles.detailLabel}>
                    {statusStyle.icon} Status
                </div>

                <div
                    style={{
                        ...styles.detailValue,
                        color: statusStyle.color,
                        fontWeight: 700,
                    }}
                >
                    {account.accountStatus}
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
                    {account.accountNumber}
                </div>
            </div>


            <div style={styles.detailCard}>
                <div style={styles.detailLabel}>
                    🏦 Branch
                </div>

                <div style={styles.detailValue}>
                    {account.branchName}
                </div>
            </div>


            <div style={styles.detailCard}>
                <div style={styles.detailLabel}>
                    📄 Account Type
                </div>

                <div style={styles.detailValue}>
                    {account.accountType}
                </div>
            </div>


            <div style={styles.detailCard}>
                <div style={styles.detailLabel}>
                    📅 Created On
                </div>

                <div style={styles.detailValue}>
                    {formatDate(account.createdAt)}
                </div>
            </div>


        </div>
    );
};


const styles = {

    loanDetailsGrid: {
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
    }

};


export default AccountInfoCards;