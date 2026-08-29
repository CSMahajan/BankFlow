const styles = {

    detailsContainer: {
        padding: "16px",
        margin: "8px",
        background: "#ffffff",
        borderRadius: "12px",
        border: "1px solid #e2e8f0",
        boxShadow: "0 2px 8px rgba(0,0,0,0.04)"
    },

    customerHeading: {
        margin: "0 0 5px 0"
    },

    expandedHeader: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "flex-start",
        marginBottom: "14px"
    },

    subtitle: {
        color: "#64748b",
        margin: "0 0 10px 0",
        fontSize: "14px"
    },

    expandedStatusBadge: {
        padding: "8px 16px",
        borderRadius: "20px",
        fontSize: "13px",
        fontWeight: "700",
        textTransform: "uppercase"
    },

    rejectionInfo: {
        marginTop: "8px",
        padding: "10px 12px",
        background: "#FEF2F2",
        border: "1px solid #FECACA",
        borderRadius: "8px",
        color: "#991B1B",
        fontSize: "13px",
        display: "flex",
        gap: "8px",
        alignItems: "center"
    },

    reviewLayout: {
        display: "grid",
        gridTemplateColumns: "1fr 1fr",
        gap: "25px"
    },

    infoSection: {
        background: "#f8fafc",
        padding: "12px",
        borderRadius: "12px"
    },

    sectionTitle: {
        margin: "0 0 10px 0",
        fontSize: "16px"
    },

    detailsGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(3, 1fr)",
        gap: "14px",
        marginTop: "12px"
    },

    infoCard: {
        background: "#ffffff",
        border: "1px solid #e2e8f0",
        borderRadius: "10px",
        padding: "12px",
        display: "flex",
        flexDirection: "column",
        gap: "6px"
    },

    infoLabel: {
        color: "#64748b",
        fontSize: "12px",
        fontWeight: "600"
    },

    infoValue: {
        margin: 0,
        fontSize: "14px",
        fontWeight: "700",
        color: "#0f172a"
    },

    documentNumberValue: {
        fontSize: "16px",
        fontWeight: "800",
        color: "#0d6360",
        background: "#ecfdf5",
        padding: "8px 12px",
        borderRadius: "8px",
        letterSpacing: "1.5px",
        display: "inline-block"
    },

    retryButton: {
        marginTop: "12px",
        padding: "8px 16px",
        borderRadius: "8px",
        border: "1px solid #0d6360",
        background: "#ffffff",
        color: "#0d6360",
        cursor: "pointer",
        fontWeight: "700"
    },

    mutedText: {
        color: "#64748b",
        fontSize: "13px"
    },

    previewSection: {
        background: "#f8fafc",
        padding: "12px",
        borderRadius: "12px",
        display: "flex",
        flexDirection: "column",
        gap: "8px"
    },

    documentHeader: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        marginBottom: "10px"
    },

    previewActions: {
        display: "flex",
        gap: "8px"
    },

    documentName: {
        display: "block",
        marginTop: "4px",
        color: "#64748b",
        fontSize: "12px"
    },

    viewButton: {
        padding: "7px 12px",
        borderRadius: "7px",
        border: "1px solid #64748b",
        background: "#ffffff",
        cursor: "pointer"
    },

    downloadButton: {
        padding: "7px 14px",
        borderRadius: "7px",
        border: "1px solid #0d6360",
        background: "#ffffff",
        color: "#0d6360",
        cursor: "pointer",
        fontWeight: "700",
        fontSize: "13px",
        display: "inline-flex",
        alignItems: "center",
        gap: "6px",
        transition: "all 0.2s ease"
    },

    previewContainer: {
        marginTop: "16px",
        border: "1px solid #e2e8f0",
        borderRadius: "12px",
        padding: "12px",
        background: "#f8fafc"
    },

    previewFrame: {
        width: "100%",
        height: "280px",
        border: "none"
    },

    previewImage: {
        width: "100%",
        maxHeight: "280px",
        objectFit: "contain"
    },

    noPreview: {
        height: "120px",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        color: "#64748b",
        background: "#ffffff",
        border: "1px dashed #cbd5e1",
        borderRadius: "10px",
        fontSize: "14px"
    },

    documentActionPanel: {
        marginTop: "14px",
        paddingTop: "12px",
        borderTop: "1px solid #e2e8f0"
    },

    actionText: {
        margin: 0,
        fontSize: "13px",
        color: "#64748b",
        lineHeight: "1.5"
    },

    detailActions: {
        display: "flex",
        justifyContent: "flex-end",
        gap: "12px",
        marginTop: "12px"
    },

    rejectButton: {
        padding: "10px 22px",
        borderRadius: "8px",
        border: "none",
        background: "#dc2626",
        color: "#ffffff",
        cursor: "pointer",
        fontWeight: "700",
        fontSize: "14px",
        boxShadow: "0 2px 5px rgba(220,38,38,0.25)"
    },

    verifyContainer: {
        display: "flex",
        flexDirection: "column",
        alignItems: "flex-end"
    },

    verifyButton: {
        padding: "10px 22px",
        borderRadius: "8px",
        border: "none",
        background: "#16a34a",
        color: "#ffffff",
        cursor: "pointer",
        fontWeight: "700",
        fontSize: "14px",
        boxShadow: "0 2px 5px rgba(22,163,74,0.25)"
    },

    verifyHint: {
        marginTop: "8px",
        fontSize: "12px",
        color: "#92400e",
        background: "#fef3c7",
        border: "1px solid #fde68a",
        padding: "6px 10px",
        borderRadius: "6px",
        fontWeight: "600",
        maxWidth: "250px",
        textAlign: "right"
    }
};

export default styles;