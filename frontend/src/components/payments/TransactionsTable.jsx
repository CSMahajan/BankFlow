import React from "react";
import { formatDate, formatCurrency } from "../../utils/formatUtils";
import styles from "./transactionStyles";

const TransactionsTable = ({
    transactions,
    selectedTransactionId,
    openTransactionDetails,
}) => {

    return (
        <table style={styles.table}>

            <thead>
                <tr>
                    <th style={{ ...styles.th, fontWeight: 700 }}>
                        Date
                    </th>

                    <th style={{ ...styles.th, fontWeight: 700 }}>
                        Account
                    </th>

                    <th style={{ ...styles.th, fontWeight: 700 }}>
                        Type
                    </th>

                    <th
                        style={{
                            ...styles.th,
                            fontWeight: 700,
                            textAlign: "right",
                        }}
                    >
                        Amount
                    </th>

                    <th
                        style={{
                            ...styles.th,
                            fontWeight: 700,
                            textAlign: "right",
                        }}
                    >
                        Balance
                    </th>

                    <th style={{ ...styles.th, fontWeight: 700 }}>
                        Description
                    </th>
                </tr>
            </thead>

            <tbody>

                {transactions.map((tx, index) => (

                    <tr
                        key={tx.transactionId}
                        onClick={() => openTransactionDetails(tx.transactionId)}
                        style={{
                            ...styles.row,
                            backgroundColor:
                                selectedTransactionId === tx.transactionId
                                    ? "#e8f1ff"
                                    : index % 2 === 0
                                        ? "#ffffff"
                                        : "#f8fafc",

                            borderLeft:
                                selectedTransactionId === tx.transactionId
                                    ? "4px solid #2563eb"
                                    : "4px solid transparent",
                        }}
                        onMouseEnter={(e) => {
                            if (selectedTransactionId !== tx.transactionId) {
                                e.currentTarget.style.background = "#eef6ff";
                                e.currentTarget.style.cursor = "pointer";
                                e.currentTarget.style.transition = "all .18s ease";
                            }
                        }}
                        onMouseLeave={(e) => {
                            if (selectedTransactionId === tx.transactionId) {
                                e.currentTarget.style.background = "#dbeafe";
                            } else {
                                e.currentTarget.style.background =
                                    index % 2 === 0
                                        ? "#ffffff"
                                        : "#f8fafc";
                            }
                        }}
                    >

                        <td
                            style={{
                                ...styles.td,
                                width: "105px",
                                whiteSpace: "nowrap",
                            }}
                        >
                            {formatDate(tx.transactionDate)}
                        </td>

                        <td
                            style={{
                                ...styles.td,
                                width: "150px",
                                fontFamily: "monospace",
                                fontSize: "13px",
                            }}
                        >
                            {tx.accountNumber}
                        </td>

                        <td style={styles.td}>
                            <span
                                style={
                                    tx.transactionType === "CREDIT"
                                        ? styles.creditBadge
                                        : styles.debitBadge
                                }
                            >
                                {tx.transactionType}
                            </span>
                        </td>

                        <td
                            style={{
                                ...styles.td,
                                color:
                                    tx.transactionType === "CREDIT"
                                        ? "#15803d"
                                        : "#dc2626",
                                fontWeight: 700,
                                textAlign: "right",
                                fontVariantNumeric: "tabular-nums",
                            }}
                        >
                            {tx.transactionType === "CREDIT" ? "+" : "-"}
                            {formatCurrency(tx.amount)}
                        </td>

                        <td
                            style={{
                                ...styles.td,
                                fontWeight: 700,
                                textAlign: "right",
                                color: "#1f2937",
                                fontVariantNumeric: "tabular-nums",
                            }}
                        >
                            {formatCurrency(tx.availableBalance)}
                        </td>

                        <td
                            style={{
                                ...styles.td,
                                maxWidth: "340px",
                                color: "#374151",
                                wordBreak: "break-word",
                                whiteSpace: "normal",
                            }}
                        >
                            {tx.description || "-"}
                        </td>

                    </tr>

                ))}

            </tbody>

        </table>
    );
};

export default TransactionsTable;