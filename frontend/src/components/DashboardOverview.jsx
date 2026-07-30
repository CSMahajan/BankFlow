import React from 'react';
import BalanceSummaryCard from './BalanceSummaryCard';
import DashboardSummaryCard from './DashboardSummaryCard';
import FinancialSummaryCard from './FinancialSummaryCard';

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

            <FinancialSummaryCard />
        </div>
    );
};

export default DashboardOverview;