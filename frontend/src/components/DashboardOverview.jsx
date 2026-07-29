import React from 'react';
import BalanceSummaryCard from './BalanceSummaryCard';

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

            {/* Future dashboard widgets will go here */}
        </div>
    );
};

export default DashboardOverview;