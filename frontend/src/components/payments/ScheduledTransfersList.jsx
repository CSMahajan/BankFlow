import React, { useEffect, useState } from "react";
import { fetchMyScheduledTransfers, cancelScheduledTransfer } from "../../api/bankService";
import { formatCurrency, formatDate } from "../../utils/formatUtils";
import ConfirmModal from "../ConfirmModal";
import toast from "react-hot-toast";

const ScheduledTransfersList = ({ refreshTrigger }) => {

    const [transfers, setTransfers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [confirmTransfer, setConfirmTransfer] = useState(null);

    const loadTransfers = async () => {
        try {
            setLoading(true);

            const response = await fetchMyScheduledTransfers();
            setTransfers(response);

        } catch (err) {
            console.error(err);
            setError("Unable to load scheduled transfers.");
        } finally {
            setLoading(false);
        }
    };

    const activeCount =
        transfers.filter(t => t.status === "ACTIVE").length;

    const cancelledCount =
        transfers.filter(t => t.status === "CANCELLED").length;

    useEffect(() => {
        loadTransfers();
    }, [refreshTrigger]);

    const handleCancel = (transfer) => {
        setConfirmTransfer(transfer);
    };

    const confirmCancelTransfer = async () => {
        const transfer = confirmTransfer;
        setConfirmTransfer(null);
        try {
            await cancelScheduledTransfer(transfer.id);
            await loadTransfers();
        } catch (err) {
            console.error(err);
            toast.error("Unable to cancel scheduled transfer.");
        }
    };

    if (loading) {
        return <p>Loading scheduled transfers...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    return (
        <div style={styles.container}>

            <div style={styles.listHeader}>
                <div>
                    <h3>Your Scheduled Transfers</h3>
                    <p>
                        Manage recurring payments and upcoming transfers
                    </p>
                </div>

                <div style={styles.countContainer}>
                    <span style={styles.activeCount}>
                        Active: {activeCount}
                    </span>

                    <span style={styles.cancelledCount}>
                        Cancelled: {cancelledCount}
                    </span>
                </div>
            </div>

            {transfers.length === 0 ? (
                <div style={styles.empty}>
                    No scheduled transfers found.
                </div>
            ) : (
                transfers.map((transfer) => (
                    <div key={transfer.id}
                        style={styles.transferCard}>

                        <div style={styles.cardTop}>

                            <span style={styles.frequencyBadge}>
                                {transfer.frequency}
                            </span>

                            <div style={styles.headerActions}>

                                <span
                                    style={
                                        transfer.status === "ACTIVE"
                                            ? styles.activeBadge
                                            : styles.cancelledBadge
                                    }
                                >
                                    {transfer.status}
                                </span>

                                {transfer.status === "ACTIVE" && (
                                    <button
                                        onClick={() => handleCancel(transfer)}
                                        style={styles.cancelButton}
                                        onMouseEnter={(e) =>
                                            e.currentTarget.style.background = "#fee2e2"
                                        }
                                        onMouseLeave={(e) =>
                                            e.currentTarget.style.background = "#ffffff"
                                        }
                                    >
                                        Cancel
                                    </button>
                                )}
                            </div>
                        </div>

                        <div style={styles.amount}>
                            {formatCurrency(transfer.amount)}
                        </div>

                        <div style={styles.accounts}>
                            <span>{transfer.sourceAccountNumber}</span>

                            <span>→</span>

                            <span>{transfer.recipientAccountNumber}</span>
                        </div>

                        <div style={styles.nextDate}>
                            Next execution
                            <br />
                            <strong>
                                {formatDate(transfer.nextExecutionDate)}
                            </strong>
                        </div>

                        <div style={styles.description}>
                            {transfer.description || "No description"}
                        </div>
                    </div>
                ))
            )}
            <ConfirmModal
                open={!!confirmTransfer}
                title="Cancel Scheduled Transfer?"
                message={
                    confirmTransfer
                        ?
                        (
                            <div>

                                <p style={{ marginBottom: "20px" }}>
                                    You are about to cancel this scheduled transfer.
                                </p>


                                <div style={styles.confirmDetails}>

                                    <div style={styles.confirmRow}>
                                        <span>Amount</span>
                                        <strong style={styles.amountHighlight}>
                                            {formatCurrency(confirmTransfer.amount)}
                                        </strong>
                                    </div>


                                    <div style={styles.confirmRow}>
                                        <span>From Account</span>
                                        <strong>
                                            {confirmTransfer.sourceAccountNumber}
                                        </strong>
                                    </div>


                                    <div style={styles.confirmRow}>
                                        <span>To Account</span>
                                        <strong>
                                            {confirmTransfer.recipientAccountNumber}
                                        </strong>
                                    </div>


                                    <div style={styles.confirmRow}>
                                        <span>Frequency</span>
                                        <strong>
                                            {confirmTransfer.frequency}
                                        </strong>
                                    </div>

                                </div>


                                <p style={{
                                    marginTop: "18px",
                                    fontWeight: "600",
                                    color: "#dc2626"
                                }}>
                                    This action cannot be undone.
                                </p>

                            </div>
                        )
                        :
                        ""
                }
                confirmText="Cancel Transfer"
                danger={true}
                onCancel={() => setConfirmTransfer(null)}
                onConfirm={confirmCancelTransfer}
            />
        </div>
    );
};

const styles = {
    container: {
        marginTop: "36px",
    },

    heading: {
        marginBottom: "20px",
    },

    empty: {
        color: "#6b7280",
        padding: "24px",
        textAlign: "center",
    },

    card: {
        border: "1px solid #e5e7eb",
        borderRadius: "12px",
        padding: "18px",
        marginBottom: "16px",
        background: "#fff",
    },

    topRow: {
        display: "flex",
        justifyContent: "space-between",
        marginBottom: "10px",
    },

    frequency: {
        fontWeight: "700",
        color: "#0d6360",
    },

    active: {
        color: "#15803d",
        fontWeight: "700",
    },

    cancelled: {
        color: "#dc2626",
        fontWeight: "700",
    },

    text: {
        marginBottom: "6px",
        color: "#374151",
    },

    cancelButton: {
        padding: "8px 14px",
        border: "1px solid #dc2626",
        borderRadius: "8px",
        background: "#ffffff",
        color: "#dc2626",
        fontWeight: 600,
        cursor: "pointer",
        transition: "all .2s ease",
    },

    countBadge: {
        background: "#eef6ff",
        color: "#2563eb",
        padding: "6px 14px",
        borderRadius: "999px",
        fontWeight: 700,
    },

    transferCard: {
        background: "#fff",
        border: "1px solid #e5e7eb",
        borderRadius: "14px",
        padding: "20px",
        display: "flex",
        flexDirection: "column",
        gap: "16px",
        transition: "all .2s",
    },

    cardTop: {
        display: "flex",
        justifyContent: "space-between",
    },

    frequencyBadge: {
        background: "#ecfeff",
        color: "#0f766e",
        padding: "5px 10px",
        borderRadius: "999px",
        fontWeight: 700,
        fontSize: "12px",
    },

    activeBadge: {
        background: "#dcfce7",
        color: "#15803d",
        padding: "5px 10px",
        borderRadius: "999px",
        fontWeight: 700,
        fontSize: "12px",
    },

    amount: {
        fontSize: "34px",
        fontWeight: 700,
        color: "#111827",
        letterSpacing: "-0.5px",
    },

    accounts: {
        display: "flex",
        justifyContent: "space-between",
        fontFamily: "monospace",
    },

    nextDate: {
        color: "#6b7280",
        fontSize: "14px",
    },

    description: {
        color: "#374151",
        lineHeight: "20px",
    },

    headerActions: {
        display: "flex",
        alignItems: "center",
        gap: "10px",
    },

    cancelledBadge: {
        background: "#f3f4f6",
        color: "#6b7280",
        padding: "5px 10px",
        borderRadius: "999px",
        fontWeight: 700,
        fontSize: "12px",
    },

    countContainer: {
        display: "flex",
        gap: "10px",
    },

    activeCount: {
        background: "#dcfce7",
        color: "#15803d",
        padding: "6px 12px",
        borderRadius: "999px",
        fontWeight: 700,
        fontSize: "12px",
    },

    cancelledCount: {
        background: "#f3f4f6",
        color: "#6b7280",
        padding: "6px 12px",
        borderRadius: "999px",
        fontWeight: 700,
        fontSize: "12px",
    },

    confirmDetails: {
        display: "flex",
        flexDirection: "column",
        gap: "12px",
        backgroundColor: "#f9fafb",
        padding: "14px",
        borderRadius: "10px",
        border: "1px solid #e5e7eb",
    },

    confirmRow: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        fontSize: "14px",
        color: "#6b7280",
    },

    amountHighlight: {
        fontSize: "18px",
        color: "#0d6360",
        fontWeight: "800",
    },

};

export default ScheduledTransfersList;