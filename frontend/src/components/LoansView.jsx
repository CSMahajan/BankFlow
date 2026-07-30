import React, { useEffect, useState } from 'react';
import ApplyLoanModal from './ApplyLoanModal';
import API from '../api/axios';

const LoansView = ({ accounts }) => {
    const [isApplyLoanModalOpen, setIsApplyLoanModalOpen] = useState(false);
    const [selectedLoanNumber, setSelectedLoanNumber] = useState(null);
    const [repayments, setRepayments] = useState([]);
    const [loans, setLoans] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const formatCurrency = (amount) =>
        new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR'
        }).format(amount);

    const formatDate = (date) => {
        if (!date) return '-';

        return new Date(date).toLocaleDateString('en-IN', {
            day: '2-digit',
            month: 'short',
            year: 'numeric'
        });
    };

    const getStatusStyle = (status) => {
        return {
            display: 'inline-block',
            padding: '4px 10px',
            borderRadius: '12px',
            fontSize: '12px',
            fontWeight: '600',
            backgroundColor:
                status === 'ACTIVE' ? '#dcfce7' : '#fef3c7',
            color:
                status === 'ACTIVE' ? '#166534' : '#92400e'
        };
    };

    const fetchLoans = async () => {
        try {
            const response = await API.get('/loans/my-loans');
            setLoans(response.data);
        } catch (err) {
            console.error(err);
            setError('Failed to load loans.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchLoans();
    }, []);

    const fetchRepayments = async (loanNumber) => {
        try {
            const response = await API.get(`/loans/${loanNumber}/repayments`);

            setSelectedLoanNumber(loanNumber);
            setRepayments(response.data);
        } catch (err) {
            console.error(err);
            alert('Failed to load repayment history.');
        }
    };

    if (loading) {
        return <p>Loading loans...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    return (
        <div>
            <h2>My Loans</h2>
            <div style={{ marginBottom: '20px' }}>
                <button
                    onClick={() => setIsApplyLoanModalOpen(true)}
                    style={{
                        backgroundColor: '#0d6360',
                        color: '#fff',
                        border: 'none',
                        padding: '10px 18px',
                        borderRadius: '8px',
                        cursor: 'pointer',
                        fontWeight: '600'
                    }}
                >
                    + Apply for Loan
                </button>
            </div>
            {loans.length === 0 ? (
                <p>No loans found.</p>
            ) : (
                loans.map((loan) => (
                    <div
                        key={loan.id}
                        style={{
                            border: '1px solid #ddd',
                            borderRadius: '8px',
                            padding: '16px',
                            marginBottom: '16px',
                            backgroundColor: '#fff'
                        }}
                    >
                        <div
                            style={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                marginBottom: '12px'
                            }}
                        >
                            <h3 style={{ margin: 0 }}>
                                {loan.loanType} Loan
                            </h3>

                            <span style={getStatusStyle(loan.status)}>
                                {loan.status}
                            </span>
                        </div>

                        <p><strong>Loan Number:</strong> {loan.loanNumber}</p>

                        <p><strong>Account:</strong> {loan.accountNumber}</p>

                        <p><strong>Principal:</strong> {formatCurrency(loan.principalAmount)}</p>

                        <p><strong>Remaining Balance:</strong> {formatCurrency(loan.remainingBalance)}</p>

                        <p><strong>Monthly EMI:</strong> {formatCurrency(loan.monthlyEmi)}</p>

                        <p><strong>Interest Rate:</strong> {loan.annualInterestRate}%</p>

                        {loan.nextDueDate && (
                            <p>
                                <strong>Next Due Date:</strong> {formatDate(loan.nextDueDate)}
                            </p>
                        )}

                        {loan.status === 'ACTIVE' && (
                            <button
                                style={{
                                    marginTop: '12px',
                                    padding: '8px 14px',
                                    border: 'none',
                                    borderRadius: '6px',
                                    backgroundColor: '#0d6360',
                                    color: '#fff',
                                    cursor: 'pointer'
                                }}
                                onClick={() => fetchRepayments(loan.loanNumber)}
                            >
                                View Repayments
                            </button>
                        )}
                        {selectedLoanNumber === loan.loanNumber && (
                            <div style={{ marginTop: '16px' }}>
                                <h4>Repayment History</h4>

                                {repayments.length === 0 ? (
                                    <p>No repayment history available.</p>
                                ) : (
                                    <table
                                        style={{
                                            width: '100%',
                                            borderCollapse: 'collapse',
                                            marginTop: '10px'
                                        }}
                                    >
                                        <thead>
                                            <tr style={{ backgroundColor: '#f3f4f6' }}>
                                                <th style={tableHeader}>Payment Date</th>
                                                <th style={tableHeader}>EMI Paid</th>
                                                <th style={tableHeader}>Principal</th>
                                                <th style={tableHeader}>Interest</th>
                                                <th style={tableHeader}>Remaining Balance</th>
                                            </tr>
                                        </thead>

                                        <tbody>
                                            {repayments.map((repayment) => (
                                                <tr key={repayment.id}>
                                                    <td style={tableCell}>{formatDate(repayment.paymentDate)}</td>
                                                    <td style={tableCell}>{formatCurrency(repayment.amountPaid)}</td>
                                                    <td style={tableCell}>{formatCurrency(repayment.principalComponent)}</td>
                                                    <td style={tableCell}>{formatCurrency(repayment.interestComponent)}</td>
                                                    <td style={tableCell}>{formatCurrency(repayment.remainingLoanBalance)}</td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                )}
                            </div>
                        )}
                    </div>
                ))
            )}
            <ApplyLoanModal
                isOpen={isApplyLoanModalOpen}
                onClose={() => setIsApplyLoanModalOpen(false)}
                accounts={accounts}
                onLoanApplied={fetchLoans}
            />
        </div>
    );
};

const tableHeader = {
    border: '1px solid #ddd',
    padding: '10px',
    textAlign: 'left',
    fontWeight: '600'
};

const tableCell = {
    border: '1px solid #ddd',
    padding: '10px'
};

export default LoansView;