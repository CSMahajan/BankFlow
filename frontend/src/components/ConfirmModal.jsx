import React from "react";

const ConfirmModal = ({
    open,
    title,
    message,
    confirmText = "Confirm",
    cancelText = "Cancel",
    onConfirm,
    onCancel,
    danger = false,
}) => {

    if (!open) {
        return null;
    }


    return (
        <div style={styles.overlay}>

            <div style={styles.modal}>

                <div style={styles.icon}>
                    {danger ? "⚠️" : "ℹ️"}
                </div>


                <h3 style={styles.title}>
                    {title}
                </h3>


                <p style={styles.message}>
                    {message}
                </p>


                <div style={styles.actions}>

                    <button
                        style={styles.cancelButton}
                        onClick={onCancel}
                    >
                        {cancelText}
                    </button>


                    <button
                        style={{
                            ...styles.confirmButton,
                            backgroundColor: danger
                                ? "#dc2626"
                                : "#15803d",
                        }}
                        onClick={onConfirm}
                    >
                        {confirmText}
                    </button>

                </div>

            </div>

        </div>
    );
};


const styles = {

    overlay: {
        position: "fixed",
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: "rgba(0,0,0,0.45)",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        zIndex: 9999,
    },


    modal: {
        width: "380px",
        backgroundColor: "#ffffff",
        borderRadius: "16px",
        padding: "28px",
        textAlign: "center",
        boxShadow: "0 20px 40px rgba(0,0,0,0.15)",
    },


    icon: {
        fontSize: "36px",
        marginBottom: "12px",
    },


    title: {
        margin: "0 0 12px",
        fontSize: "20px",
        fontWeight: "800",
        color: "#111827",
    },


    message: {
        whiteSpace: "pre-line",
        color: "#6b7280",
        fontSize: "14px",
        lineHeight: "1.6",
        marginBottom: "24px",
    },


    actions: {
        display: "flex",
        justifyContent: "center",
        gap: "12px",
    },


    cancelButton: {
        padding: "10px 20px",
        borderRadius: "8px",
        border: "1px solid #d1d5db",
        backgroundColor: "#ffffff",
        cursor: "pointer",
        fontWeight: "700",
    },


    confirmButton: {
        padding: "10px 20px",
        borderRadius: "8px",
        border: "none",
        color: "#ffffff",
        cursor: "pointer",
        fontWeight: "700",
    },

};


export default ConfirmModal;