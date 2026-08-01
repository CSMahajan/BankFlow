import React, { useEffect, useState } from "react";
import { fetchMyFixedDeposits, closeFixedDeposit } from "../api/bankService";
import { formatDate, formatCurrency } from "../utils/formatUtils";

const ViewFds = ({ onFdClosed }) => {
    const [fds, setFds] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const handleCloseFd = async (fd) => {

        const confirmed = window.confirm(
            new Date(fd.maturityDate) > new Date()
                ? "This Fixed Deposit has not matured yet.\n\nOnly the principal amount will be credited.\n\nDo you want to continue?"
                : "The maturity amount will be credited to your source account.\n\nDo you want to continue?"
        );

        if (!confirmed) return;

        try {
            await closeFixedDeposit(fd.fdNumber);

            await Promise.all([
                loadFds(),
                onFdClosed?.()
            ]);

            alert("✅ Fixed Deposit closed successfully.");
        } catch (err) {
            console.error(err);
            alert(
                err.response?.data?.message ??
                "Failed to close Fixed Deposit."
            );
        }
    };

    useEffect(() => {
        loadFds();
    }, []);

    const loadFds = async () => {

        try {
            setLoading(true);
            setError(null);
            const response = await fetchMyFixedDeposits();
            setFds(response);
        } catch (err) {
            console.error(err);
            setError("Unable to load Fixed Deposits.");
        } finally {
            setLoading(false);
        }
    };

    if (loading)
        return <div style={{ textAlign: "center", padding: "40px" }}>
            Loading Fixed Deposits...
        </div>

    if (error)
        return <div>{error}</div>;

    if (fds.length === 0)
        return <div style={{
            textAlign: "center",
            padding: "50px",
            color: "#64748b"
        }}>
            <h3>No Fixed Deposits Yet</h3>
            <p>Open your first Fixed Deposit to start earning guaranteed returns.</p>
        </div>

    return (
        <div style={styles.container}>
            <div style={styles.sectionHeader}>
                <h2>💰 Your Fixed Deposits</h2>

                <button
                    style={styles.refreshBtn}
                    onClick={loadFds}
                >
                    Refresh
                </button>
            </div>

            <div style={styles.grid}>
                {fds.map(fd => {
                    const isPremature =
                        new Date(fd.maturityDate) > new Date();

                    return (
                        <div key={fd.id} style={styles.card}>

                            <div style={styles.top}>
                                <span style={styles.fdNumber}>
                                    {fd.fdNumber}
                                </span>

                                <span
                                    style={{
                                        ...styles.status,
                                        backgroundColor:
                                            fd.status === "ACTIVE"
                                                ? "#dcfce7"
                                                : "#cbd5e1",
                                        color:
                                            fd.status === "ACTIVE"
                                                ? "#15803d"
                                                : "#334155",
                                    }}
                                >
                                    {fd.status === "ACTIVE"
                                        ? "🟢 ACTIVE"
                                        : "✓ CLOSED"}
                                </span>
                            </div>

                            <div style={styles.row}>
                                <span>Source Account</span>
                                <strong>{fd.sourceAccountNumber}</strong>
                            </div>

                            <div style={styles.row}>
                                <span>Deposit Amount</span>
                                <strong>
                                    {formatCurrency(fd.depositAmount)}
                                </strong>
                            </div>

                            <div style={styles.row}>
                                <span>Interest Rate</span>
                                <strong>{fd.interestRate}%</strong>
                            </div>

                            <div style={styles.row}>
                                <span>Tenure</span>
                                <strong>{fd.tenureYears} Years</strong>
                            </div>

                            <div style={styles.row}>
                                <span>Deposit Date</span>
                                <strong>{formatDate(fd.depositDate)}</strong>
                            </div>

                            <div style={styles.row}>
                                <span>Maturity Date</span>
                                <strong>{formatDate(fd.maturityDate)}</strong>
                            </div>

                            {fd.status === "CLOSED" && (
                                <div style={styles.row}>
                                    <span>Closed On</span>
                                    <strong>{formatDate(fd.closedDate)}</strong>
                                </div>
                            )}

                            <div style={styles.row}>
                                <span>Maturity Amount</span>
                                <strong style={{ color: "#0d6360" }}>
                                    {formatCurrency(fd.maturityAmount)}
                                </strong>
                            </div>
                            {fd.status === "ACTIVE" && (
                                <button
                                    style={{
                                        ...styles.closeButton,
                                        backgroundColor: isPremature
                                            ? "#ea580c"
                                            : "#b91c1c",
                                    }}
                                    onClick={() => handleCloseFd(fd)}
                                >
                                    {isPremature
                                        ? "Prematurely Close FD"
                                        : "Close FD"}
                                </button>
                            )}

                        </div>
                    );
                })}

            </div>
        </div>
    );
};

const styles = {
    container: {
        display: "flex",
        flexDirection: "column",
        gap: 20,
    },

    sectionHeader: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
    },

    grid: {
        display: "grid",
        gridTemplateColumns: "repeat(auto-fill,minmax(340px,1fr))",
        gap: 20,
    },

    card: {
        background: "#fff",
        border: "1px solid #eef0ec",
        borderRadius: 16,
        padding: 20,
    },

    top: {
        display: "flex",
        justifyContent: "space-between",
        marginBottom: 16,
    },

    fdNumber: {
        fontWeight: 700,
        fontSize: 17,
        color: "#0f172a",
    },

    status: {
        background: "#e2ece9",
        color: "#0d6360",
        padding: "4px 10px",
        borderRadius: 12,
        fontSize: 12,
        fontWeight: 700,
    },

    row: {
        display: "flex",
        justifyContent: "space-between",
        marginBottom: 10,
        fontSize: 14,
    },

    refreshBtn: {
        padding: "8px 14px",
        background: "#0d6360",
        color: "#fff",
        border: "none",
        borderRadius: 8,
        cursor: "pointer",
    },

    closeButton: {
        width: "100%",
        marginTop: 16,
        padding: "10px",
        border: "none",
        borderRadius: 10,
        backgroundColor: "#b91c1c",
        color: "#fff",
        fontWeight: 700,
        cursor: "pointer",
    },
};

export default ViewFds;