import React from "react";
import {
    formatCurrency,
    formatDateTime
} from "../../utils/formatUtils";


const TransactionHistory = ({
    account,
    accountTransactions,
    transactionLoading,
    loadAccountTransactions
}) => {

    const transactions =
        accountTransactions[account.accountNumber];


    if (transactionLoading) {
        return <p>Loading transactions...</p>;
    }


    if (!transactions?.content?.length) {
        return <p>No transactions found.</p>;
    }


    return (
        <div style={styles.transactionsSection}>

            <hr
                style={{
                    border: "none",
                    borderTop: "1px solid #e5e7eb",
                    margin: "28px 0",
                }}
            />


            <h4 style={styles.actionTitle}>
                Transaction History (
                {transactions.totalElements}{" "}
                {
                    transactions.totalElements === 1
                        ? "transaction"
                        : "transactions"
                }
                )
            </h4>


            <div
                style={{
                    overflowX: "auto",
                    border: "1px solid #e5e7eb",
                    borderRadius: "12px",
                    marginTop: "16px",
                }}
            >

                <table
                    style={{
                        width: "100%",
                        borderCollapse: "collapse",
                    }}
                >

                    <thead>
                        <tr>
                            <th style={styles.header}>
                                Transaction ID
                            </th>

                            <th style={styles.header}>
                                Date & Time
                            </th>

                            <th style={styles.header}>
                                Type
                            </th>

                            <th style={styles.header}>
                                Amount
                            </th>

                            <th style={styles.header}>
                                Balance After
                            </th>

                            <th style={styles.header}>
                                Description
                            </th>
                        </tr>
                    </thead>


                    <tbody>

                        {
                            transactions.content.map(
                                (transaction, index) => (

                                    <tr
                                        key={transaction.transactionId}
                                        style={{
                                            background:
                                                index % 2 === 0
                                                    ? "#ffffff"
                                                    : "#f8fafc",
                                        }}
                                    >

                                        <td style={styles.cell}>
                                            {transaction.transactionId}
                                        </td>

                                        <td style={styles.cell}>
                                            {formatDateTime(
                                                transaction.transactionDate
                                            )}
                                        </td>


                                        <td style={styles.cell}>
                                            <span
                                                style={{
                                                    padding: "5px 10px",
                                                    borderRadius: "999px",
                                                    fontSize: "12px",
                                                    fontWeight: 700,
                                                    background:
                                                        transaction.transactionType === "CREDIT"
                                                            ? "#DCFCE7"
                                                            : "#FEE2E2",
                                                    color:
                                                        transaction.transactionType === "CREDIT"
                                                            ? "#166534"
                                                            : "#991B1B",
                                                }}
                                            >
                                                {transaction.transactionType}
                                            </span>
                                        </td>


                                        <td style={styles.cell}>
                                            {formatCurrency(transaction.amount)}
                                        </td>


                                        <td style={styles.cell}>
                                            {formatCurrency(
                                                transaction.availableBalance
                                            )}
                                        </td>


                                        <td style={styles.cell}>
                                            {transaction.description}
                                        </td>

                                    </tr>

                                ))
                        }

                    </tbody>

                </table>

            </div>


            {
                transactions.totalPages > 1 && (

                    <div style={styles.transactionPagination}>

                        <button
                            disabled={transactions.first}
                            onClick={() =>
                                loadAccountTransactions(
                                    account.accountNumber,
                                    transactions.number - 1
                                )
                            }
                            style={styles.transactionPageButton}
                        >
                            ← Previous Tx
                        </button>


                        <span style={styles.transactionPageInfo}>
                            Transactions Page{" "}
                            {transactions.number + 1}
                            {" "}of{" "}
                            {transactions.totalPages}
                        </span>


                        <button
                            disabled={transactions.last}
                            onClick={() =>
                                loadAccountTransactions(
                                    account.accountNumber,
                                    transactions.number + 1
                                )
                            }
                            style={styles.transactionPageButton}
                        >
                            Next Tx →
                        </button>

                    </div>
                )
            }

        </div>
    );
};


const styles = {

    transactionsSection: {
        marginTop: "20px",
    },


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


    actionTitle: {
        margin: 0,
        fontSize: "17px",
        fontWeight: 700,
        color: "#0f172a",
    },


    transactionPagination: {
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        gap: "12px",
        marginTop: "18px",
        padding: "12px",
        background: "#f8fafc",
        borderRadius: "10px",
        border: "1px solid #e2e8f0",
    },


    transactionPageButton: {
        padding: "6px 14px",
        borderRadius: "20px",
        border: "1px solid #93c5fd",
        background: "#eff6ff",
        color: "#1d4ed8",
        cursor: "pointer",
        fontWeight: 600,
        fontSize: "13px",
    },


    transactionPageInfo: {
        padding: "6px 14px",
        borderRadius: "20px",
        background: "#dbeafe",
        color: "#1e40af",
        fontWeight: 700,
        fontSize: "13px",
    }

};


export default TransactionHistory;