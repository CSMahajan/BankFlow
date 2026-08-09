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

    drawerLoading: {
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        height: "200px",
    },

    exportButton: {
        padding: "10px 18px",
        border: "none",
        borderRadius: "8px",
        backgroundColor: "#0d6360",
        color: "#ffffff",
        cursor: "pointer",
        fontWeight: "600",
        whiteSpace: "nowrap",
    },

    exportMenu: {
        position: "absolute",
        top: "46px",
        right: 0,
        minWidth: "190px",
        backgroundColor: "#ffffff",
        border: "1px solid #d1d5db",
        borderRadius: "10px",
        boxShadow: "0 8px 20px rgba(0,0,0,0.12)",
        overflow: "hidden",
        zIndex: 100,
    },

    exportMenuItem: {
        width: "100%",
        padding: "12px 16px",
        border: "none",
        backgroundColor: "#ffffff",
        textAlign: "left",
        cursor: "pointer",
        fontSize: "14px",
        opacity: loading ? 0.6 : 1,
        cursor: loading ? "not-allowed" : "pointer",
    },
};

export default styles;