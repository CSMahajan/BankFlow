import React, { useEffect, useState } from "react";
import { getMyTransactions, getTransactionDetails } from "../../api/bankService";
import { formatDate, formatCurrency } from '../../utils/formatUtils';

const TransactionsView = ({
    accounts = [],
}) => {
    const [transactions, setTransactions] = useState([]);
    const [selectedTransactionId, setSelectedTransactionId] = useState(null);
    const [transactionDetails, setTransactionDetails] = useState(null);
    const [drawerOpen, setDrawerOpen] = useState(false);
    const [detailsLoading, setDetailsLoading] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [page, setPage] = useState(0);
    const [pageData, setPageData] = useState(null);

    const [filters, setFilters] = useState({
        accountNumber: "",
        transactionType: "",
        fromDate: "",
        toDate: "",
        search: "",
    });

    const [appliedFilters, setAppliedFilters] = useState({
        accountNumber: "",
        transactionType: "",
        fromDate: "",
        toDate: "",
        search: "",
    });

    const today = new Date().toISOString().split("T")[0];

    const loadTransactions = async (page = 0) => {
        try {
            setError("");
            setLoading(true);

            if (
                appliedFilters.fromDate &&
                appliedFilters.toDate &&
                appliedFilters.fromDate > appliedFilters.toDate
            ) {
                setError("From date cannot be after To date.");
                setTransactions([]);
                setLoading(false);
                return;
            }
            const response = await getMyTransactions({
                page,
                size: 20,

                ...(appliedFilters.accountNumber && {
                    accountNumber: appliedFilters.accountNumber,
                }),

                ...(appliedFilters.transactionType && {
                    type: appliedFilters.transactionType,
                }),

                ...(appliedFilters.fromDate && {
                    startDate: appliedFilters.fromDate,
                }),

                ...(appliedFilters.toDate && {
                    endDate: appliedFilters.toDate,
                }),

                ...(appliedFilters.search && {
                    search: appliedFilters.search,
                }),
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
    }, [
        page,
        appliedFilters,
    ]);

    useEffect(() => {
        setDrawerOpen(false);
        setSelectedTransactionId(null);
        setTransactionDetails(null);
    }, [page, appliedFilters]);

    const resetFilters = () => {
        const defaultFilters = {
            accountNumber: "",
            transactionType: "",
            fromDate: "",
            toDate: "",
            search: "",
        };

        setFilters(defaultFilters);
        setAppliedFilters(defaultFilters);
        setDrawerOpen(false);
        setSelectedTransactionId(null);
        setTransactionDetails(null);

        setPage(0);
    };

    const openTransactionDetails = async (transactionId) => {
        try {

            if (detailsLoading) return;

            setDrawerOpen(true);
            setTransactionDetails(null);
            setSelectedTransactionId(transactionId);
            setDetailsLoading(true);

            const response = await getTransactionDetails(transactionId);

            setTransactionDetails(response.data);
        } catch (err) {
            console.error(err);
            setError("Unable to load transaction details.");
        } finally {
            setDetailsLoading(false);
        }
    };

    return (
        <div style={styles.card}>

            <div style={styles.header}>

                <div style={styles.headerText}>
                    <h3 style={styles.title}>
                        📜 Transaction History
                    </h3>

                    <p style={styles.subtitle}>
                        Browse and filter transactions across your accounts
                    </p>
                </div>

            </div>


            <div style={styles.filterBar}>
                <input
                    type="text"
                    placeholder="Search transaction ID or description"
                    value={filters.search}
                    onChange={(e) =>
                        setFilters(prev => ({
                            ...prev,
                            search: e.target.value,
                        }))
                    }
                    onKeyDown={(e) => {
                        if (e.key === "Enter") {
                            setPage(0);
                            setAppliedFilters(filters);
                        }
                    }}
                    style={styles.searchInput}
                />
                <select
                    value={filters.accountNumber}
                    onChange={(e) =>
                        setFilters(prev => ({
                            ...prev,
                            accountNumber: e.target.value,
                        }))
                    }
                    style={styles.filterSelect}
                >
                    <option value="">All Accounts</option>

                    {accounts.map(account => (
                        <option
                            key={account.accountNumber}
                            value={account.accountNumber}
                        >
                            {account.accountType} • {account.accountNumber}
                        </option>
                    ))}
                </select>

                <select
                    value={filters.transactionType}
                    onChange={(e) =>
                        setFilters(prev => ({
                            ...prev,
                            transactionType: e.target.value,
                        }))
                    }
                    style={styles.filterSelect}
                >
                    <option value="">All Transactions</option>
                    <option value="CREDIT">Credits</option>
                    <option value="DEBIT">Debits</option>
                </select>

                <div style={styles.dateToolbar}>
                    <div style={styles.dateField}>
                        <span style={styles.dateLabel}>Start Date</span>
                        <input
                            type="date"
                            value={filters.fromDate}
                            max={today}
                            onChange={(e) =>
                                setFilters(prev => ({
                                    ...prev,
                                    fromDate: e.target.value,
                                }))
                            }
                            style={styles.dateInput}
                        />
                    </div>
                    <div style={styles.dateField}>
                        <span style={styles.dateLabel}>End Date</span>
                        <input
                            type="date"
                            value={filters.toDate}
                            min={filters.fromDate}
                            max={today}
                            onChange={(e) =>
                                setFilters(prev => ({
                                    ...prev,
                                    toDate: e.target.value,
                                }))
                            }
                            style={styles.dateInput}
                        />
                    </div>

                    <button
                        style={styles.applyButton}
                        onClick={() => {
                            setPage(0);
                            setAppliedFilters(filters);
                        }}
                    >
                        Apply Filters
                    </button>

                    <button
                        style={styles.resetButton}
                        onClick={resetFilters}
                    >
                        Reset
                    </button>
                </div>
            </div>

            {error && (
                <div style={styles.errorBanner}>
                    {error}
                </div>
            )}

            {transactions.length === 0 ? (
                <div style={styles.emptyState}>
                    <div style={{ fontSize: "42px" }}>📜</div>
                    <h4>No transactions matched your filters.</h4>
                    <p>
                        Try changing the filters or make your first transaction.
                    </p>
                </div>
            ) : (
                <>
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
                                                index % 2 === 0 ? "#ffffff" : "#f8fafc";
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

            {drawerOpen && (
                <div
                    style={styles.drawerOverlay}
                    onClick={() => {
                        setDrawerOpen(false);
                        setSelectedTransactionId(null);
                        setTransactionDetails(null);
                    }}
                >

                    <div
                        style={styles.drawer}
                        onClick={(e) => e.stopPropagation()}
                    >

                        <div style={styles.drawerHeader}>
                            <div>
                                <h3 style={styles.drawerTitle}>
                                    Transaction Details
                                </h3>

                                <p style={styles.drawerSubtitle}>
                                    View complete transaction information
                                </p>
                            </div>

                            <button
                                style={styles.closeButton}
                                onClick={() => {
                                    setDrawerOpen(false);
                                    setSelectedTransactionId(null);
                                    setTransactionDetails(null);
                                }}
                            >
                                ✕
                            </button>
                        </div>

                        {detailsLoading ? (

                            <div style={{
                                display: "flex",
                                justifyContent: "center",
                                alignItems: "center",
                                height: "200px"
                            }}>
                                Loading transaction...
                            </div>

                        ) : transactionDetails ? (

                            <div style={styles.detailsGrid}>

                                <div style={styles.detailItem}>
                                    <span style={styles.detailLabel}>Reference</span>
                                    <span
                                        style={{
                                            fontFamily: "monospace",
                                            fontSize: "13px",
                                            color: "#374151",
                                            wordBreak: "break-all",
                                        }}
                                    >
                                        {transactionDetails.transactionId}
                                    </span>
                                </div>

                                <div style={styles.detailItem}>
                                    <span style={styles.detailLabel}>Date</span>
                                    <span>{formatDate(transactionDetails.transactionDate)}</span>
                                </div>

                                <div style={styles.detailItem}>
                                    <span style={styles.detailLabel}>Account</span>
                                    <span>{transactionDetails.accountNumber}</span>
                                </div>

                                <div style={styles.detailItem}>
                                    <span style={styles.detailLabel}>Type</span>
                                    <span
                                        style={
                                            transactionDetails.transactionType === "CREDIT"
                                                ? styles.creditBadge
                                                : styles.debitBadge
                                        }
                                    >
                                        {transactionDetails.transactionType}
                                    </span>
                                </div>

                                <div style={styles.detailItem}>
                                    <span style={styles.detailLabel}>Amount</span>

                                    <span
                                        style={{
                                            fontSize: "22px",
                                            fontWeight: 700,
                                            color:
                                                transactionDetails.transactionType === "CREDIT"
                                                    ? "#15803d"
                                                    : "#dc2626",
                                        }}
                                    >
                                        {transactionDetails.transactionType === "CREDIT"
                                            ? "+"
                                            : "-"}
                                        {formatCurrency(transactionDetails.amount)}
                                    </span>
                                </div>

                                <div style={styles.detailItem}>
                                    <span style={styles.detailLabel}>Available Balance</span>
                                    <span>{formatCurrency(transactionDetails.availableBalance)}</span>
                                </div>

                                <div style={styles.detailItem}>
                                    <span style={styles.detailLabel}>Description</span>
                                    <span>{transactionDetails.description || "-"}</span>
                                </div>

                            </div>

                        ) : (

                            <p>No details found.</p>

                        )}

                    </div>

                </div>
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
        alignItems: "flex-start",
        gap: "24px",
        marginBottom: "24px",
        flexWrap: "wrap",
    },

    title: {
        margin: 0,
        marginBottom: "4px",
        fontSize: "24px",
        fontFamily: "Georgia, serif",
        whiteSpace: "nowrap",
    },

    subtitle: {
        margin: 0,
        color: "#6b7280",
        fontSize: "15px",
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
        boxShadow: "inset 4px 0 0 transparent",
    },

    filterSelect: {
        width: "240px",
        padding: "10px 14px",
        borderRadius: "8px",
        border: "1px solid #d1d5db",
        backgroundColor: "#fff",
        fontSize: "14px",
        cursor: "pointer",
    },

    headerText: {
        flex: "1 1 340px",
    },

    filterBar: {
        display: "flex",
        alignItems: "center",
        gap: "14px",
        flexWrap: "wrap",
        marginBottom: "24px",
        padding: "18px",
        border: "1px solid #e5e7eb",
        borderRadius: "12px",
        backgroundColor: "#fafafa",
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

    dateToolbar: {
        display: "flex",
        alignItems: "center",
        gap: "14px",
    },

    dateField: {
        display: "flex",
        alignItems: "center",
        gap: "8px",
    },

    dateLabel: {
        fontSize: "13px",
        fontWeight: "600",
        color: "#6b7280",
        minWidth: "36px",
    },

    dateInput: {
        width: "155px",
        padding: "10px 12px",
        borderRadius: "8px",
        border: "1px solid #d1d5db",
        fontSize: "14px",
    },

    applyButton: {
        padding: "10px 18px",
        height: "42px",
        border: "none",
        borderRadius: "8px",
        backgroundColor: "#0d6360",
        color: "#ffffff",
        cursor: "pointer",
        fontWeight: "600",
        whiteSpace: "nowrap",
    },

    resetButton: {
        padding: "10px 18px",
        height: "42px",
        borderRadius: "8px",
        border: "1px solid #d1d5db",
        backgroundColor: "#ffffff",
        color: "#374151",
        cursor: "pointer",
        fontWeight: "600",
        whiteSpace: "nowrap",
    },

    drawerOverlay: {
        position: "fixed",
        inset: 0,
        backgroundColor: "rgba(15,23,42,0.15)",
        display: "flex",
        justifyContent: "flex-end",
        alignItems: "center",
        padding: "24px",
        zIndex: 1000,
    },

    drawer: {
        width: "420px",
        maxWidth: "90vw",
        height: "84vh",
        backgroundColor: "#ffffff",
        borderRadius: "18px",
        padding: "28px",
        overflowY: "auto",
        boxShadow: "0 20px 50px rgba(0,0,0,.18)",
    },

    drawerHeader: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        marginBottom: "28px",
    },

    drawerTitle: {
        margin: 0,
        fontSize: "22px",
        fontFamily: "Georgia, serif",
    },

    drawerSubtitle: {
        marginTop: "4px",
        color: "#6b7280",
        fontSize: "14px",
    },

    closeButton: {
        border: "none",
        background: "transparent",
        cursor: "pointer",
        fontSize: "20px",
    },

    detailsGrid: {
        display: "flex",
        flexDirection: "column",
        gap: "18px",
    },

    detailItem: {
        display: "flex",
        flexDirection: "column",
        gap: "6px",
        paddingBottom: "14px",
        borderBottom: "1px solid #eef2f7",
    },

    detailLabel: {
        color: "#6b7280",
        fontSize: "12px",
        fontWeight: 700,
        textTransform: "uppercase",
        letterSpacing: ".4px",
    },

    searchInput: {
        width: "280px",
        padding: "10px 14px",
        borderRadius: "8px",
        border: "1px solid #d1d5db",
        fontSize: "14px",
        outline: "none",
    },

    errorBanner: {
        marginBottom: "20px",
        padding: "12px 16px",
        borderRadius: "8px",
        backgroundColor: "#fef2f2",
        color: "#b91c1c",
        border: "1px solid #fecaca",
        fontWeight: "500",
    },
};

export default TransactionsView;