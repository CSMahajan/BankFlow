import React, { useEffect, useState } from 'react';
import ApplyLoanModal from './ApplyLoanModal';
import PayEmiModal from './PayEmiModal';
import LoanDetailsModal from './LoanDetailsModal';
import { formatCurrency, formatDate } from '../../utils/formatUtils';
import { getLoanStatusStyle } from '../../utils/loanStatusUtils';
import { fetchMyLoans, fetchLoanRepayments } from "../../api/bankService";

const LoansView = ({ accounts }) => {
    const [isPayEmiModalOpen, setIsPayEmiModalOpen] = useState(false);
    const [isLoanDetailsModalOpen, setIsLoanDetailsModalOpen] = useState(false);
    const [selectedLoan, setSelectedLoan] = useState(null);
    const [isApplyLoanModalOpen, setIsApplyLoanModalOpen] = useState(false);
    const [repayments, setRepayments] = useState([]);
    const [repaymentLoading, setRepaymentLoading] = useState(false);
    const [repaymentError, setRepaymentError] = useState('');
    const [loans, setLoans] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchLoans = async () => {
        try {
            setLoading(true);
            setError(null);
            const loans = await fetchMyLoans();
            setLoans(loans);
        } catch (err) {
            console.error(err);
            setError("Failed to load loans.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchLoans();
    }, []);

    const openPayEmiModal = (loan) => {
        setSelectedLoan(loan);
        setIsPayEmiModalOpen(true);
    };

    const openLoanDetailsModal = async (loan) => {
        setSelectedLoan(loan);
        setRepayments([]);
        setIsLoanDetailsModalOpen(true);

        await fetchRepayments(loan.loanNumber);
    };

    const fetchRepayments = async (loanNumber) => {
        try {
            setRepaymentLoading(true);
            setRepaymentError('');
            setRepayments([]);

            const repayments = await fetchLoanRepayments(loanNumber);
            setRepayments(repayments);
        } catch (err) {
            console.error(err);
            setRepaymentError('Failed to load repayment history.');
        } finally {
            setRepaymentLoading(false);
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

                            <span style={getLoanStatusStyle(loan.status)}>
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

                        <div
                            style={{
                                display: 'flex',
                                gap: '10px',
                                marginTop: '12px'
                            }}
                        >
                            <button
                                style={{
                                    padding: '8px 14px',
                                    border: 'none',
                                    borderRadius: '6px',
                                    backgroundColor: '#0d6360',
                                    color: '#fff',
                                    cursor: 'pointer'
                                }}
                                onClick={() => openLoanDetailsModal(loan)}
                            >
                                Loan Details
                            </button>

                            {loan.status === 'ACTIVE' && (
                                <button
                                    style={{
                                        padding: '8px 14px',
                                        border: 'none',
                                        borderRadius: '6px',
                                        backgroundColor: '#0d6360',
                                        color: '#fff',
                                        cursor: 'pointer'
                                    }}
                                    onClick={() => openPayEmiModal(loan)}
                                >
                                    Pay EMI
                                </button>
                            )}
                        </div>
                    </div>
                ))
            )}
            <ApplyLoanModal
                isOpen={isApplyLoanModalOpen}
                onClose={() => setIsApplyLoanModalOpen(false)}
                accounts={accounts}
                onLoanApplied={fetchLoans}
            />
            <PayEmiModal
                isOpen={isPayEmiModalOpen}
                onClose={() => {
                    setIsPayEmiModalOpen(false);
                    setSelectedLoan(null);
                }}
                loan={selectedLoan}
                accounts={accounts}
                onPaymentSuccess={fetchLoans}
            />
            <LoanDetailsModal
                isOpen={isLoanDetailsModalOpen}
                onClose={() => {
                    setIsLoanDetailsModalOpen(false);
                    setSelectedLoan(null);
                    setRepayments([]);
                    setRepaymentError('');
                }}
                loan={selectedLoan}
                repayments={repayments}
                repaymentLoading={repaymentLoading}
                repaymentError={repaymentError}
            />
        </div>
    );
};

export default LoansView;