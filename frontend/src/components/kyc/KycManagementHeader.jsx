import React from "react";

const KycManagementHeader = ({ onRefresh, loading }) => {
    return (
        <div style={styles.headerRow}>
            <h2 style={styles.title}>
                KYC Verification Management
            </h2>

            <button
                style={styles.refreshButton}
                onClick={onRefresh}
            >
                🔄 Refresh
            </button>
        </div>
    );
};

const styles = {
    headerRow: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center"
    },

    title: {
        fontSize: "24px",
        fontFamily: "Georgia, serif"
    },

    refreshButton: {
        background: "#0d6360",
        color: "#ffffff",
        border: "none",
        padding: "10px 18px",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: "700"
    }
};

export default KycManagementHeader;