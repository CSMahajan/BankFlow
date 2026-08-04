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
    },

    sectionTitle: {
        marginBottom: "16px",
        fontSize: "18px",
        fontWeight: 700,
    },

    accountCard: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        border: "1px solid #e5e7eb",
        borderRadius: "12px",
        padding: "16px",
        marginBottom: "12px",
        background: "#fafafa",
    },

    accountNumber: {
        marginTop: "4px",
        color: "#64748b",
        fontFamily: "monospace",
        fontSize: "13px",
    },

    accountHeader: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        marginBottom: "8px",
    },

    accountStatus: {
        padding: "4px 10px",
        borderRadius: "999px",
        fontSize: "11px",
        fontWeight: 700,
    },

    branchName: {
        marginTop: "8px",
        color: "#64748b",
        fontSize: "13px",
    },

    balanceSection: {
        textAlign: "right",
        minWidth: "130px",
    },

    balanceLabel: {
        color: "#64748b",
        fontSize: "12px",
    },

    balanceValue: {
        marginTop: "4px",
        fontWeight: 700,
        fontSize: "18px",
        color: "#0f172a",
    },

    emptyAccounts: {
        padding: "24px",
        textAlign: "center",
        color: "#6b7280",
        border: "1px dashed #d1d5db",
        borderRadius: "10px",
    },
};

export default styles;