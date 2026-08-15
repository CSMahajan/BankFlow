import React from 'react';
import SummaryCard from './SummaryCard';

const AdminDashboardOverview = ({
    summary,
    loading,
    error,
}) => {

    if (loading) return <div>Loading dashboard...</div>;

    if (error) return <div>{error}</div>;

    if (!summary) return null;

    return (
        <div
            style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(3, 1fr)',
                gap: '16px',
            }}
        >
            <SummaryCard
                icon="👥"
                title="Customers"
                value={summary.totalCustomers}
                subtitle="Registered Users"
            />

            <SummaryCard
                icon="💳"
                title="Accounts"
                value={summary.totalAccounts}
                subtitle="Bank Accounts"
            />

            <SummaryCard
                icon="🏠"
                title="Active Loans"
                value={summary.activeLoans}
                subtitle="Currently Running"
            />

            <SummaryCard
                icon="⏳"
                title="Pending Loans"
                value={summary.pendingLoans}
                subtitle="Awaiting Approval"
            />

            <SummaryCard
                icon="🪪"
                title="Pending KYC"
                value={summary.pendingKycDocuments}
                subtitle="Documents Awaiting Review"
            />

            <SummaryCard
                icon="💰"
                title="Fixed Deposits"
                value={summary.activeFixedDeposits}
                subtitle="Active FDs"
            />

            <SummaryCard
                icon="🏦"
                title="Total Deposits"
                value={`₹${Number(summary.totalDeposits).toLocaleString('en-IN')}`}
                subtitle="Across All Accounts"
            />
        </div>
    );
};

export default AdminDashboardOverview;