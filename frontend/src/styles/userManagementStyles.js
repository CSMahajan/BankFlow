const styles = {

    toolbar: {
        display: "flex",
        justifyContent: "space-between",
        gap: "16px",
        marginBottom: "24px",
    },

    searchContainer: {
        display: "flex",
        flex: 1,
        gap: "8px",
    },

    searchInput: {
        flex: 1,
        padding: "12px 14px",
        borderRadius: "10px",
        border: "1px solid #d1d5db",
        fontSize: "14px",
        outline: "none",
    },

    clearButton: {
        width: "45px",
        borderRadius: "10px",
        border: "1px solid #d1d5db",
        background: "#fff",
        cursor: "pointer",
        fontSize: "18px"
    },

    roleFilterContainer: {
        display: "flex",
        gap: "10px",
        marginBottom: "20px",
    },

    roleButton: {
        padding: "8px 16px",
        borderRadius: "20px",
        border: "1px solid #cbd5e1",
        cursor: "pointer",
        fontWeight: 600,
        fontSize: "14px",
    },

    activeRoleButton: {
        backgroundColor: "#1e293b",
        color: "#fff",
    },

    inactiveRoleButton: {
        backgroundColor: "#fff",
        color: "#334155",
    },


    tableContainer: {
        overflowX: "auto",
        border: "1px solid #e2e8f0",
        borderRadius: "10px",
    },


    table: {
        width: "100%",
        borderCollapse: "collapse",
    },


    row: {
        cursor: "pointer",
        transition: ".18s",
    },


    selectedRow: {
        background: "#eef6ff",
    },


    normalRow: {
        background: "#fff",
    },


    noDataContainer: {
        padding: "40px",
        textAlign: "center",
    },


    noDataTitle: {
        color: "#334155",
        fontSize: "20px",
        fontWeight: 700,
    },


    noDataText: {
        color: "#64748b",
        marginTop: "8px",
    },


    pagination: {
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        gap: "18px",
        marginTop: "28px",
        paddingBottom: "10px",
    },


    pageButton: {
        padding: "10px 18px",
        borderRadius: "10px",
        border: "1px solid #cbd5e1",
        background: "#ffffff",
        color: "#334155",
        cursor: "pointer",
        fontWeight: 600,
        fontSize: "14px",
        transition: "all .18s ease",
    },


    pageButtonHover: {
        background: "#f8fafc",
    },


    disabledPageButton: {
        opacity: 0.5,
        cursor: "not-allowed",
    },


    pageInfo: {
        fontSize: "14px",
        fontWeight: 600,
        color: "#475569",
        minWidth: "120px",
        textAlign: "center",
    },

    drawerDivider: {
        border: "none",
        borderTop: "1px solid #e5e7eb",
        margin: "24px 0",
    },


    sectionCard: {
        background: "#fff",
        border: "1px solid #e5e7eb",
        borderRadius: "12px",
        padding: "16px",
        marginBottom: "16px",
    },


    sectionLoading: {
        textAlign: "center",
        padding: "20px",
        color: "#64748b",
    },


    emptySection: {
        padding: "20px",
        textAlign: "center",
        color: "#64748b",
        fontSize: "14px",
        border: "1px dashed #d1d5db",
        borderRadius: "10px",
        background: "#f8fafc",
    },


    cardItem: {
        border: "1px solid #e5e7eb",
        borderRadius: "12px",
        padding: "16px",
        marginBottom: "12px",
        background: "#fafafa",
    },


    loanItem: {
        border: "1px solid #e5e7eb",
        borderRadius: "12px",
        padding: "16px",
        marginBottom: "12px",
        background: "#fafafa",
    },


    fdItem: {
        border: "1px solid #e5e7eb",
        borderRadius: "12px",
        padding: "16px",
        marginBottom: "12px",
        background: "#fafafa",
    },


    itemTitle: {
        fontWeight: 700,
        color: "#1e293b",
    },


    itemSubText: {
        marginTop: "5px",
        fontSize: "13px",
        color: "#64748b",
    },


    itemValue: {
        fontWeight: 700,
        fontSize: "18px",
        color: "#0f172a",
    },

    toolbar: {
        display: "flex",
        justifyContent: "space-between",
        gap: "16px",
        marginBottom: "24px",
    },


    searchWrapper: {
        display: "flex",
        flex: 1,
        gap: "8px",
        position: "relative",
    },


    searchInput: {
        flex: 1,
        padding: "12px 14px",
        borderRadius: "10px",
        border: "1px solid #d1d5db",
        fontSize: "14px",
        outline: "none",
    },


    searchStatus: {
        position: "absolute",
        right: "15px",
        top: "12px",
        color: "#64748b",
        fontSize: "13px",
    },


    clearButton: {
        width: "45px",
        borderRadius: "10px",
        border: "1px solid #d1d5db",
        background: "#fff",
        cursor: "pointer",
        fontSize: "18px",
    },


    filterSelect: {
        width: "180px",
        padding: "12px",
        borderRadius: "10px",
        border: "1px solid #d1d5db",
        fontSize: "14px",
    },


    resultInfo: {
        marginBottom: "16px",
        color: "#64748b",
        fontSize: "14px",
        fontWeight: 600,
    },

    resultRow: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        marginBottom: "16px",
    },


    loadingText: {
        color: "#64748b",
        fontSize: "13px",
        fontWeight: 600,
    },
};

export default styles;