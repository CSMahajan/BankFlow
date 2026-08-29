import React from "react";

const KycHeader = ({ styles }) => {
    return (
        <div style={styles.header}>
            <h2 style={styles.title}>
                KYC Verification
            </h2>

            <p style={styles.subtitle}>
                Verify your identity by uploading required documents.
                This helps us keep your account secure.
            </p>
        </div>
    );
};

export default KycHeader;