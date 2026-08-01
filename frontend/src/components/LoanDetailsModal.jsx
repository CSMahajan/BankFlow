import React from 'react';
import modalStyles from '../styles/modalStyles';
import { formatCurrency, formatDate } from '../utils/formatUtils';
import { tableHeader, tableCell } from '../styles/tableStyles';
import { getLoanStatusStyle } from '../utils/loanStatusUtils';

const LoanDetailsModal = ({
    isOpen,
    onClose,
    loan,
    repayments,
    repaymentLoading,
    repaymentError
}) => {

    console.log("Loan Details:", loan);
    if (!isOpen || !loan) return null;
    <button
        onClick={() => toast.success("Hello BankFlow!")}
    >
        Test Toast
    </button>
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
                                ...getLoanStatusStyle(loan.status)
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
                            {formatCurrency(loan.remainingBalance)}
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
                            value={formatCurrency(loan.monthlyEmi)}
                        />

                        <SummaryCard
                            title="Next Due"
                            value={formatDate(loan.nextDueDate)}
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
                        value={formatCurrency(loan.principalAmount)}
                    />


                    <DetailRow
                        label="Tenure"
                        value={`${loan.tenureMonths} months`}
                    />

                    <DetailRow
                        label="Start Date"
                        value={formatDate(loan.startDate)}
                    />

                    {loan.status === "REJECTED" && loan.rejectionRemarks && (
                        <div
                            style={{
                                gridColumn: "1 / -1",
                                padding: "12px",
                                backgroundColor: "#fef2f2",
                                border: "1px solid #fecaca",
                                borderRadius: "8px"
                            }}
                        >
                            <strong style={{ color: "#b91c1c" }}>
                                Rejection Reason
                            </strong>

                            <p
                                style={{
                                    marginTop: "8px",
                                    marginBottom: 0,
                                    color: "#374151"
                                }}
                            >
                                {loan.rejectionRemarks}
                            </p>
                        </div>
                    )}
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