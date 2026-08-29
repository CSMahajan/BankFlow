import React from "react";

const KycStatusCard = ({ status }) => {

    const statusColors = getStatusColor(status);

    return (
        <div style={styles.statusCard}>

            <div>
                <h3 style={styles.sectionTitle}>
                    🪪 Verification Status
                </h3>

                <p style={styles.statusDescription}>
                    Complete your KYC verification to access all banking services.
                </p>
            </div>

            <div
                style={{
                    ...styles.statusBadge,
                    backgroundColor: statusColors.background,
                    color: statusColors.color
                }}
            >
                <span>
                    ●
                </span>

                {status || "PENDING"}
            </div>

        </div>
    );
};

const getStatusColor = (status) => {

    switch (status) {

        case "VERIFIED":
            return {
                background: "#dcfce7",
                color: "#166534"
            };

        case "PENDING":
            return {
                background: "#fef3c7",
                color: "#92400e"
            };

        case "REJECTED":
            return {
                background: "#fee2e2",
                color: "#991b1b"
            };

        case "INCOMPLETE":
            return {
                background: "#fef3c7",
                color: "#92400e"
            };

        case "UNDER_REVIEW":
            return {
                background: "#dbeafe",
                color: "#1d4ed8"
            };

        default:
            return {
                background: "#e5e7eb",
                color: "#374151"
            };
    }
};

const styles = {

    statusCard: {
        background: "#ffffff",
        padding: "24px",
        borderRadius: "14px",
        border: "1px solid #eef0ec",
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        boxShadow: "0 4px 12px rgba(0,0,0,0.04)"
    },

    sectionTitle: {
        margin: 0,
        fontSize: "18px",
        color: "#111827"
    },

    statusDescription: {
        marginTop: "6px",
        fontSize: "13px",
        color: "#6b7280"
    },

    statusBadge: {
        display: "flex",
        alignItems: "center",
        gap: "8px",
        padding: "8px 16px",
        borderRadius: "20px",
        fontWeight: "700",
        fontSize: "13px"
    }

};

export default KycStatusCard;