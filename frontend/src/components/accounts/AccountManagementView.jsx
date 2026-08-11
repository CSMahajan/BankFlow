import React, { Fragment, useEffect, useState } from "react";
import { fetchAllAccounts, freezeAccount, unfreezeAccount, fetchAccountSummary } from '../../api/bankService';
import { formatDate, formatCurrency } from '../../utils/formatUtils';
import { getAccountStatusStyle } from '../../utils/accountStatusUtils';
import modalStyles from "../../styles/modalStyles";
import toast from "react-hot-toast";
import PageCard from '../PageCard';

const AccountManagementView = ({
    refreshDashboard,
}) => {
    const [accounts, setAccounts] = useState([]);
    const [pageData, setPageData] = useState(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState("");
    const [accountStatusFilter, setAccountStatusFilter] = useState("ALL");
    const [expandedAccountId, setExpandedAccountId] = useState(null);
    const [showStatusModal, setShowStatusModal] = useState(false);
    const [selectedAccount, setSelectedAccount] = useState(null);
    const [accountSummary, setAccountSummary] = useState(null);

    const handleToggleStatus = async () => {

        try {

            const updatedAccount =
                selectedAccount.accountStatus === "ACTIVE"
                    ? await freezeAccount(selectedAccount.accountNumber)
                    : await unfreezeAccount(selectedAccount.accountNumber);


            await loadAccounts();
            const summary = await fetchAccountSummary();
            setAccountSummary(summary);
            await refreshDashboard?.();
            setShowStatusModal(false);
            setSelectedAccount(null);

            toast.success(
                updatedAccount.accountStatus === "ACTIVE"
                    ? "Account unfrozen successfully."
                    : "Account frozen successfully."
            );
        } catch (err) {
            console.error(err);
            toast.error("Failed to update account status.");
        }
    };

    const handleStatusClick = (account) => {
        setSelectedAccount(account);
        setShowStatusModal(true);
    };

    const loadAccounts = async () => {

        try {

            setLoading(true);

            const response = await fetchAllAccounts({
                page: currentPage,
                size: 20,
                search,
                status: accountStatusFilter,
            });

            setAccounts(response.content);
            setPageData(response);

        } catch (err) {

            console.error(err);
            toast.error("Unable to load accounts.");

        } finally {

            setLoading(false);

        }
    };

    useEffect(() => {
        loadAccounts();
    }, [
        currentPage,
        search,
        accountStatusFilter,
    ]);

    useEffect(() => {

        const loadSummary = async () => {

            try {

                const summary = await fetchAccountSummary();

                setAccountSummary(summary);

            } catch (err) {

                console.error(err);
                toast.error("Unable to load account summary.");

            }

        };

        loadSummary();

    }, []);

    if (loading) {
        return (
            <PageCard title="🏦 Account Management">
                <p>Loading accounts...</p>
            </PageCard>
        );
    }

    const activeCount = accountSummary?.activeAccounts ?? 0;

    const frozenCount = accountSummary?.frozenAccounts ?? 0;

    const savingsCount = accountSummary?.savingsAccounts ?? 0;

    const currentCount = accountSummary?.currentAccounts ?? 0;

    const accountSummaryCards = [
        {
            title: "Active Accounts",
            icon: "🟢",
            count: activeCount,
            style: {
                background: "#ECFDF5",
                border: "1px solid #A7F3D0",
            },
        },
        {
            title: "Frozen Accounts",
            icon: "🔒",
            count: frozenCount,
            style: {
                background: "#FEF2F2",
                border: "1px solid #FECACA",
            },
        },
        {
            title: "Savings Accounts",
            icon: "💰",
            count: savingsCount,
            style: {
                background: "#EFF6FF",
                border: "1px solid #BFDBFE",
            },
        },
        {
            title: "Current Accounts",
            icon: "🏦",
            count: currentCount,
            style: {
                background: "#FEFCE8",
                border: "1px solid #FDE68A",
            },
        },
    ];

    const handleRowClick = (accountId) => {
        setExpandedAccountId(prev =>
            prev === accountId ? null : accountId
        );
    };

    return (
        <PageCard title="🏦 Account Management">

            <div style={styles.pageHeader}>

                <div>
                    <p style={styles.subtitle}>
                        View customer accounts and freeze or unfreeze them when required.
                    </p>
                </div>

                <div style={styles.pendingBadge}>
                    {accounts.length} Accounts
                </div>

            </div>

            <div style={styles.summaryGrid}>
                {accountSummaryCards.map((card) => (
                    <div
                        key={card.title}
                        style={{
                            ...styles.summaryCard,
                            ...card.style,
                        }}
                    >
                        <div style={styles.summaryTitle}>
                            <span>{card.icon}</span>
                            <span>{card.title}</span>
                        </div>
                        <div style={styles.summaryValue}>
                            {card.count}
                        </div>
                    </div>
                ))}
            </div>

            {accounts.length === 0 ? (
                <p>No accounts.</p>
            ) : (
                <>
                    <div style={styles.toolbar}>

                        <input
                            placeholder="Search customer, account number..."
                            value={search}
                            onChange={(e) => {
                                setCurrentPage(0);
                                setSearch(e.target.value);
                            }}
                            style={styles.searchInput}
                        />

                        <select
                            value={accountStatusFilter}
                            onChange={(e) => {
                                setCurrentPage(0);
                                setAccountStatusFilter(e.target.value);
                            }}
                            style={styles.filterSelect}
                        >
                            <option value="ALL">All Status</option>
                            <option value="ACTIVE">Active</option>
                            <option value="FROZEN">Frozen</option>
                            <option value="INACTIVE">Inactive</option>
                        </select>

                    </div>
                    <hr
                        style={{
                            border: "none",
                            borderTop: "1px solid #e5e7eb",
                            margin: "28px 0",
                        }}
                    />
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
                                    <th style={styles.header}>Account Number</th>
                                    <th style={styles.header}>Type</th>
                                    <th style={styles.header}>Balance</th>
                                    <th style={styles.header}>Status</th>
                                </tr>
                            </thead>

                            <tbody>
                                {accounts.map((account, index) => {
                                    const statusStyle = getAccountStatusStyle(account.accountStatus);

                                    return (
                                        <Fragment key={account.id}>
                                            <tr
                                                onClick={() => handleRowClick(account.id)}
                                                style={{
                                                    cursor: "pointer",
                                                    transition: ".15s",
                                                    background:
                                                        expandedAccountId === account.id
                                                            ? "#eff6ff"
                                                            : index % 2 === 0
                                                                ? "#ffffff"
                                                                : "#fafafa",

                                                }}
                                                onMouseEnter={(e) => {
                                                    if (expandedAccountId !== account.id) {
                                                        e.currentTarget.style.background = "#eff6ff";
                                                    }
                                                }}
                                                onMouseLeave={(e) => {
                                                    if (expandedAccountId !== account.id) {
                                                        e.currentTarget.style.background =
                                                            index % 2 === 0 ? "#ffffff" : "#fafafa";
                                                    }
                                                }}
                                            >
                                                <td style={styles.cell}>{account.customerName}</td>
                                                <td style={styles.cell}>
                                                    <span
                                                        style={{
                                                            fontFamily: "monospace",
                                                            fontWeight: 600,
                                                            color: "#15803d",
                                                        }}
                                                    >
                                                        {account.accountNumber}
                                                    </span>
                                                </td>
                                                <td style={styles.cell}>{account.accountType}</td>
                                                <td style={styles.cell}>{formatCurrency(account.currentBalance)}</td>
                                                <td style={styles.cell}>
                                                    <span
                                                        style={{
                                                            padding: "5px 10px",
                                                            borderRadius: "999px",
                                                            fontWeight: 600,
                                                            fontSize: "12px",
                                                            ...statusStyle,
                                                        }}
                                                    >
                                                        {account.accountStatus}
                                                    </span>
                                                </td>
                                            </tr>
                                            {expandedAccountId === account.id && (
                                                <tr>
                                                    <td
                                                        colSpan={5}
                                                        style={{
                                                            padding: "18px",
                                                            background: "#fff",
                                                            borderBottom: "1px solid #e5e7eb",
                                                        }}
                                                    >
                                                        <div style={styles.loanDetailsContainer}>
                                                            <div style={styles.detailsHeader}>
                                                                <div>
                                                                    <h3 style={styles.customerName}>
                                                                        👤 {account.customerName}
                                                                    </h3>

                                                                    <p style={styles.detailsSubtitle}>
                                                                        Manage account status and review account information.
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
                                                                        Account Actions
                                                                    </h4>

                                                                    <p style={styles.actionSubtitle}>
                                                                        Freeze or unfreeze this account when required.
                                                                    </p>
                                                                </div>
                                                            </div>
                                                            <div style={styles.detailsActions}>
                                                                <button
                                                                    onClick={(e) => {
                                                                        e.stopPropagation();
                                                                        handleStatusClick(account);
                                                                    }}
                                                                    style={
                                                                        account.accountStatus === "ACTIVE"
                                                                            ? styles.freezeButton
                                                                            : styles.unfreezeButton
                                                                    }
                                                                >
                                                                    {account.accountStatus === "ACTIVE"
                                                                        ? "🔒 Freeze Account"
                                                                        : "🔓 Unfreeze Account"}
                                                                </button>
                                                            </div>
                                                        </div>
                                                    </td>
                                                </tr >
                                            )}
                                        </Fragment>
                                    );
                                })}
                            </tbody>
                        </table>
                        {showStatusModal && selectedAccount && (
                            <div style={modalStyles.overlay}>
                                <div style={modalStyles.modal}>

                                    <h3>
                                        {selectedAccount.accountStatus === "ACTIVE"
                                            ? "🔒 Freeze Account"
                                            : "🔓 Unfreeze Account"}
                                    </h3>

                                    <p style={{ color: "#64748b", marginBottom: "20px" }}>
                                        Please review the account details before continuing.
                                    </p>

                                    <div style={styles.confirmationCard}>

                                        <div style={styles.confirmationRow}>
                                            <strong>Customer</strong>
                                            <span>{selectedAccount.customerName}</span>
                                        </div>

                                        <div style={styles.confirmationRow}>
                                            <strong>Account Number</strong>
                                            <span
                                                style={{
                                                    fontFamily: "monospace",
                                                }}
                                            >
                                                {selectedAccount.accountNumber}
                                            </span>
                                        </div>

                                        <div style={styles.confirmationRow}>
                                            <strong>Account Type</strong>
                                            <span>{selectedAccount.accountType}</span>
                                        </div>

                                        <div style={styles.confirmationRow}>
                                            <strong>Available Balance</strong>
                                            <span>
                                                {formatCurrency(selectedAccount.currentBalance)}
                                            </span>
                                        </div>

                                    </div>

                                    <div
                                        style={{
                                            ...styles.alertCard,
                                            ...(selectedAccount.accountStatus === "ACTIVE"
                                                ? styles.warningAlert
                                                : styles.successAlert),
                                        }}
                                    >
                                        {selectedAccount.accountStatus === "ACTIVE" ? (
                                            <>
                                                <strong>⚠️ Important</strong>
                                                <br />
                                                Freezing this account will temporarily disable all deposits,
                                                withdrawals and fund transfers until it is unfrozen.
                                            </>
                                        ) : (
                                            <>
                                                <strong>ℹ️ Information</strong>
                                                <br />
                                                Unfreezing this account will restore all banking operations
                                                immediately.
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
                                            onClick={() => setShowStatusModal(false)}
                                            onMouseEnter={(e) => {
                                                e.currentTarget.style.background = "#f8fafc";
                                            }}
                                            onMouseLeave={(e) => {
                                                e.currentTarget.style.background = "#ffffff";
                                            }}
                                        >
                                            Cancel
                                        </button>

                                        <button
                                            onClick={handleToggleStatus}
                                            style={
                                                selectedAccount.accountStatus === "ACTIVE"
                                                    ? styles.freezeButton
                                                    : styles.unfreezeButton
                                            }
                                        >
                                            {selectedAccount.accountStatus === "ACTIVE"
                                                ? "Freeze Account"
                                                : "Unfreeze Account"}
                                        </button>
                                    </div>
                                </div>
                            </div>
                        )}
                    </div >
                </>
            )
            }

            {
                pageData && pageData.totalPages > 1 && (

                    <div style={styles.pagination}>

                        <button
                            disabled={pageData.first}
                            onClick={() =>
                                setCurrentPage(prev => prev - 1)
                            }
                            style={styles.pageButton}
                        >
                            ← Previous
                        </button>


                        <span>
                            Page {pageData.number + 1}
                            {" "}of{" "}
                            {pageData.totalPages}
                        </span>


                        <button
                            disabled={pageData.last}
                            onClick={() =>
                                setCurrentPage(prev => prev + 1)
                            }
                            style={styles.pageButton}
                        >
                            Next →
                        </button>

                    </div>

                )
            }
        </PageCard>
    );
};

const styles = {

    pageHeader: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "flex-start",
        marginBottom: "28px",
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

    title: {
        margin: 0,
        fontSize: "30px",
        fontWeight: 700,
    },

    subtitle: {
        marginTop: "6px",
        color: "#64748b",
        fontSize: "15px",
    },

    pendingBadge: {
        background: "#FEF3C7",
        color: "#92400E",
        padding: "8px 14px",
        borderRadius: "999px",
        fontWeight: 700,
        fontSize: "14px",
        alignSelf: "flex-start",
    },

    cell: {
        padding: "16px",
        borderBottom: "1px solid #f1f5f9",
        fontSize: "14px",
        color: "#334155",
    },

    summaryGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(4, 1fr)",
        gap: "18px",
        marginBottom: "26px",
    },

    summaryCard: {
        border: "1px solid #e5e7eb",
        borderRadius: "12px",
        padding: "18px",
        background: "#fff",
        display: "flex",
        flexDirection: "column",
        alignItems: "flex-start",
    },

    summaryValue: {
        fontSize: "30px",
        fontWeight: 700,
        color: "#0f172a",
    },

    summaryLabel: {
        marginTop: "8px",
        color: "#64748b",
        fontSize: "14px",
    },

    toolbar: {
        display: "flex",
        justifyContent: "space-between",
        gap: "16px",
        marginBottom: "24px",
    },

    searchInput: {
        flex: 1,
        padding: "12px 14px",
        borderRadius: "10px",
        border: "1px solid #d1d5db",
        fontSize: "14px",
    },

    filterSelect: {
        width: "180px",
        padding: "12px",
        borderRadius: "10px",
        border: "1px solid #d1d5db",
        fontSize: "14px",
    },

    loanDetailsContainer: {
        background: "#ffffff",
        border: "1px solid #e5e7eb",
        borderRadius: "14px",
        padding: "14px",
        boxShadow: "0 2px 8px rgba(15, 23, 42, 0.05)",
    },

    loanDetailsGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(2, 1fr)",
        gap: "20px 32px",
    },

    detailItem: {
        display: "flex",
        flexDirection: "column",
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

    detailsActions: {
        width: "100%",
        display: "flex",
        justifyContent: "flex-end",
        gap: "12px",
    },

    detailCard: {
        background: "#ffffff",
        border: "1px solid #e5e7eb",
        borderRadius: "10px",
        padding: "16px",
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
    },

    detailsHeader: {
        marginBottom: "24px",
    },

    detailsTitle: {
        margin: 0,
        fontSize: "18px",
        fontWeight: 700,
        color: "#0f172a",
    },

    detailsSubtitle: {
        marginTop: "4px",
        fontSize: "14px",
        color: "#64748b",
    },

    summaryTitle: {
        display: "flex",
        alignItems: "center",
        gap: "8px",
        fontWeight: 600,
        color: "#334155",
        marginBottom: "18px",
    },

    summarySubtext: {
        marginTop: "6px",
        color: "#64748b",
        fontSize: "13px",
    },

    customerName: {
        margin: 0,
        fontSize: "22px",
        fontWeight: 700,
        color: "#0f172a",
    },

    actionSection: {
        display: "flex",
        flexDirection: "column",
        gap: "12px",
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
        color: "#0f172a",
    },

    actionSubtitle: {
        marginTop: "4px",
        fontSize: "14px",
        color: "#64748b",
    },

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
        color: "#64748b",
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

    confirmRejectButton: {
        background: "#dc2626",
        color: "#ffffff",
        border: "none",
        borderRadius: "8px",
        padding: "10px 18px",
        fontWeight: 600,
        cursor: "pointer",
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

    pagination: {
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        gap: "16px",
        marginTop: "24px",
    },

    pageButton: {
        padding: "8px 16px",
        borderRadius: "8px",
        border: "1px solid #d1d5db",
        background: "#fff",
        cursor: "pointer",
        fontWeight: 600,
    },
};

export default AccountManagementView;