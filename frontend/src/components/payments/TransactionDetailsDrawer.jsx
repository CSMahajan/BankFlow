import React from "react";
import { formatDate, formatCurrency } from "../../utils/formatUtils";
import styles from "./transactionStyles";

const TransactionDetailsDrawer = ({
    drawerOpen,
    transactionDetails,
    detailsLoading,
    onClose,
}) => {

    if (!drawerOpen) {
        return null;
    }

    return (
        <div
            style={styles.drawerOverlay}
            onClick={onClose}
        >
            <div
                style={styles.drawer}
                onClick={(e) => e.stopPropagation()}
            >
                <div style={styles.drawerHeader}>
                    <div>
                        <h3 style={styles.drawerTitle}>
                            Transaction Details
                        </h3>

                        <p style={styles.drawerSubtitle}>
                            View complete transaction information
                        </p>
                    </div>

                    <button
                        style={styles.closeButton}
                        onClick={onClose}
                    >
                        ✕
                    </button>
                </div>

                {detailsLoading ? (

                    <div style={styles.drawerLoading}>
                        Loading transaction...
                    </div>

                ) : transactionDetails ? (

                    <div style={styles.detailsGrid}>

                        <div style={styles.detailItem}>
                            <span style={styles.detailLabel}>Reference</span>
                            <span
                                style={{
                                    fontFamily: "monospace",
                                    fontSize: "13px",
                                    color: "#374151",
                                    wordBreak: "break-all",
                                }}
                            >
                                {transactionDetails.transactionId}
                            </span>
                        </div>

                        <div style={styles.detailItem}>
                            <span style={styles.detailLabel}>Date</span>
                            <span>
                                {formatDate(transactionDetails.transactionDate)}
                            </span>
                        </div>

                        <div style={styles.detailItem}>
                            <span style={styles.detailLabel}>Account</span>
                            <span>{transactionDetails.accountNumber}</span>
                        </div>

                        <div style={styles.detailItem}>
                            <span style={styles.detailLabel}>Type</span>

                            <span
                                style={
                                    transactionDetails.transactionType === "CREDIT"
                                        ? styles.creditBadge
                                        : styles.debitBadge
                                }
                            >
                                {transactionDetails.transactionType}
                            </span>
                        </div>

                        <div style={styles.detailItem}>
                            <span style={styles.detailLabel}>Amount</span>

                            <span
                                style={{
                                    fontSize: "22px",
                                    fontWeight: 700,
                                    color:
                                        transactionDetails.transactionType === "CREDIT"
                                            ? "#15803d"
                                            : "#dc2626",
                                }}
                            >
                                {transactionDetails.transactionType === "CREDIT"
                                    ? "+"
                                    : "-"}
                                {formatCurrency(transactionDetails.amount)}
                            </span>
                        </div>

                        <div style={styles.detailItem}>
                            <span style={styles.detailLabel}>
                                Available Balance
                            </span>

                            <span>
                                {formatCurrency(transactionDetails.availableBalance)}
                            </span>
                        </div>

                        <div style={styles.detailItem}>
                            <span style={styles.detailLabel}>Description</span>
                            <span>
                                {transactionDetails.description || "-"}
                            </span>
                        </div>

                    </div>

                ) : (

                    <p>No details found.</p>

                )}

            </div>
        </div>
    );
};

export default TransactionDetailsDrawer;