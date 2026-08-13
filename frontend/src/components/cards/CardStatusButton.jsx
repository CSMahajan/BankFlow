const CardStatusButton = ({
    cardStatus,
    onClick,
    loading = false,
}) => {

    const isBlocked = cardStatus === "BLOCKED";

    return (
        <button
            disabled={loading}
            onClick={onClick}
            style={
                isBlocked
                    ? styles.unblockButton
                    : styles.blockButton
            }
        >
            {
                loading
                    ? "Updating..."
                    : isBlocked
                        ? "Unblock Card"
                        : "Block Card"
            }
        </button>
    );
};


const styles = {

    blockButton: {
        background: "#dc2626",
        color: "#ffffff",
        border: "none",
        padding: "10px 22px",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: 700,
        fontSize: "14px",
        minWidth: "180px",
        boxShadow: "0 2px 5px rgba(220,38,38,0.25)",
    },

    unblockButton: {
        background: "#16a34a",
        color: "#ffffff",
        border: "none",
        padding: "10px 22px",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: 700,
        fontSize: "14px",
        minWidth: "180px",
        boxShadow: "0 2px 5px rgba(22,163,74,0.25)",
    }

};

export default CardStatusButton;