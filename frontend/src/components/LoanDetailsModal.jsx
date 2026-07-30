import React from 'react';
import modalStyles from '../styles/modalStyles';
import { formatCurrency, formatDate } from '../utils/formatUtils';
import { tableHeader, tableCell } from '../styles/tableStyles';

const LoanDetailsModal = ({
    isOpen,
    onClose,
    loan,
    repayments,
    repaymentLoading,
    repaymentError
}) => {
    if (!isOpen || !loan) return null;

    const DetailRow = ({ label, value }) => (
        <div
            style={{
                display: 'flex',
                justifyContent: 'space-between',
                padding: '10px 0',
                borderBottom: '1px solid #e5e7eb'
            }}
        >
            <strong>{label}</strong>
            <span>{value}</span>
        </div>
    );

    const SummaryCard = ({ title, value }) => (
        <div>
            <div
                style={{
                    fontSize: '12px',
                    color: '#6b7280'
                }}
            >
                {title}
            </div>

            <div
                style={{
                    marginTop: '4px',
                    fontWeight: '700',
                    fontSize: '18px'
                }}
            >
                {value}
            </div>
        </div>
    );

    const getStatusBadgeStyle = (status) => {
        switch (status) {
            case 'ACTIVE':
                return {
                    backgroundColor: '#dcfce7',
                    color: '#15803d'
                };

            case 'PENDING':
                return {
                    backgroundColor: '#fef3c7',
                    color: '#b45309'
                };

            case 'APPROVED':
                return {
                    backgroundColor: '#dbeafe',
                    color: '#1d4ed8'
                };

            case 'CLOSED':
                return {
                    backgroundColor: '#e5e7eb',
                    color: '#4b5563'
                };

            case 'REJECTED':
                return {
                    backgroundColor: '#fee2e2',
                    color: '#b91c1c'
                };

            default:
                return {
                    backgroundColor: '#f3f4f6',
                    color: '#374151'
                };
        }
    };

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

    return (
        <div style={modalStyles.overlay}>
            <div style={modalStyles.modal}>
                <div style={modalStyles.header}>
                    <h3 style={modalStyles.title}>
                        Loan Details
                    </h3>

                    <button
                        style={modalStyles.closeBtn}
                        onClick={onClose}
                    >
                        ✕
                    </button>
                </div>
                <div
                    style={{
                        backgroundColor: '#f9fafb',
                        borderRadius: '12px',
                        padding: '18px',
                        marginBottom: '20px'
                    }}
                >
                    <div
                        style={{
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center'
                        }}
                    >
                        <h2
                            style={{
                                margin: 0,
                                color: '#0d6360'
                            }}
                        >
                            {loan.loanType} Loan
                        </h2>

                        <div
                            style={{
                                display: 'inline-block',
                                padding: '4px 12px',
                                borderRadius: '999px',
                                fontWeight: '600',
                                fontSize: '13px',
                                ...getStatusBadgeStyle(loan.status)
                            }}
                        >
                            {loan.status}
                        </div>
                    </div>

                    <div
                        style={{
                            marginTop: '18px'
                        }}
                    >
                        <div
                            style={{
                                fontSize: '30px',
                                fontWeight: '700',
                                color: '#111827'
                            }}
                        >
                            ₹{loan.remainingBalance}
                        </div>

                        <div
                            style={{
                                marginTop: '4px',
                                color: '#6b7280',
                                fontSize: '14px'
                            }}
                        >
                            Remaining Balance
                        </div>
                    </div>

                    <div
                        style={{
                            display: 'grid',
                            gridTemplateColumns: 'repeat(3, 1fr)',
                            gap: '16px',
                            marginTop: '18px'
                        }}
                    >
                        <SummaryCard
                            title="Monthly EMI"
                            value={`₹${loan.monthlyEmi}`}
                        />

                        <SummaryCard
                            title="Next Due"
                            value={loan.nextDueDate}
                        />

                        <SummaryCard
                            title="Interest Rate"
                            value={`${loan.annualInterestRate}%`}
                        />
                    </div>
                </div>
                <h3
                    style={{
                        marginBottom: '16px',
                        color: '#374151'
                    }}
                >
                    Loan Information
                </h3>
                <div
                    style={{
                        display: 'grid',
                        gridTemplateColumns: '1fr 1fr',
                        gap: '12px 24px',
                        marginBottom: '20px'
                    }}
                >
                    <DetailRow
                        label="Loan Number"
                        value={loan.loanNumber}
                    />

                    <DetailRow
                        label="Principal Amount"
                        value={`₹${loan.principalAmount}`}
                    />


                    <DetailRow
                        label="Tenure"
                        value={`${loan.tenureMonths} months`}
                    />

                    <DetailRow
                        label="Start Date"
                        value={loan.startDate}
                    />
                </div>
                <hr style={{ margin: '24px 0' }} />

                <h3 style={{ marginBottom: '12px' }}>
                    Repayment History ({repayments.length})
                </h3>

                {repaymentLoading ? (
                    <p>Loading repayment history...</p>
                ) : repaymentError ? (
                    <p style={{ color: '#dc2626' }}>{repaymentError}</p>
                ) : repayments.length === 0 ? (
                    <p>No EMI payments have been made yet.</p>
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

                <div style={modalStyles.actions}>
                    <button
                        style={modalStyles.cancelBtn}
                        onClick={onClose}
                    >
                        Close
                    </button>
                </div>
            </div>
        </div>
    );
};

export default LoanDetailsModal;