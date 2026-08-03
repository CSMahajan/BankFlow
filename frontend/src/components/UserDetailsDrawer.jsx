import React from "react";
import { formatDate, formatCurrency } from "../utils/formatUtils";

const UserDetailsDrawer = ({
    open,
    loading,
    user,
    onClose,
}) => {

    if (!open) return null;

    return (
        <div
            style={styles.overlay}
            onClick={onClose}
        >
            <div
                style={styles.drawer}
                onClick={(e) => e.stopPropagation()}
            >
                <div style={styles.drawerTopBar}>
                    <button
                        onClick={onClose}
                        style={styles.closeBtn}
                    >
                        ✕
                    </button>
                </div>

                {loading ? (
                    <div style={styles.loading}>
                        Loading...
                    </div>
                ) : user ? (
                    <>
                        <div style={styles.profileRow}>
                            <div style={styles.avatar}>
                                {user.fullName.charAt(0).toUpperCase()}
                            </div>
                            <div style={styles.profileInfo}>
                                <h3 style={styles.userName}>
                                    {user.fullName}
                                </h3>
                            </div>
                            <span
                                style={{
                                    ...styles.roleBadge,
                                    background:
                                        user.role === "ADMIN"
                                            ? "#dbeafe"
                                            : "#dcfce7",
                                    color:
                                        user.role === "ADMIN"
                                            ? "#1d4ed8"
                                            : "#15803d",
                                }}
                            >
                                {user.role}
                            </span>
                        </div>

                        <div style={styles.statsGrid}>
                            <div
                                style={styles.statCard}
                                onClick={() => loadCustomerAccounts(user.id)}
                                onMouseEnter={(e) => {
                                    e.currentTarget.style.transform = "translateY(-3px)";
                                    e.currentTarget.style.boxShadow = "0 10px 22px rgba(0,0,0,.08)";
                                }}
                                onMouseLeave={(e) => {
                                    e.currentTarget.style.transform = "translateY(0)";
                                    e.currentTarget.style.boxShadow = "none";
                                }}
                            >
                                <div style={styles.statIcon}>🏦</div>
                                <div style={styles.statTitle}>
                                    Accounts
                                </div>
                                <div style={styles.statValue}>
                                    {user.accountCount}
                                </div>
                                <div style={styles.statFooter}>
                                    View Details →
                                </div>
                            </div>
                            <div
                                style={styles.statCard}
                                onClick={() => loadCustomerAccounts(user.id)}
                                onMouseEnter={(e) => {
                                    e.currentTarget.style.transform = "translateY(-3px)";
                                    e.currentTarget.style.boxShadow = "0 10px 22px rgba(0,0,0,.08)";
                                }}
                                onMouseLeave={(e) => {
                                    e.currentTarget.style.transform = "translateY(0)";
                                    e.currentTarget.style.boxShadow = "none";
                                }}
                            >
                                <div style={styles.statIcon}>💳</div>
                                <div style={styles.statTitle}>
                                    Cards
                                </div>
                                <div style={styles.statValue}>
                                    {user.cardCount}
                                </div>
                                <div style={styles.statFooter}>
                                    View Details →
                                </div>
                            </div>
                            <div
                                style={styles.statCard}
                                onClick={() => loadCustomerAccounts(user.id)}
                                onMouseEnter={(e) => {
                                    e.currentTarget.style.transform = "translateY(-3px)";
                                    e.currentTarget.style.boxShadow = "0 10px 22px rgba(0,0,0,.08)";
                                }}
                                onMouseLeave={(e) => {
                                    e.currentTarget.style.transform = "translateY(0)";
                                    e.currentTarget.style.boxShadow = "none";
                                }}
                            >
                                <div style={styles.statIcon}>📄</div>
                                <div style={styles.statTitle}>
                                    Loans
                                </div>
                                <div style={styles.statValue}>
                                    {user.loanCount}
                                </div>
                                <div style={styles.statFooter}>
                                    View Details →
                                </div>
                            </div>
                            <div
                                style={styles.statCard}
                                onClick={() => loadCustomerAccounts(user.id)}
                                onMouseEnter={(e) => {
                                    e.currentTarget.style.transform = "translateY(-3px)";
                                    e.currentTarget.style.boxShadow = "0 10px 22px rgba(0,0,0,.08)";
                                }}
                                onMouseLeave={(e) => {
                                    e.currentTarget.style.transform = "translateY(0)";
                                    e.currentTarget.style.boxShadow = "none";
                                }}
                            >
                                <div style={styles.statIcon}>💰</div>
                                <div style={styles.statTitle}>
                                    Fixed Deposits
                                </div>
                                <div style={styles.statValue}>
                                    {user.fixedDepositCount}
                                </div>
                                <div style={styles.statFooter}>
                                    View Details →
                                </div>
                            </div>
                        </div>

                        <div style={styles.financialCard}>
                            <div style={styles.financialLabel}>
                                💰 Total Balance
                            </div>
                            <div style={styles.financialValue}>
                                {formatCurrency(user.totalBalance)}
                            </div>
                        </div>

                        <div style={styles.loanCard}>
                            <div style={styles.financialLabel}>
                                🏦 Outstanding Loan
                            </div>
                            <div style={styles.loanValue}>
                                {formatCurrency(user.outstandingLoanAmount)}
                            </div>
                        </div>

                        <div style={styles.infoSection}>

                            <h3 style={styles.sectionTitle}>
                                Customer Information
                            </h3>
                            <div style={styles.infoRow}>
                                <span>User ID</span>
                                <strong>{user.id}</strong>
                            </div>
                            <div style={styles.infoRow}>
                                <span>Email</span>
                                <strong>{user.email}</strong>
                            </div>
                            <div style={styles.infoRow}>
                                <span>Joined</span>
                                <strong>
                                    {formatDate(user.createdAt)}
                                </strong>
                            </div>
                        </div>

                        <div style={styles.infoSection}>

                            <h3 style={styles.sectionTitle}>
                                Quick Status
                            </h3>

                            <div style={styles.statusList}>

                                <div style={styles.statusRow}>
                                    <span>🏦 Bank Accounts</span>
                                    <span style={styles.greenStatus}>
                                        {user.accountCount > 0 ? "Available" : "None"}
                                    </span>
                                </div>

                                <div style={styles.statusRow}>
                                    <span>💳 Cards</span>
                                    <span
                                        style={
                                            user.cardCount > 0
                                                ? styles.greenStatus
                                                : styles.grayStatus
                                        }
                                    >
                                        {user.cardCount > 0 ? "Issued" : "Not Issued"}
                                    </span>
                                </div>

                                <div style={styles.statusRow}>
                                    <span>📄 Loans</span>
                                    <span
                                        style={
                                            user.loanCount > 0
                                                ? styles.orangeStatus
                                                : styles.grayStatus
                                        }
                                    >
                                        {user.loanCount > 0 ? "Exists" : "None"}
                                    </span>
                                </div>

                                <div style={styles.statusRow}>
                                    <span>💰 Fixed Deposits</span>
                                    <span
                                        style={
                                            user.fixedDepositCount > 0
                                                ? styles.greenStatus
                                                : styles.grayStatus
                                        }
                                    >
                                        {user.fixedDepositCount > 0 ? "Available" : "None"}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </>
                ) : (
                    <p>No user found.</p>
                )}
            </div>
        </div>
    );
};

