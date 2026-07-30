import React from 'react';
import BalanceSummaryCard from './BalanceSummaryCard';
import DashboardSummaryCard from './DashboardSummaryCard';

const DashboardOverview = ({
    accounts,
    refreshAccounts,
}) => {
    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <BalanceSummaryCard
                accounts={accounts}
                refreshAccounts={refreshAccounts}
            />

            <DashboardSummaryCard />
        </div>
    );
};

export default DashboardOverview;