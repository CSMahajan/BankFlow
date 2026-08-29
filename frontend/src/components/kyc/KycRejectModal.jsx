import React from "react";

const KycRejectModal = ({
    rejectionReason,
    setRejectionReason,
    onCancel,
    onReject
}) => {

    return (
        <div style={styles.modalOverlay}>

            <div style={styles.modal}>

                <h3>
                    Reject Document
                </h3>

                <textarea
                    style={styles.textarea}
                    placeholder="Enter rejection reason"
                    value={rejectionReason}
                    onChange={(e) =>
                        setRejectionReason(e.target.value)
                    }
                />

                <div style={styles.modalActions}>

                    <button
                        style={styles.cancelButton}
                        onClick={onCancel}
                    >
                        Cancel
                    </button>

                    <button
                        style={styles.rejectButton}
                        onClick={onReject}
                    >
                        Reject
                    </button>

                </div>

            </div>

        </div>
    );
};

const styles = {

    modalOverlay: {
        position: "fixed",
        inset: 0,
        background: "rgba(0,0,0,0.4)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        zIndex: 1000
    },

    modal: {
        background: "#ffffff",
        padding: "24px",
        borderRadius: "12px",
        width: "400px"
    },

    textarea: {
        width: "100%",
        height: "120px",
        marginTop: "15px",
        padding: "10px",
        borderRadius: "8px",
        border: "1px solid #cbd5e1"
    },

    modalActions: {
        display: "flex",
        justifyContent: "flex-end",
        gap: "10px",
        marginTop: "15px"
    },

    cancelButton: {
        padding: "8px 14px",
        borderRadius: "7px",
        border: "1px solid #cbd5e1",
        background: "#ffffff",
        cursor: "pointer"
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
    }

};

export default KycRejectModal;