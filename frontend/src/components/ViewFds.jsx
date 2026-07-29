import React, { useEffect, useState } from "react";
import API from "../api/axios";

const ViewFds = () => {
    const [fds, setFds] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        loadFds();
    }, []);

    const loadFds = async () => {
        try {
            const response = await API.get("/fd/my-fds");
            setFds(response.data || []);
        } catch (err) {
            setError("Unable to load Fixed Deposits.");
        } finally {
            setLoading(false);
        }
    };

    const formatCurrency = (amount) =>
        new Intl.NumberFormat("en-IN", {
            style: "currency",
            currency: "INR",
            maximumFractionDigits: 2,
        }).format(amount || 0);

    if (loading)
        return <div>Loading Fixed Deposits...</div>;

    if (error)
        return <div>{error}</div>;

    if (fds.length === 0)
        return <div>No Fixed Deposits found.</div>;

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
                {fds.map(fd => (
                    <div key={fd.id} style={styles.card}>

                        <div style={styles.top}>
                            <span style={styles.fdNumber}>
                                {fd.fdNumber}
                            </span>

                            <span style={styles.status}>
                                {fd.status}
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
                            <strong>{fd.depositDate}</strong>
                        </div>

                        <div style={styles.row}>
                            <span>Maturity Date</span>
                            <strong>{fd.maturityDate}</strong>
                        </div>

                        <div style={styles.row}>
                            <span>Maturity Amount</span>
                            <strong style={{ color: "#0d6360" }}>
                                {formatCurrency(fd.maturityAmount)}
                            </strong>
                        </div>

                    </div>
                ))}
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
        fontSize: 16,
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
};

export default ViewFds;