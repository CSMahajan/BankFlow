import { useState } from "react";
import toast from "react-hot-toast";

const UpdateCardLimitModal = ({
    isOpen,
    onClose,
    currentLimit,
    cardId,
    onSave,
}) => {

    const [newLimit, setNewLimit] = useState(currentLimit);

    if (!isOpen) return null;

    return (

        <div style={styles.overlay}>

            <div style={styles.modal}>

                <h3 style={styles.title}>
                    Update Daily Limit
                </h3>

                <div style={styles.field}>

                    <label style={styles.label}>
                        Current Limit
                    </label>

                    <input
                        disabled
                        value={`₹${Number(currentLimit).toLocaleString("en-IN")}`}
                        style={styles.input}
                    />

                </div>

                <div style={styles.field}>

                    <label style={styles.label}>
                        New Daily Limit
                    </label>

                    <input
                        type="number"
                        value={newLimit}
                        onChange={(e) => setNewLimit(e.target.value)}
                        style={styles.input}
                    />

                </div>

                <div style={styles.actions}>

                    <button
                        style={styles.cancel}
                        onClick={onClose}
                    >
                        Cancel
                    </button>

                    <button
                        style={styles.save}
                        onClick={async () => {

                            if (Number(newLimit) <= 0) {
                                toast.error("Daily limit must be greater than ₹0");
                                return;
                            }

                            await onSave(cardId, Number(newLimit));

                            onClose();

                        }}
                    >
                        Save Changes
                    </button>

                </div>

            </div>

        </div>

    );

};

const styles = {

    overlay: {
        position: "fixed",
        inset: 0,
        background: "rgba(0,0,0,.45)",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        zIndex: 999,
    },

    modal: {
        background: "#fff",
        borderRadius: "16px",
        width: "420px",
        padding: "28px",
        boxShadow: "0 20px 60px rgba(0,0,0,.18)",
    },

    title: {
        marginTop: 0,
        marginBottom: "24px",
    },

    field: {
        marginBottom: "18px",
    },

    label: {
        display: "block",
        marginBottom: "8px",
        fontWeight: "600",
        color: "#475569",
    },

    input: {
        width: "100%",
        padding: "12px",
        borderRadius: "10px",
        border: "1px solid #d1d5db",
        fontSize: "15px",
        boxSizing: "border-box",
    },

    actions: {
        display: "flex",
        justifyContent: "flex-end",
        gap: "12px",
        marginTop: "26px",
    },

    cancel: {
        padding: "10px 18px",
        borderRadius: "10px",
        border: "1px solid #d1d5db",
        background: "#fff",
        cursor: "pointer",
    },

    save: {
        padding: "10px 20px",
        borderRadius: "10px",
        border: "none",
        background: "#0d6360",
        color: "#fff",
        fontWeight: "700",
        cursor: "pointer",
    }

};

export default UpdateCardLimitModal;