const styles = {
    overlay: {
        position: "fixed",
        inset: 0,
        background: "rgba(0,0,0,.2)",
        display: "flex",
        justifyContent: "flex-end",
        zIndex: 1000,
    },

    drawer: {
        width: "430px",
        background: "#fff",
        padding: "28px",
        overflowY: "auto",
        boxShadow: "-8px 0 30px rgba(0,0,0,.15)",
    },

    header: {
        display: "flex",
        justifyContent: "space-between",
        marginBottom: "24px",
    },

    title: {
        margin: 0,
    },

    subtitle: {
        marginTop: "4px",
        color: "#6b7280",
    },

    closeBtn: {
        border: "none",
        background: "transparent",
        cursor: "pointer",
        fontSize: "20px",
    },

    loading: {
        textAlign: "center",
        padding: "60px",
    },

    profile: {
        marginBottom: "24px",
    },

    statsGrid: {
        display: "grid",
        gridTemplateColumns: "1fr 1fr",
        gap: "12px",
        marginBottom: "24px",
    },

    statIcon: {
        fontSize: "24px",
        marginBottom: "10px",
    },

    statTitle: {
        color: "#64748b",
        fontSize: "13px",
        fontWeight: 600,
        marginBottom: "8px",
    },

    statValue: {
        fontSize: "26px",
        fontWeight: 700,
        color: "#1e293b",
    },

    section: {
        marginBottom: "24px",
    },

    profileRow: {
        display: "flex",
        alignItems: "center",
        gap: "16px",
        marginBottom: "28px",
    },

    profileInfo: {
        flex: 1,
    },

    avatar: {
        width: "58px",
        height: "58px",
        borderRadius: "50%",
        background: "#0d6360",
        color: "#fff",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        fontSize: "24px",
        fontWeight: 700,
        flexShrink: 0,
    },

    userName: {
        margin: 0,
        fontSize: "20px",
        fontWeight: 700,
        color: "#1e293b",
    },

    email: {
        marginTop: "4px",
        fontSize: "14px",
        color: "#64748b",
    },

    roleBadge: {
        padding: "6px 14px",
        borderRadius: "999px",
        fontWeight: 700,
        fontSize: "12px",
        whiteSpace: "nowrap",
    },

    drawerTopBar: {
        display: "flex",
        justifyContent: "flex-end",
        marginBottom: "10px",
    },

    financialCard: {
        background: "#ecfdf5",
        border: "1px solid #bbf7d0",
        borderRadius: "14px",
        padding: "18px",
        marginBottom: "18px",
    },

    loanCard: {
        background: "#fff7ed",
        border: "1px solid #fed7aa",
        borderRadius: "14px",
        padding: "18px",
        marginBottom: "18px",
    },

    financialLabel: {
        fontSize: "13px",
        fontWeight: "600",
        color: "#64748b",
        marginBottom: "8px",
        textTransform: "uppercase",
        letterSpacing: ".5px",
    },

    financialValue: {
        fontSize: "30px",
        fontWeight: "700",
        color: "#15803d",
        wordBreak: "break-word",
    },

    loanValue: {
        fontSize: "30px",
        fontWeight: "700",
        color: "#c2410c",
        wordBreak: "break-word",
    },

    infoSection: {
        marginTop: "30px",
        borderTop: "1px solid #e5e7eb",
        paddingTop: "22px",
    },

    sectionTitle: {
        margin: "0 0 18px",
        fontSize: "18px",
        fontWeight: "700",
        color: "#1e293b",
    },

    infoRow: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        padding: "12px 0",
        borderBottom: "1px solid #f3f4f6",
        fontSize: "14px",
    },

    statusList: {
        display: "flex",
        flexDirection: "column",
        gap: "12px",
    },

    statusRow: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
    },

    greenStatus: {
        background: "#dcfce7",
        color: "#15803d",
        padding: "5px 12px",
        borderRadius: "999px",
        fontSize: "12px",
        fontWeight: 700,
    },

    orangeStatus: {
        background: "#ffedd5",
        color: "#c2410c",
        padding: "5px 12px",
        borderRadius: "999px",
        fontSize: "12px",
        fontWeight: 700,
    },

    grayStatus: {
        background: "#f3f4f6",
        color: "#6b7280",
        padding: "5px 12px",
        borderRadius: "999px",
        fontSize: "12px",
        fontWeight: 700,
    },

    statCard: {
        border: "1px solid #e5e7eb",
        borderRadius: "14px",
        padding: "18px",
        textAlign: "center",
        background: "#fff",
        cursor: "pointer",
        transition: "all .18s ease",
    },

    statFooter: {
        marginTop: "14px",
        fontSize: "12px",
        color: "#0d6360",
        fontWeight: 600,
    }
};

export default UserDetailsDrawer;