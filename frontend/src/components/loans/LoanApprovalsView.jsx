import React, { useEffect, useState } from 'react';
import { fetchPendingLoans, approveLoan, rejectLoan } from '../../api/bankService';
import { formatDate, formatCurrency } from '../../utils/formatUtils';
import modalStyles from "../../styles/modalStyles";
import toast from "react-hot-toast";
import PageCard from '../PageCard';
import { getLoanTypeStyle, getLoanTypeIcon } from '../../utils/loanTypeUtils';

const LoanApprovalsView = ({
    refreshDashboard,
}) => {
    const [pendingLoans, setPendingLoans] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showRejectModal, setShowRejectModal] = useState(false);
    const [selectedLoanId, setSelectedLoanId] = useState(null);
    const [selectedLoan, setSelectedLoan] = useState(null);
    const [rejectionRemarks, setRejectionRemarks] = useState("");
    const [search, setSearch] = useState("");
    const [loanTypeFilter, setLoanTypeFilter] = useState("ALL");
    const [expandedLoanId, setExpandedLoanId] = useState(null);

    const loadPendingLoans = async () => {
        try {
            setLoading(true);
            setError("");

            const loans = await fetchPendingLoans();
            setPendingLoans(loans);
        } catch (err) {
            console.error(err);
            setError("Unable to load pending loan applications.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadPendingLoans();
    }, []);

    if (loading) {
        return <p>Loading pending loan applications...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    const handleApprove = async (loanId) => {
        try {
            await approveLoan(loanId);

            setPendingLoans(prev =>
                prev.filter(loan => loan.id !== loanId)
            );

            await refreshDashboard?.();

            toast.success("Loan approved successfully.");
        } catch (err) {
            console.error(err);
            toast.error('Failed to approve loan.');
        }
    };

    const handleReject = (loan) => {
        setSelectedLoanId(loan.id);
        setSelectedLoan(loan);
        setRejectionRemarks("");
        setShowRejectModal(true);
    };

    const confirmReject = async () => {
        if (!rejectionRemarks.trim()) {
            toast.error("Rejection remarks are required.");
            return;
        }

        try {
            await rejectLoan(selectedLoanId, rejectionRemarks.trim());

            setPendingLoans(prev =>
                prev.filter(loan => loan.id !== selectedLoanId)
            );

            await refreshDashboard?.();

            setShowRejectModal(false);
            setSelectedLoanId(null);
            setSelectedLoan(null);
            setRejectionRemarks("");

            toast.success("Loan rejected successfully.");
        } catch (error) {
            console.error(error);
            console.error(error.response?.data);

            toast.error(
                error.response?.data?.message ??
                error.response?.data?.error ??
                JSON.stringify(error.response?.data) ??
                error.message
            );
        }
    };

    const personalCount = pendingLoans.filter(
        loan => loan.loanType === "PERSONAL"
    ).length;

    const homeCount = pendingLoans.filter(
        loan => loan.loanType === "HOME"
    ).length;

    const vehicleCount = pendingLoans.filter(
        loan => loan.loanType === "VEHICLE"
    ).length;

    const filteredLoans = pendingLoans.filter((loan) => {

        const matchesSearch =
            loan.loanNumber.toLowerCase().includes(search.toLowerCase()) ||
            loan.customerName.toLowerCase().includes(search.toLowerCase()) ||
            loan.accountNumber.toLowerCase().includes(search.toLowerCase());

        const matchesType =
            loanTypeFilter === "ALL" ||
            loan.loanType === loanTypeFilter;

        return matchesSearch && matchesType;

    });



    const formatLoanType = (loanType) => {
        switch (loanType) {
            case "HOME":
                return "Home";
            case "PERSONAL":
                return "Personal";
            case "VEHICLE":
                return "Vehicle";
            default:
                return loanType;
        }
    };

    const loanTypeCards = [
        {
            title: "Personal Loans",
            icon: "👤",
            count: personalCount,
            style: {
                background: "#FEFCE8",
                border: "1px solid #FDE68A",
            },
        },
        {
            title: "Home Loans",
            icon: "🏠",
            count: homeCount,
            style: {
                background: "#EFF6FF",
                border: "1px solid #BFDBFE",
            },
        },
        {
            title: "Vehicle Loans",
            icon: "🚗",
            count: vehicleCount,
            style: {
                background: "#ECFDF5",
                border: "1px solid #A7F3D0",
            },
        },
    ];

    return (
        <PageCard title="🏦 Loan Approvals">

            <div style={styles.pageHeader}>

                <div>
                    <p style={styles.subtitle}>
                        Review and approve customer loan applications.
                    </p>
                </div>

                <div style={styles.pendingBadge}>
                    {pendingLoans.length} Pending
                </div>

            </div>

            <div style={styles.summaryGrid}>
                {loanTypeCards.map((card) => (
                    <div
                        key={card.title}
                        style={{
                            ...styles.summaryCard,
                            ...card.style,
                        }}
                    >
                        <div style={styles.summaryTitle}>
                            <span>{card.icon}</span>
                            <span>{card.title}</span>
                        </div>
                        <div style={styles.summaryValue}>
                            {card.count}
                        </div>
                        <div style={styles.summarySubtext}>
                            Pending Applications
                        </div>
                    </div>
                ))}
            </div>

            {pendingLoans.length === 0 ? (
                <p>No pending loan applications.</p>
            ) : (
                <>
                    <div style={styles.toolbar}>

                        <input
                            placeholder="Search loan, customer or account..."
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                            style={styles.searchInput}
                        />

                        <select
                            value={loanTypeFilter}
                            onChange={(e) => setLoanTypeFilter(e.target.value)}
                            style={styles.filterSelect}
                        >
                            <option value="ALL">All Types</option>
                            <option value="PERSONAL">Personal</option>
                            <option value="HOME">Home</option>
                            <option value="VEHICLE">Vehicle</option>
                        </select>

                    </div>
                    <hr
                        style={{
                            border: "none",
                            borderTop: "1px solid #e5e7eb",
                            margin: "28px 0",
                        }}
                    />
                    <div
                        style={{
                            overflowX: "auto",
                            border: "1px solid #e5e7eb",
                            borderRadius: "12px",
                        }}
                    >
                        <table style={{
                            width: '100%', borderCollapse: 'collapse'
                        }}>
                            <thead>
                                < tr >
                                    <th style={styles.header}>Customer</th>
                                    <th style={styles.header}>Type</th>
                                    <th style={styles.header}>Principal Amount</th>
                                    <th style={styles.header}>Application Date</th>
                                    <th style={styles.header}>Status</th>
                                </tr>
                            </thead>

                            <tbody>
                                {filteredLoans.map((loan, index) => (
                                    <React.Fragment key={loan.id}>
                                        <tr
                                            key={loan.id}
                                            onClick={() =>
                                                setExpandedLoanId(
                                                    expandedLoanId === loan.id ? null : loan.id
                                                )
                                            }
                                            style={{
                                                cursor: "pointer",
                                                transition: ".15s",
                                                background:
                                                    expandedLoanId === loan.id
                                                        ? "#eff6ff"
                                                        : index % 2 === 0
                                                            ? "#ffffff"
                                                            : "#fafafa",

                                            }}
                                            onMouseEnter={(e) => {
                                                if (expandedLoanId !== loan.id) {
                                                    e.currentTarget.style.background = "#eff6ff";
                                                }
                                            }}
                                            onMouseLeave={(e) => {
                                                if (expandedLoanId !== loan.id) {
                                                    e.currentTarget.style.background =
                                                        index % 2 === 0 ? "#ffffff" : "#fafafa";
                                                }
                                            }}
                                        >
                                            <td style={styles.cell}>{loan.customerName}</td>
                                            <td style={styles.cell}><span
                                                style={{
                                                    padding: "4px 10px",
                                                    borderRadius: "999px",
                                                    fontWeight: 600,
                                                    fontSize: "12px",
                                                    ...getLoanTypeStyle(loan.loanType)
                                                }}
                                            >
                                                {loan.loanType}
                                            </span></td>
                                            <td style={styles.cell}>{formatCurrency(loan.principalAmount)}</td>
                                            <td style={styles.cell}>{formatDate(loan.applicationDate)}</td>
                                            <td style={styles.cell}>
                                                <span
                                                    style={{
                                                        padding: "5px 10px",
                                                        borderRadius: "999px",
                                                        background: "#fef3c7",
                                                        color: "#92400e",
                                                        fontWeight: 600,
                                                        fontSize: "12px",
                                                    }}
                                                >
                                                    {loan.status}
                                                </span>
                                            </td>
                                        </tr>
                                        {expandedLoanId === loan.id && (
                                            <tr>
                                                <td
                                                    colSpan={5}
                                                    style={{
                                                        padding: "18px",
                                                        background: "#fff",
                                                        borderBottom: "1px solid #e5e7eb",
                                                    }}
                                                >
                                                    <div style={styles.loanDetailsContainer}>
                                                        <div style={styles.detailsHeader}>
                                                            <div>
                                                                <h3 style={styles.customerName}>
                                                                    👤 {loan.customerName}
                                                                </h3>
                                                                <p style={styles.detailsSubtitle}>
                                                                    {getLoanTypeIcon(loan.loanType)}{" "}
                                                                    {formatLoanType(loan.loanType)} Loan
                                                                    {" • "}
                                                                    <span
                                                                        style={{
                                                                            fontFamily: "monospace",
                                                                            fontWeight: 600,
                                                                        }}
                                                                    >
                                                                        {loan.loanNumber}
                                                                    </span>
                                                                </p>
                                                            </div>
                                                        </div>
                                                        <div style={styles.loanDetailsGrid}>
                                                            <div
                                                                style={{
                                                                    ...styles.detailCard,
                                                                    background: "#eff6ff",
                                                                    border: "1px solid #bfdbfe",
                                                                }}
                                                            >
                                                                <div style={styles.detailLabel}>💰 Principal Amount</div>
                                                                <div
                                                                    style={{
                                                                        ...styles.detailValue,
                                                                        fontSize: "20px",
                                                                        fontWeight: 700,
                                                                        color: "#1d4ed8",
                                                                    }}
                                                                >
                                                                    {formatCurrency(loan.principalAmount)}
                                                                </div>
                                                            </div>
                                                            <div
                                                                style={{
                                                                    ...styles.detailCard,
                                                                    background: "#ecfdf5",
                                                                    border: "1px solid #a7f3d0",
                                                                }}
                                                            >
                                                                <div style={styles.detailLabel}>💵 Monthly EMI</div>
                                                                <div style={{
                                                                    ...styles.detailValue,
                                                                    fontSize: "18px",
                                                                    color: "#0f766e",
                                                                }}
                                                                >
                                                                    {formatCurrency(loan.monthlyEmi)}
                                                                </div>
                                                            </div>
                                                            <div style={styles.detailCard}>
                                                                <div style={styles.detailLabel}>🏦 Account Number</div>
                                                                <div style={{
                                                                    ...styles.detailValue,
                                                                    fontFamily: "monospace",
                                                                }}
                                                                >
                                                                    {loan.accountNumber}</div>
                                                            </div>
                                                            <div style={styles.detailCard}>
                                                                <div style={styles.detailLabel}>📈 Interest Rate</div>
                                                                <div style={styles.detailValue}>{loan.annualInterestRate}%</div>
                                                            </div>
                                                            <div style={styles.detailCard}>
                                                                <div style={styles.detailLabel}>📅 Tenure</div>
                                                                <div style={styles.detailValue}>
                                                                    {loan.tenureMonths} Months
                                                                </div>
                                                            </div>
                                                            <div style={styles.detailCard}>
                                                                <div style={styles.detailLabel}>📝 Applied On</div>
                                                                <div style={styles.detailValue}>
                                                                    {formatDate(loan.applicationDate)}
                                                                </div>
                                                            </div>
                                                        </div>
                                                        <hr
                                                            style={{
                                                                border: "none",
                                                                borderTop: "1px solid #e5e7eb",
                                                                margin: "28px 0",
                                                            }}
                                                        />
                                                        <div style={styles.actionSection}>
                                                            <div style={styles.actionInfo}>
                                                                <h4 style={styles.actionTitle}>
                                                                    Decision
                                                                </h4>
                                                                <p style={styles.actionSubtitle}>
                                                                    Approve or reject this loan application.
                                                                </p>
                                                            </div>
                                                        </div>
                                                        <div style={styles.detailsActions}>
                                                            <button
                                                                style={styles.approveButton}
                                                                onClick={() => handleApprove(loan.id)}
                                                            >
                                                                ✓ Approve Loan
                                                            </button>
                                                            <button
                                                                style={styles.rejectButton}
                                                                onClick={() => handleReject(loan)}
                                                            >
                                                                ✕ Reject Application
                                                            </button>
                                                        </div>
                                                    </div>
                                                </td>
                                            </tr>
                                        )}
                                    </React.Fragment>
                                ))}
                            </tbody>
                        </table>
                    </div >
                </>
            )}

            {
                showRejectModal && (
                    <div style={modalStyles.overlay}>
                        <div style={modalStyles.modal}>
                            <h3 style={styles.rejectModalTitle}>
                                ⚠️ Reject Loan Application
                            </h3>
                            <p style={styles.rejectModalSubtitle}>
                                This action cannot be undone.
                                <br />
                                Please provide a clear reason for rejecting this application.
                            </p>

                            {selectedLoan && (
                                <div style={styles.loanSummaryCard}>

                                    <div style={styles.loanSummaryName}>
                                        👤 {selectedLoan.customerName}
                                    </div>

                                    <div style={styles.loanSummaryMeta}>
                                        {getLoanTypeIcon(selectedLoan.loanType)}
                                        {" "}
                                        {formatLoanType(selectedLoan.loanType)} Loan
                                        {" • "}
                                        <span style={{ fontFamily: "monospace" }}>
                                            {selectedLoan.loanNumber}
                                        </span>
                                    </div>

                                    <div style={styles.loanSummaryAmount}>
                                        <div style={styles.loanSummaryAmountLabel}>
                                            Principal Amount
                                        </div>

                                        <div style={styles.loanSummaryAmountValue}>
                                            {formatCurrency(selectedLoan.principalAmount)}
                                        </div>
                                    </div>

                                </div>
                            )}

                            <div style={styles.rejectReasonSection}>

                                <label style={styles.rejectReasonLabel}>
                                    Reason for Rejection <span style={{ color: "#dc2626" }}>*</span>
                                </label>

                                <textarea
                                    value={rejectionRemarks}
                                    onChange={(e) => setRejectionRemarks(e.target.value)}
                                    rows={5}
                                    maxLength={500}
                                    placeholder="Example:
• Income documents could not be verified.
• Credit assessment did not meet approval criteria.
• Required documents were incomplete."
                                    style={styles.rejectTextarea}
                                />

                                <div
                                    style={{
                                        ...styles.characterCount,
                                        color:
                                            rejectionRemarks.length > 450
                                                ? "#dc2626"
                                                : "#64748b",
                                    }}
                                >
                                    {rejectionRemarks.length}/500 characters
                                </div>

                            </div>

                            <div
                                style={{
                                    display: "flex",
                                    justifyContent: "flex-end",
                                    gap: "10px"
                                }}
                            >
                                <button
                                    style={styles.cancelButton}
                                    onClick={() => {
                                        setShowRejectModal(false);
                                        setSelectedLoanId(null);
                                        setSelectedLoan(null);
                                        setRejectionRemarks("");
                                    }}
                                >
                                    Cancel
                                </button>

                                <button
                                    style={{
                                        ...styles.confirmRejectButton,
                                        opacity: rejectionRemarks.trim() ? 1 : 0.5,
                                        cursor: rejectionRemarks.trim() ? "pointer" : "not-allowed",
                                    }}
                                    disabled={!rejectionRemarks.trim()}
                                    onClick={confirmReject}
                                >
                                    ✕ Reject Loan
                                </button>
                            </div>
                        </div>
                    </div>
                )
            }
        </PageCard >
    );
};

const styles = {

    pageHeader: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "flex-start",
        marginBottom: "28px",
    },

    header: {
        background: "#f8fafc",
        color: "#334155",
        fontWeight: 700,
        fontSize: "14px",
        padding: "14px 16px",
        textAlign: "left",
        borderBottom: "1px solid #e5e7eb",
        whiteSpace: "nowrap",
    },

    title: {
        margin: 0,
        fontSize: "30px",
        fontWeight: 700,
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

    cell: {
        padding: "16px",
        borderBottom: "1px solid #f1f5f9",
        fontSize: "14px",
        color: "#334155",
    },

    summaryGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(3, 1fr)",
        gap: "18px",
        marginBottom: "26px",
    },

    summaryCard: {
        border: "1px solid #e5e7eb",
        borderRadius: "12px",
        padding: "18px",
        background: "#fff",
        display: "flex",
        flexDirection: "column",
        alignItems: "flex-start",
    },

    summaryValue: {
        fontSize: "30px",
        fontWeight: 700,
        color: "#0f172a",
    },

    summaryLabel: {
        marginTop: "8px",
        color: "#64748b",
        fontSize: "14px",
    },

    toolbar: {
        display: "flex",
        justifyContent: "space-between",
        gap: "16px",
        marginBottom: "24px",
    },

    searchInput: {
        flex: 1,
        padding: "12px 14px",
        borderRadius: "10px",
        border: "1px solid #d1d5db",
        fontSize: "14px",
    },

    filterSelect: {
        width: "180px",
        padding: "12px",
        borderRadius: "10px",
        border: "1px solid #d1d5db",
        fontSize: "14px",
    },

    loanDetailsContainer: {
        background: "#ffffff",
        border: "1px solid #e5e7eb",
        borderRadius: "14px",
        padding: "14px",
        boxShadow: "0 2px 8px rgba(15, 23, 42, 0.05)",
    },

    loanDetailsGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(2, 1fr)",
        gap: "20px 32px",
    },

    detailItem: {
        display: "flex",
        flexDirection: "column",
    },

    detailLabel: {
        fontSize: "13px",
        color: "#64748b",
        marginBottom: "6px",
    },

    detailValue: {
        fontWeight: 600,
        color: "#0f172a",
        fontSize: "15px",
    },

    detailsActions: {
        width: "100%",
        display: "flex",
        justifyContent: "flex-end",
        gap: "12px",
    },

    detailCard: {
        background: "#ffffff",
        border: "1px solid #e5e7eb",
        borderRadius: "10px",
        padding: "16px",
    },

    approveButton: {
        background: "#16a34a",
        color: "#fff",
        border: "none",
        padding: "12px 22px",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: 600,
        minWidth: "170px",
    },

    rejectButton: {
        background: "#fff",
        color: "#dc2626",
        border: "1px solid #dc2626",
        padding: "12px 22px",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: 600,
        minWidth: "170px",
    },

    detailsHeader: {
        marginBottom: "24px",
    },

    detailsTitle: {
        margin: 0,
        fontSize: "18px",
        fontWeight: 700,
        color: "#0f172a",
    },

    detailsSubtitle: {
        marginTop: "4px",
        fontSize: "14px",
        color: "#64748b",
    },

    summaryTitle: {
        display: "flex",
        alignItems: "center",
        gap: "8px",
        fontWeight: 600,
        color: "#334155",
        marginBottom: "18px",
    },

    summarySubtext: {
        marginTop: "6px",
        color: "#64748b",
        fontSize: "13px",
    },

    customerName: {
        margin: 0,
        fontSize: "22px",
        fontWeight: 700,
        color: "#0f172a",
    },

    actionSection: {
        display: "flex",
        flexDirection: "column",
        gap: "12px",
        marginTop: "20px",
    },

    actionInfo: {
        display: "flex",
        flexDirection: "column",
    },

    actionTitle: {
        margin: 0,
        fontSize: "17px",
        fontWeight: 700,
        color: "#0f172a",
    },

    actionSubtitle: {
        marginTop: "4px",
        fontSize: "14px",
        color: "#64748b",
    },

    rejectModalTitle: {
        margin: 0,
        fontSize: "22px",
        fontWeight: 700,
        color: "#991b1b",
    },

    rejectModalSubtitle: {
        marginTop: "10px",
        color: "#64748b",
        lineHeight: 1.5,
        marginBottom: "24px",
    },

    loanSummaryCard: {
        background: "#f8fafc",
        border: "1px solid #e2e8f0",
        borderRadius: "10px",
        padding: "16px",
        marginBottom: "20px",
    },

    loanSummaryName: {
        fontWeight: 700,
        fontSize: "17px",
        color: "#0f172a",
    },

    loanSummaryMeta: {
        marginTop: "6px",
        color: "#64748b",
        fontSize: "14px",
    },

    loanSummaryAmount: {
        marginTop: "16px",
        paddingTop: "16px",
        borderTop: "1px solid #e5e7eb",
    },

    loanSummaryAmountLabel: {
        fontSize: "13px",
        color: "#64748b",
    },

    loanSummaryAmountValue: {
        marginTop: "4px",
        fontSize: "24px",
        fontWeight: 700,
        color: "#1d4ed8",
    },

    rejectReasonSection: {
        marginTop: "24px",
    },

    rejectReasonLabel: {
        display: "block",
        marginBottom: "8px",
        fontWeight: 600,
        color: "#334155",
        fontSize: "14px",
    },

    rejectTextarea: {
        width: "100%",
        minHeight: "120px",
        resize: "vertical",
        padding: "12px",
        borderRadius: "10px",
        border: "1px solid #cbd5e1",
        fontSize: "14px",
        lineHeight: 1.5,
        outline: "none",
        boxSizing: "border-box",
    },

    characterCount: {
        marginTop: "8px",
        textAlign: "right",
        fontSize: "12px",
        color: "#64748b",
    },

    cancelButton: {
        background: "#ffffff",
        color: "#475569",
        border: "1px solid #cbd5e1",
        borderRadius: "8px",
        padding: "10px 18px",
        fontWeight: 600,
        cursor: "pointer",
    },

    confirmRejectButton: {
        background: "#dc2626",
        color: "#ffffff",
        border: "none",
        borderRadius: "8px",
        padding: "10px 18px",
        fontWeight: 600,
        cursor: "pointer",
    },
};

export default LoanApprovalsView;