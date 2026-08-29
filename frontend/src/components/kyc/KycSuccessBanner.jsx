import React from "react";

const KycSuccessBanner = ({ styles }) => {
    return (
        <div style={styles.successBanner}>
            ✅ Your KYC verification is completed.
            You can now access all banking services.
        </div>
    );
};

export default KycSuccessBanner;