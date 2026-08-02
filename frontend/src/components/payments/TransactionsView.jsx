import React, { useEffect, useState } from "react";
import { getMyTransactions } from "../../api/bankService";
import { formatDate, formatCurrency } from '../../utils/formatUtils';

const TransactionsView = () => {

    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [transactionType, setTransactionType] = useState("");
    const [page, setPage] = useState(0);
    const [pageData, setPageData] = useState(null);
    const [searchText, setSearchText] = useState("");

    const loadTransactions = async (page = 0) => {
        try {
            setLoading(true);

            const response = await getMyTransactions({
                page,
                size: 20,
                ...(transactionType && { type: transactionType }),
            });

            setTransactions(response.data.content);
            setPageData(response.data);
        } catch (err) {
            console.error(err);
            setError("Unable to load transactions.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadTransactions(page);
    }, [page, transactionType]);

    const handleTypeChange = (e) => {
        setTransactionType(e.target.value);
        setPage(0);
    };

    if (loading) {
        return <div>Loading transactions...</div>;
    }

    if (error) {
        return <div>{error}</div>;
    }

    const filteredTransactions = transactions.filter(tx =>
        (tx.description || "")
            .toLowerCase()
            .includes(searchText.toLowerCase())
    );

    return (
        <div style={styles.card}>

            <div style={styles.header}>

                <div>
                    <h3 style={styles.title}>
                        📜 Transaction History
                    </h3>

                    <p style={styles.subtitle}>
                        Showing your latest transactions
                    </p>
                </div>

                <div style={styles.headerActions}>

                    <select
                        value={transactionType}
                        onChange={(e) => setTransactionType(e.target.value)}
                        style={styles.filterSelect}
                    >
                        <option value="">All Transactions</option>
                        <option value="CREDIT">Credits</option>
                        <option value="DEBIT">Debits</option>
                    </select>

                    <button
                        style={styles.refreshButton}
                        onClick={() => loadTransactions(page)}
                    >
                        🔄 Refresh
                    </button>

                </div>

            </div>

            {transactions.length === 0 ? (
                <div style={styles.emptyState}>
                    <div style={{ fontSize: "42px" }}>📜</div>
                    <h4>No transactions found</h4>
                    <p>
                        Try changing the filters or make your first transaction.
                    </p>
                </div>
            ) : (
                <>
                    <input
                        placeholder="Search description..."
                        value={searchText}
                        onChange={(e) => setSearchText(e.target.value)}
                        style={styles.searchInput}
                    />
                    <table style={styles.table}>

                        <thead>
                            <tr>
                                <th
                                    style={{
                                        ...styles.th,
                                        fontWeight: 700
                                    }}
                                >
                                    Date
                                </th>
                                <th
                                    style={{
                                        ...styles.th,
                                        fontWeight: 700
                                    }}
                                >
                                    Account
                                </th>
                                <th
                                    style={{
                                        ...styles.th,
                                        fontWeight: 700
                                    }}
                                >
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
                                <th
                                    style={{
                                        ...styles.th,
                                        fontWeight: 700
                                    }}
                                >
                                    Description
                                </th>
                            </tr>
                        </thead>

                        <tbody>
                            {filteredTransactions.map((tx, index) => (
                                <tr
                                    key={tx.transactionId}
                                    style={{
                                        ...styles.row,
                                        backgroundColor: index % 2 === 0 ? "#ffffff" : "#f8fafc",
                                    }}
                                    onMouseEnter={(e) =>
                                        e.currentTarget.style.background = "#eef6ff"
                                    }
                                    onMouseLeave={(e) =>
                                        e.currentTarget.style.background =
                                        index % 2 === 0 ? "#ffffff" : "#f8fafc"
                                    }
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
                                            fontVariantNumeric: "tabular-nums",
                                            textAlign: "right",
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
                    {pageData && pageData.totalPages > 1 && (
                        <div style={styles.pagination}>

                            <button
                                disabled={pageData.first}
                                onClick={() => setPage(prev => prev - 1)}
                                style={{
                                    ...styles.pageButton,
                                    opacity: pageData.first ? 0.5 : 1,
                                    cursor: pageData.first ? "not-allowed" : "pointer",
                                }}
                            >
                                ← Previous
                            </button>

                            <span style={styles.pageInfo}>
                                Page {pageData.number + 1} of {pageData.totalPages}
                            </span>

                            <button
                                disabled={pageData.last}
                                onClick={() => setPage(prev => prev + 1)}
                                style={{
                                    ...styles.pageButton,
                                    opacity: pageData.last ? 0.5 : 1,
                                    cursor: pageData.last ? "not-allowed" : "pointer",
                                }}
                            >
                                Next →
                            </button>

                        </div>
                    )}
                </>
            )}

        </div>
    );
};

const styles = {
    card: {
        backgroundColor: "#ffffff",
        borderRadius: "16px",
        padding: "32px",
        border: "1px solid #eef0ec",
        width: "100%",
        boxSizing: "border-box",
    },

    header: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        marginBottom: "20px",
    },

    subtitle: {
        marginTop: "4px",
        color: "#6b7280",
        fontSize: "16px",
    },

    title: {
        margin: 0,
        fontSize: "24px",
        fontFamily: "Georgia, serif",
    },

    table: {
        width: "100%",
        borderCollapse: "collapse",
    },

    th: {
        textAlign: "left",
        padding: "12px",
        borderBottom: "1px solid #e5e7eb",
        fontSize: "12px",
        fontWeight: "700",
        color: "#6b7280",
        textTransform: "uppercase",
        letterSpacing: "0.4px",
    },

    td: {
        padding: "14px 12px",
        borderBottom: "1px solid #f1f5f9",
        fontSize: "14px",
    },

    creditBadge: {
        backgroundColor: "#dcfce7",
        color: "#15803d",
        padding: "4px 10px",
        borderRadius: "999px",
        fontWeight: "700",
        fontSize: "12px",
    },

    debitBadge: {
        backgroundColor: "#fee2e2",
        color: "#b91c1c",
        padding: "4px 10px",
        borderRadius: "999px",
        fontWeight: "700",
        fontSize: "12px",
    },

    row: {
        transition: "background-color .2s",
        cursor: "pointer",
    },

    filterSelect: {
        padding: "10px 14px",
        borderRadius: "8px",
        border: "1px solid #d1d5db",
        backgroundColor: "#fff",
        fontSize: "14px",
        cursor: "pointer",
    },

    headerActions: {
        display: "flex",
        gap: "12px",
        alignItems: "center",
    },

    refreshButton: {
        padding: "10px 16px",
        borderRadius: "8px",
        border: "1px solid #d1d5db",
        background: "#fff",
        cursor: "pointer",
        fontWeight: "600",
    },

    pagination: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        marginTop: "24px",
    },

    pageInfo: {
        fontWeight: "600",
        color: "#374151",
    },

    pageButton: {
        padding: "8px 14px",
        border: "1px solid #d1d5db",
        backgroundColor: "#fff",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: "600",
    },

    emptyState: {
        textAlign: "center",
        padding: "60px 20px",
        color: "#6b7280",
    },

    emptyIcon: {
        fontSize: "52px",
        marginBottom: "16px",
    },

    searchInput: {
        padding: "10px 14px",
        borderRadius: "8px",
        border: "1px solid #d1d5db",
        minWidth: "240px",
    },
};

export default TransactionsView;