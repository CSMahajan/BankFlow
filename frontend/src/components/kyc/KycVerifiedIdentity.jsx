import React from "react";

const KycVerifiedIdentity = ({
    kycDetails,
    showPan,
    showAadhaar,
    onViewPan,
    onViewAadhaar,
    maskPan,
    maskAadhaar
}) => {

    return (
        <div style={styles.kycDataCard}>

            <h3 style={styles.sectionTitle}>
                🔐 Verified Identity Details
            </h3>

            <div style={styles.kycDataRow}>

                <span>
                    PAN Number
                </span>

                <div style={styles.valueWithButton}>

                    <strong>
                        {
                            showPan
                                ? kycDetails.pan
                                : maskPan(kycDetails.pan)
                        }
                    </strong>

                    <button
                        style={styles.smallButton}
                        onClick={() => {

                            if (showPan) {
                                onViewPan(false);
                            }
                            else {
                                onViewPan(true);
                            }

                        }}
                    >
                        {
                            showPan
                                ? "Hide"
                                : "View"
                        }
                    </button>

                </div>

            </div>

            <div style={styles.kycDataRow}>

                <span>
                    Aadhaar Number
                </span>

                <div style={styles.valueWithButton}>

                    <strong>
                        {
                            showAadhaar
                                ? kycDetails.aadhaar
                                : maskAadhaar(kycDetails.aadhaar)
                        }
                    </strong>

                    <button
                        style={styles.smallButton}
                        onClick={() => {

                            if (showAadhaar) {
                                onViewAadhaar(false);
                            }
                            else {
                                onViewAadhaar(true);
                            }

                        }}
                    >
                        {
                            showAadhaar
                                ? "Hide"
                                : "View"
                        }
                    </button>

                </div>

            </div>

        </div>
    );
};

const styles = {

    sectionTitle: {
        margin: 0,
        fontSize: "18px",
        color: "#111827"
    },

    kycDataCard: {
        background: "#ffffff",
        padding: "20px",
        borderRadius: "14px",
        border: "1px solid #eef0ec",
        boxShadow: "0 4px 12px rgba(0,0,0,0.04)"
    },

    kycDataRow: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        padding: "12px 0",
        borderBottom: "1px solid #f1f5f9",
        fontSize: "14px",
    },

    valueWithButton: {
        display: "flex",
        alignItems: "center",
        gap: "12px",
    },

    smallButton: {
        background: "#0d6360",
        color: "#fff",
        border: "none",
        padding: "6px 12px",
        borderRadius: "6px",
        cursor: "pointer",
        fontSize: "12px"
    }

};

export default KycVerifiedIdentity;