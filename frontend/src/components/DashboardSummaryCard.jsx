import React from 'react';
import { formatDate, formatCurrency } from "../utils/formatUtils";

const DashboardSummaryCard = ({
    summary,
    loading,
    error,
}) => {

    if (loading) {
        return <div>Loading dashboard summary...</div>;
    }

    if (error) {
        return <div>{error}</div>;
    }

    return (
        <div
            style={{
                backgroundColor: '#ffffff',
                borderRadius: '16px',
                padding: '20px',
                border: '1px solid #eef0ec',
            }}
        >
            <h3 style={{ marginTop: 0 }}>Dashboard Summary</h3>

            <div
                style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(2, 1fr)',
                    gap: '16px',
                    marginTop: '16px',
                }}
            >
                <div style={cardStyle}>
                    <div style={labelStyle}>Net Worth</div>
                    <div style={valueStyle}>
                        {formatCurrency(summary.totalNetWorth)}
                    </div>
                </div>

                <div style={cardStyle}>
                    <div style={labelStyle}>Active Accounts</div>
                    <div style={valueStyle}>
                        {summary.activeAccountsCount}
                    </div>
                </div>

                <div style={cardStyle}>
                    <div style={labelStyle}>Fixed Deposits</div>
                    <div style={valueStyle}>
                        {summary.activeFdCount}
                    </div>
                </div>

                <div style={cardStyle}>
                    <div style={labelStyle}>Loans</div>
                    <div style={valueStyle}>
                        {summary.activeLoanCount}
                    </div>
                </div>
            </div>
            <div style={{ marginTop: '28px' }}>
                <h3 style={{ margin: '0 0 16px 0' }}>
                    Recent Transactions
                </h3>

                {summary.recentTransactions.length === 0 ? (
                    <div>No recent transactions.</div>
                ) : (
                    <div
                        style={{
                            display: 'flex',
                            flexDirection: 'column',
                            gap: '12px',
                        }}
                    >
                        {summary.recentTransactions.map((txn) => (
                            <div
                                key={txn.transactionId}
                                style={{
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    alignItems: 'center',
                                    padding: '12px',
                                    border: '1px solid #e5e7eb',
                                    borderRadius: '10px',
                                    backgroundColor: '#f9fafb',
                                }}
                            >
                                <div>
                                    <div style={{ fontWeight: '600' }}>
                                        {txn.description}
                                    </div>

                                    <div
                                        style={{
                                            fontSize: '12px',
                                            color: '#6b7280',
                                            marginTop: '4px',
                                        }}
                                    >
                                        {txn.accountNumber}
                                    </div>
                                </div>

                                <div
                                    style={{
                                        fontWeight: '700',
                                        color:
                                            txn.transactionType === 'CREDIT'
                                                ? '#15803d'
                                                : '#dc2626',
                                    }}
                                >
                                    {txn.transactionType === 'CREDIT' ? '+' : '-'}
                                    {formatCurrency(txn.amount)}
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
            {summary.activeLoanCount > 0 && (
                <div
                    style={{
                        marginTop: '28px',
                        padding: '16px',
                        border: '1px solid #fde68a',
                        backgroundColor: '#fffbeb',
                        borderRadius: '12px',
                    }}
                >
                    <h3 style={{ margin: '0 0 12px 0' }}>
                        Upcoming EMI
                    </h3>

                    <div>
                        <strong>Due Date:</strong> {summary.nextEmiDueDate ? formatDate(summary.nextEmiDueDate) : '-'}
                    </div>

                    <div style={{ marginTop: '8px' }}>
                        <strong>Amount:</strong>
                        {formatCurrency(summary.nextEmiAmount)}
                    </div>
                </div>
            )}
        </div>
    );
};

const cardStyle = {
    backgroundColor: '#f9fafb',
    border: '1px solid #e5e7eb',
    borderRadius: '12px',
    padding: '16px',
};

const labelStyle = {
    fontSize: '13px',
    color: '#6b7280',
    marginBottom: '8px',
};

const valueStyle = {
    fontSize: '24px',
    fontWeight: '700',
    color: '#111827',
};

export default DashboardSummaryCard;