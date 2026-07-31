import React, { useEffect, useState } from 'react';
import { fetchPendingLoans, approveLoan, rejectLoan } from '../api/bankService';
import { formatDate, formatCurrency } from '../utils/formatUtils';
import modalStyles from "../styles/modalStyles";

const LoanApprovalsView = () => {
    const [pendingLoans, setPendingLoans] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showRejectModal, setShowRejectModal] = useState(false);
    const [selectedLoanId, setSelectedLoanId] = useState(null);
    const [rejectionRemarks, setRejectionRemarks] = useState("");
    useEffect(() => {
        const loadPendingLoans = async () => {
            try {
                const data = await fetchPendingLoans();
                console.log(data);
                setPendingLoans(data);
            } catch (err) {
                console.error(err);
                setError('Unable to load pending loan applications.');
            } finally {
                setLoading(false);
            }
        };

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

            setPendingLoans((prevLoans) =>
                prevLoans.filter((loan) => loan.id !== loanId)
            );

            alert('Loan approved successfully.');
        } catch (err) {
            console.error(err);
            alert('Failed to approve loan.');
        }
    };

    const handleReject = (loanId) => {
        setSelectedLoanId(loanId);
        setRejectionRemarks("");
        setShowRejectModal(true);
    };

    const confirmReject = async () => {
        if (!rejectionRemarks.trim()) {
            alert("Rejection remarks are required.");
            return;
        }

        try {
            await rejectLoan(selectedLoanId, rejectionRemarks.trim());

            setPendingLoans((prevLoans) =>
                prevLoans.filter((loan) => loan.id !== selectedLoanId)
            );

            setShowRejectModal(false);
            setSelectedLoanId(null);
            setRejectionRemarks("");

            alert("Loan rejected successfully.");
        } catch (error) {
            console.error(error);
            console.error(error.response?.data);

            alert(
                error.response?.data?.message ??
                error.response?.data?.error ??
                JSON.stringify(error.response?.data) ??
                error.message
            );
        }
    };

    return (
        <div>
            <h2>Loan Approvals</h2>

            <p>Pending Loan Applications: {pendingLoans.length}</p>

            {pendingLoans.length === 0 ? (
                <p>No pending loan applications.</p>
            ) : (
                <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '20px' }}>
                    <thead>
                        <tr>
                            <th style={styles.header}>Loan #</th>
                            <th style={styles.header}>Customer</th>
                            <th style={styles.header}>Account</th>
                            <th style={styles.header}>Type</th>
                            <th style={styles.header}>Principal</th>
                            <th style={styles.header}>EMI</th>
                            <th style={styles.header}>Interest %</th>
                            <th style={styles.header}>Tenure</th>
                            <th style={styles.header}>Applied On</th>
                            <th style={styles.header}>Status</th>
                            <th style={styles.header}>Action</th>
                        </tr>
                    </thead>

                    <tbody>
                        {pendingLoans.map((loan) => (
                            <tr key={loan.id}>
                                <td style={styles.cell}>{loan.loanNumber}</td>
                                <td style={styles.cell}>{loan.customerName}</td>
                                <td style={styles.cell}>{loan.accountNumber}</td>
                                <td style={styles.cell}>{loan.loanType}</td>
                                <td style={styles.cell}>{formatCurrency(loan.principalAmount)}</td>
                                <td style={styles.cell}>{formatCurrency(loan.monthlyEmi)}</td>
                                <td style={styles.cell}>{loan.annualInterestRate}%</td>
                                <td style={styles.cell}>{loan.tenureMonths} months</td>
                                <td style={styles.cell}>{formatDate(loan.applicationDate)}</td>
                                <td style={styles.cell}>{loan.status}</td>
                                <td style={styles.cell}>
                                    <div style={{ display: "flex", gap: "8px" }}>
                                        <button onClick={() => handleApprove(loan.id)}>
                                            Approve
                                        </button>

                                        <button onClick={() => handleReject(loan.id)}>
                                            Reject
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}

            {showRejectModal && (
                <div style={modalStyles.overlay}>
                    <div style={modalStyles.modal}>
                        <h3>Reject Loan Application</h3>

                        <p>Please enter the reason for rejection.</p>

                        <textarea
                            value={rejectionRemarks}
                            onChange={(e) => setRejectionRemarks(e.target.value)}
                            rows={4}
                            placeholder="Enter rejection remarks"
                            style={{
                                width: "100%",
                                resize: "vertical",
                                marginTop: "10px",
                                marginBottom: "20px"
                            }}
                        />

                        <div
                            style={{
                                display: "flex",
                                justifyContent: "flex-end",
                                gap: "10px"
                            }}
                        >
                            <button
                                onClick={() => {
                                    setShowRejectModal(false);
                                    setSelectedLoanId(null);
                                    setRejectionRemarks("");
                                }}
                            >
                                Cancel
                            </button>

                            <button onClick={confirmReject}>
                                Reject
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

const styles = {
    header: {
        border: '1px solid #ddd',
        padding: '10px',
        backgroundColor: '#f5f5f5',
        textAlign: 'left'
    },
    cell: {
        border: '1px solid #ddd',
        padding: '10px'
    }
};

export default LoanApprovalsView;