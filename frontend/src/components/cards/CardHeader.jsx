import React from "react";

const CardHeader = ({
    totalCards,
}) => {

    return (
        <div style={styles.pageHeader}>

            <div>
                <p style={styles.subtitle}>
                    View debit and credit cards and block or unblock them when required.
                </p>
            </div>

            <div style={styles.pendingBadge}>
                {totalCards} Cards
            </div>

        </div>
    );
};


const styles = {

    pageHeader: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "flex-start",
        marginBottom: "28px",
    },

    subtitle: {
        marginTop: "6px",
        color: "#64748b",
        fontSize: "15px",
    },

    pendingBadge: {
        background: "#FEF3C7",
        color: "#92400E",
        padding: "8px 14px",
        borderRadius: "999px",
        fontWeight: 700,
        fontSize: "14px",
        alignSelf: "flex-start",
    },

};


export default CardHeader;